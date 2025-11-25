package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.alibaba.fastjson2.JSON;
import com.dayz.sapientiacloud_edupivot.celestial_hub.clients.MinIOClient;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileVectorizeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.FileDocument;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.FileDocumentEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.FileStatusEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.FileDocumentRepository;
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

    @Value("${spring.kafka.topic.file-vectorize:file-vectorize-topic}")
    private String fileVectorizeTopic;

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

            log.info("开始处理文件向量化任务, fileId: {}, partition: {}, offset: {}", fileId, partition, offset);

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

            log.info("文件向量化任务完成, fileId: {}", fileId);
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

            // 构建文档元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("file_id", fileDocument.getId().toString());
            metadata.put("file_name", fileDocument.getFileName());
            metadata.put("file_type", fileDocument.getFileType());
            metadata.put("course_id", fileDocument.getCourseId() != null ? fileDocument.getCourseId().toString() : null);
            metadata.put("session_id", fileDocument.getSessionId() != null ? fileDocument.getSessionId().toString() : null);
            metadata.put("user_id", fileDocument.getSysUserId() != null ? fileDocument.getSysUserId().toString() : null);
            metadata.put("chunk_index", i);
            metadata.put("chunk_total", chunks.size());
            metadata.put("content_type", "file_document");

            // 创建文档
            Document document = new Document(chunk, metadata);
            documents.add(document);
        }

        if (!documents.isEmpty()) {
            // 批量向量化并保存
            vectorStore.add(documents);
            log.debug("文件向量化完成, fileId: {}, chunks: {}", fileDocument.getId(), documents.size());

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
}

