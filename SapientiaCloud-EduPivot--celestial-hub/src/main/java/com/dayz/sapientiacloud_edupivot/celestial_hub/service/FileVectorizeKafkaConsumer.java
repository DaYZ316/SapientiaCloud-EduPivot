package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.alibaba.fastjson2.JSON;
import com.dayz.sapientiacloud_edupivot.celestial_hub.clients.MinIOClient;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.KnowledgeConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileVectorizeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.FileDocument;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.KnowledgeVector;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.FileDocumentEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.FileStatusEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.FileDocumentRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.KnowledgeVectorRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.VectorIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 文件向量化Kafka消费者
 * 从Kafka接收文件向量化任务，执行文件下载、解析和向量化
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileVectorizeKafkaConsumer {

    private final FileDocumentRepository fileDocumentRepository;
    private final MinIOClient minIOClient;
    private final IFileParserService fileParserService;
    private final VectorStore vectorStore;

    private final KnowledgeVectorRepository knowledgeVectorRepository;

    @Value("${spring.kafka.topic.file-vectorize:file-vectorize-topic}")
    private String fileVectorizeTopic;

    @Value("${spring.ai.vectorstore.redis.prefix:vector}")
    private String redisVectorKeyPrefix;

    /**
     * 消费文件向量化任务
     */
    @KafkaListener(topics = "${spring.kafka.topic.file-vectorize:file-vectorize-topic}",
            groupId = "${spring.kafka.consumer.group-id:chat-group}",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional(rollbackFor = Exception.class)
    public void consumeFileVectorizeTask(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        UUID fileId = null;
        try {
            // 解析消息
            FileVectorizeRequestDTO request = JSON.parseObject(message, FileVectorizeRequestDTO.class);
            fileId = request.getFileId();

            log.debug("开始处理文件向量化任务, fileId: {}, partition: {}, offset: {}", fileId, partition, offset);

            // 获取文件文档
            FileDocument fileDocument = fileDocumentRepository.findById(fileId)
                    .orElseThrow(() -> new BusinessException(FileDocumentEnum.FILE_NOT_EXISTS));

            // 更新状态为处理中
            fileDocument.setStatus(FileStatusEnum.PROCESSING.getCode());
            fileDocument.setUpdateTime(LocalDateTime.now());
            fileDocumentRepository.save(fileDocument);

            // 1. 从MinIO下载文件
            log.debug("从MinIO下载文件, fileId: {}, storagePath: {}", fileId, fileDocument.getStoragePath());
            Result<byte[]> downloadResult = minIOClient.downloadFile(
                    fileDocument.getStoragePath(),
                    fileDocument.getBucketCode()
            );

            if (downloadResult == null || !downloadResult.isSuccess() || downloadResult.getData() == null) {
                throw new BusinessException(FileDocumentEnum.FILE_DOWNLOAD_FAILED);
            }

            byte[] fileBytes = downloadResult.getData();
            InputStream inputStream = new ByteArrayInputStream(fileBytes);

            // 2. 解析文件内容
            log.debug("解析文件内容, fileId: {}, fileName: {}", fileId, fileDocument.getFileName());
            String fileContent = fileParserService.parseFile(
                    inputStream,
                    fileDocument.getFileName(),
                    fileDocument.getMimeType()
            );

            if (fileContent == null || fileContent.trim().isEmpty()) {
                log.warn("文件内容为空, fileId: {}", fileId);
                fileDocument.setStatus(FileStatusEnum.NORMAL.getCode());
                fileDocument.setIsVectorized(false);
                fileDocument.setUpdateTime(LocalDateTime.now());
                fileDocumentRepository.save(fileDocument);
                acknowledgment.acknowledge();
                return;
            }

            // 3. 向量化文件内容
            log.debug("向量化文件内容, fileId: {}, contentLength: {}", fileId, fileContent.length());
            vectorizeFileContent(fileDocument, fileContent);

            // 4. 更新状态为正常
            fileDocument.setStatus(FileStatusEnum.NORMAL.getCode());
            fileDocument.setIsVectorized(true);
            fileDocument.setUpdateTime(LocalDateTime.now());
            fileDocumentRepository.save(fileDocument);

            log.debug("文件向量化任务完成, fileId: {}", fileId);
            acknowledgment.acknowledge();

        } catch (BusinessException e) {
            log.error("文件向量化业务异常, fileId: {}, error: {}", fileId, e.getMessage());
            handleVectorizeError(fileId, e.getMessage());
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("文件向量化任务处理失败, fileId: {}, error: {}", fileId, e.getMessage(), e);
            handleVectorizeError(fileId, e.getMessage());
            // 可以选择不acknowledge，让消息重新消费，但需要设置重试次数限制
            acknowledgment.acknowledge();
        }
    }

    /**
     * 向量化文件内容
     */
    private void vectorizeFileContent(FileDocument fileDocument, String fileContent) {
        // 将文件内容分块（每块最大长度，避免超过向量化API限制）
        int chunkSize = 2000;
        List<String> chunks = splitContentIntoChunks(fileContent, chunkSize);

        List<Document> documents = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            if (chunk == null || chunk.trim().isEmpty()) {
                continue;
            }

            // 构建文档元数据（禁止 null 值），与课程/聊天向量保持统一结构
            Map<String, Object> metadata = new HashMap<>();

            // ===== 统一后的主结构（camelCase，与课程相关内容向量一致） =====
            // 内容类型：文件
            metadata.put(KnowledgeConstants.METADATA_CONTENT_TYPE, KnowledgeConstants.CONTENT_TYPE_FILE);
            // 以文件ID作为内容ID（contentId）
            metadata.put(KnowledgeConstants.METADATA_CONTENT_ID, fileDocument.getId().toString());
            // 课程ID
            putIfNotNull(metadata, KnowledgeConstants.METADATA_COURSE_ID, fileDocument.getCourseId());
            // 章节信息对文件向量暂不使用，保持为空字符串
            metadata.put(KnowledgeConstants.METADATA_CHAPTER_ID, KnowledgeConstants.DEFAULT_EMPTY_STRING);
            // 文件ID（fileId）
            metadata.put(KnowledgeConstants.METADATA_FILE_ID, fileDocument.getId().toString());
            // 会话ID => chatSessionId
            putIfNotNull(metadata, KnowledgeConstants.METADATA_SESSION_ID, fileDocument.getSessionId());
            // 用户ID
            putIfNotNull(metadata, KnowledgeConstants.METADATA_USER_ID, fileDocument.getSysUserId());
            // 标题统一用 title，取文件名
            putIfNotNull(metadata, KnowledgeConstants.METADATA_TITLE, fileDocument.getFileName());
            // 创建时间
            metadata.put(KnowledgeConstants.METADATA_CREATE_TIME,
                    fileDocument.getCreateTime() != null ? fileDocument.getCreateTime().toString() : LocalDateTime.now().toString());
            // tags 与 embeddingModel
            metadata.put(KnowledgeConstants.METADATA_TAGS, new ArrayList<String>());
            metadata.put(KnowledgeConstants.METADATA_EMBEDDING_MODEL, KnowledgeConstants.EMBEDDING_MODEL_TEXT_V1);
            // Chunk 索引
            metadata.put(KnowledgeConstants.METADATA_CHUNK_INDEX, i);
            String vectorId = VectorIdUtil.ensureVectorId(metadata, redisVectorKeyPrefix);

            // 创建文档（向量库内部可能会生成自己的ID，但我们自己维护的 vectorId 保存在 metadata 中）
            Document document = new Document(vectorId, chunk, metadata);
            documents.add(document);
        }

        if (!documents.isEmpty()) {
            // 批量向量化并保存
            vectorStore.add(documents);
            log.debug("文件向量化完成, fileId: {}, chunks: {}", fileDocument.getId(), documents.size());

            // 将文件向量元数据写入知识向量表，便于检索时补全信息
            List<KnowledgeVector> vectors = new ArrayList<>();
            for (Document doc : documents) {
                KnowledgeVector vector = buildFileKnowledgeVector(doc, fileDocument);
                if (vector != null) {
                    vectors.add(vector);
                }
            }
            if (!vectors.isEmpty()) {
                knowledgeVectorRepository.saveAll(vectors);
            }

            // 更新向量数量
            fileDocument.setVectorCount(documents.size());
        }
    }

    /**
     * 将内容分块
     */
    private List<String> splitContentIntoChunks(String content, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return chunks;
        }

        int length = content.length();
        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(i + chunkSize, length);
            chunks.add(content.substring(i, end));
        }

        return chunks;
    }

    /**
     * 处理向量化错误
     */
    private void handleVectorizeError(UUID fileId, String errorMessage) {
        try {
            FileDocument fileDocument = fileDocumentRepository.findById(fileId).orElse(null);
            if (fileDocument != null) {
                fileDocument.setStatus(FileStatusEnum.FAILED.getCode());
                fileDocument.setParseError(errorMessage);
                fileDocument.setUpdateTime(LocalDateTime.now());
                fileDocumentRepository.save(fileDocument);
            }
        } catch (Exception e) {
            log.error("更新文件状态失败, fileId: {}, error: {}", fileId, e.getMessage(), e);
        }
    }

    /**
     * 构建文件向量的 KnowledgeVector 记录
     */
    private KnowledgeVector buildFileKnowledgeVector(Document doc, FileDocument fileDocument) {
        if (doc == null) {
            return null;
        } else {
            doc.getId();
        }

        Map<String, Object> metadata = doc.getMetadata();

        KnowledgeVector vector = new KnowledgeVector();
        vector.setId(com.github.f4b6a3.uuid.UuidCreator.getTimeOrderedEpoch());
        vector.setVectorId(doc.getId());
        // 对于文件向量，courseId / userId / sessionId 等可以从 FileDocument 回填，避免 metadata 丢失时字段为空
        vector.setCourseId(fileDocument.getCourseId());
        vector.setChapterId(null);
        vector.setContentType(KnowledgeConstants.CONTENT_TYPE_FILE);
        // 使用文件ID作为内容ID
        vector.setContentId(fileDocument.getId());
        vector.setQuestionBankId(null);
        vector.setQuestionId(null);
        vector.setTaskId(null);
        vector.setForumId(null);
        vector.setPostId(null);
        vector.setUserId(fileDocument.getSysUserId());
        vector.setSessionId(fileDocument.getSessionId());
        vector.setTitle(fileDocument.getFileName());
        vector.setContent(doc.getFormattedContent());
        vector.setEmbeddingModel(KnowledgeConstants.EMBEDDING_MODEL_TEXT_V1);
        vector.setMetadata(metadata);
        // 文件标签暂不传递，保持为空列表
        vector.setTags(Collections.emptyList());
        vector.setStatus(StatusEnum.NORMAL.getCode());
        vector.setCreateTime(LocalDateTime.now());
        vector.setUpdateTime(LocalDateTime.now());

        return vector;
    }

    /**
     * 只在值非空时放入 metadata，避免 Spring AI 抛出空值异常
     */
    private void putIfNotNull(Map<String, Object> metadata, String key, Object value) {
        if (value == null) {
            return;
        }
        metadata.put(key, value instanceof UUID ? value.toString() : value);
    }
}

