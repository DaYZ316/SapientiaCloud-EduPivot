package com.dayz.sapientiacloud_edupivot.celestial_hub.service.impl;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.clients.CourseClient;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo.*;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.FileParserConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.KnowledgeConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KnowledgeSearchRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.VectorizeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.KnowledgeVector;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.KnowledgeSearchResultVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.ContentTypeEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.KnowledgeEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.KnowledgeVectorRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.KnowledgeService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.HtmlTextUtil;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.VectorIdUtil;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeVectorRepository knowledgeVectorRepository;
    private final CourseClient courseClient;
    private final VectorStore vectorStore;

    @Value("${spring.ai.vectorstore.redis.prefix:vector}")
    private String redisVectorKeyPrefix;

    /**
     * 构建基础metadata
     */
    private static Map<String, Object> buildBaseMetadata(Integer contentType, String contentId,
                                                         String courseId, String chapterId, String title) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(KnowledgeConstants.METADATA_CONTENT_TYPE, contentType);
        metadata.put(KnowledgeConstants.METADATA_CONTENT_ID, contentId != null ? contentId : KnowledgeConstants.DEFAULT_EMPTY_STRING);
        metadata.put(KnowledgeConstants.METADATA_COURSE_ID, courseId != null ? courseId : KnowledgeConstants.DEFAULT_EMPTY_STRING);
        metadata.put(KnowledgeConstants.METADATA_CHAPTER_ID, chapterId != null ? chapterId : KnowledgeConstants.DEFAULT_EMPTY_STRING);
        metadata.put(KnowledgeConstants.METADATA_TITLE, title != null ? title : KnowledgeConstants.DEFAULT_EMPTY_STRING);
        return metadata;
    }

    private static String safeString(Object value) {
        return value == null ? KnowledgeConstants.DEFAULT_EMPTY_STRING : String.valueOf(value);
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    /**
     * 检查标签是否匹配
     *
     * @param postTags   帖子标签
     * @param filterTags 过滤标签列表，如果为null或空则不过滤
     * @return true表示匹配（或不需要过滤），false表示不匹配
     */
    private static boolean matchesTags(List<String> postTags, List<String> filterTags) {
        if (filterTags == null || filterTags.isEmpty()) {
            return true;
        }
        if (postTags == null || postTags.isEmpty()) {
            return false;
        }
        // 检查是否有任一过滤标签在帖子标签中
        for (String filterTag : filterTags) {
            if (postTags.contains(filterTag)) {
                return true;
            }
        }
        return false;
    }

    private static int normalizeTopK(Integer topK) {
        if (topK == null || topK <= 0) {
            return KnowledgeConstants.DEFAULT_TOP_K;
        }
        return topK;
    }

    private static String buildQuestionText(QuestionVO q) {
        StringBuilder sb = new StringBuilder();
        if (q.getQuestionTitle() != null && !q.getQuestionTitle().isEmpty()) {
            sb.append(q.getQuestionTitle()).append("\n");
        }
        if (q.getQuestionContent() != null && !q.getQuestionContent().isEmpty()) {
            // 处理可能的HTML内容，转换为纯文本
            String plainContent = HtmlTextUtil.stripHtmlPreserveLines(q.getQuestionContent());
            sb.append(plainContent).append("\n");
        }

        // 添加选项（如果有）
        if (q.getOptions() != null && !q.getOptions().isEmpty()) {
            sb.append(KnowledgeConstants.QUESTION_OPTIONS_LABEL);
            for (var option : q.getOptions()) {
                if (option == null) {
                    continue;
                }
                String label = option.getOptionLabel() != null ? option.getOptionLabel() : "";
                String content = option.getOptionContent() != null ? option.getOptionContent() : "";
                if (!content.isEmpty()) {
                    // 处理可能的HTML内容
                    String plainOptionContent = HtmlTextUtil.stripHtmlPreserveLines(content);
                    sb.append(label).append(". ").append(plainOptionContent).append("\n");
                }
            }
        }

        // 添加答案（如果有）
        if (q.getAnswers() != null && !q.getAnswers().isEmpty()) {
            sb.append(KnowledgeConstants.QUESTION_ANSWERS_LABEL);
            for (var answer : q.getAnswers()) {
                if (answer == null) {
                    continue;
                }
                String answerContent = answer.getAnswerContent() != null ? answer.getAnswerContent() : "";
                if (!answerContent.isEmpty()) {
                    // 处理可能的HTML内容
                    String plainAnswerContent = HtmlTextUtil.stripHtmlPreserveLines(answerContent);
                    // 如果有序号，显示序号
                    if (answer.getSortOrder() != null) {
                        sb.append(KnowledgeConstants.ANSWER_ORDER_PREFIX)
                                .append(answer.getSortOrder())
                                .append(KnowledgeConstants.ANSWER_ORDER_SUFFIX);
                    }
                    sb.append(plainAnswerContent).append(FileParserConstants.LINE_SEPARATOR);
                }
            }
        }

        return sb.toString();
    }

    private static String buildTaskText(CourseTaskVO task) {
        StringBuilder sb = new StringBuilder();
        // 任务名称作为标题
        if (task.getTaskName() != null && !task.getTaskName().isEmpty()) {
            sb.append(task.getTaskName()).append("\n");
        }
        // 任务描述
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            sb.append(task.getDescription()).append("\n");
        }
        // 任务内容（富文本，需要转换为纯文本）
        if (task.getTaskContent() != null && !task.getTaskContent().isEmpty()) {
            String plainContent = HtmlTextUtil.stripHtmlPreserveLines(task.getTaskContent());
            sb.append(plainContent);
        }
        return sb.toString();
    }

    /**
     * 安全地将对象转换为字符串，处理null和空字符串
     */
    private static String safeToString(Object value) {
        if (value == null) {
            return null;
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return null;
        }
        return str;
    }

    /**
     * 安全解析UUID，处理null、空字符串和异常情况
     */
    private static UUID safeParseUUID(Object value) {
        String str = safeToString(value);
        if (str == null) {
            return null;
        }
        try {
            return UUID.fromString(str);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 安全解析Integer，处理null、空字符串和异常情况
     */
    private static Integer safeParseInteger(Object value) {
        String str = safeToString(value);
        if (str == null) {
            return null;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double safeParseDouble(Object value) {
        String str = safeToString(value);
        if (str == null) {
            return null;
        }
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double firstNonNullDouble(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object value : values) {
            Double parsed = safeParseDouble(value);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private static boolean hasMetadataFilter() {
        // 无论是否传入 sessionId，都会对文件向量进行额外处理，因此始终视为存在过滤逻辑
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void vectorizeCourseContent(VectorizeRequestDTO request) {
        try {
            if (!ContentTypeEnum.isValidCode(request.getContentType())) {
                throw new BusinessException(KnowledgeEnum.VECTORIZE_FAILED);
            }
            List<Document> documents = fetchContentForVectorization(request);

            if (documents.isEmpty()) {
                return;
            }

            // 分批处理文档，避免超过 DashScope API 的批次限制（25个）
            int batchSize = KnowledgeConstants.EMBEDDING_BATCH_SIZE;

            // 批量保存向量元数据，避免N+1问题
            List<KnowledgeVector> vectors = new ArrayList<>();

            for (int i = 0; i < documents.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, documents.size());
                List<Document> batch = documents.subList(i, endIndex);
                try {
                    vectorStore.add(batch);

                    // 为当前批次构建向量元数据
                    for (Document doc : batch) {
                        KnowledgeVector vector = buildKnowledgeVector(doc);
                        if (vector != null) {
                            vectors.add(vector);
                        }
                    }
                } catch (Exception e) {
                    throw e;
                }
            }

            // 批量保存所有向量元数据
            if (!vectors.isEmpty()) {
                knowledgeVectorRepository.saveAll(vectors);
            }
        } catch (Exception e) {
            throw new BusinessException(KnowledgeEnum.VECTORIZE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void vectorizeChatContent(String userQuery, String aiResponse, UUID sessionId, UUID messageId, UUID courseId, UUID userId) {
        try {
            // 验证输入参数
            if (userQuery == null || userQuery.trim().isEmpty()) {
                return;
            }
            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                return;
            }
            if (userId == null) {
                return;
            }

            // 按照 Q&A 对格式构建内容
            String content = KnowledgeConstants.CHAT_USER_PREFIX + userQuery + KnowledgeConstants.CHAT_AI_PREFIX + aiResponse;

            // 控制向量化文本长度，避免触发 DashScope 的长度限制错误
            if (content.length() > KnowledgeConstants.CHAT_EMBEDDING_MAX_LENGTH) {
                content = content.substring(0, KnowledgeConstants.CHAT_EMBEDDING_MAX_LENGTH);
            }
            // 额外兜底：若裁剪后内容为空，则直接跳过
            if (content.isEmpty()) {
                return;
            }

            // 构建元数据，将用户消息作为title记录
            Map<String, Object> metadata = buildBaseMetadata(
                    ContentTypeEnum.CHAT.getCode(),
                    messageId != null ? messageId.toString() : null,
                    courseId != null ? courseId.toString() : null,
                    null,
                    userQuery
            );

            // 添加会话ID和消息ID到元数据
            if (sessionId != null) {
                metadata.put(KnowledgeConstants.METADATA_SESSION_ID, sessionId.toString());
            }
            if (messageId != null) {
                metadata.put(KnowledgeConstants.METADATA_MESSAGE_ID, messageId.toString());
            }
            // 添加用户ID到元数据，用于用户隔离
            metadata.put(KnowledgeConstants.METADATA_USER_ID, userId.toString());
            // 添加创建时间到元数据
            metadata.put(KnowledgeConstants.METADATA_CREATE_TIME, LocalDateTime.now().toString());
            // 补充其他类型特有的字段（CHAT类型不使用，但保持结构完整）
            metadata.put(KnowledgeConstants.METADATA_QUESTION_BANK_ID, KnowledgeConstants.DEFAULT_EMPTY_STRING);
            metadata.put(KnowledgeConstants.METADATA_QUESTION_ID, KnowledgeConstants.DEFAULT_EMPTY_STRING);
            metadata.put(KnowledgeConstants.METADATA_TASK_ID, KnowledgeConstants.DEFAULT_EMPTY_STRING);
            metadata.put(KnowledgeConstants.METADATA_FORUM_ID, KnowledgeConstants.DEFAULT_EMPTY_STRING);
            metadata.put(KnowledgeConstants.METADATA_POST_ID, KnowledgeConstants.DEFAULT_EMPTY_STRING);
            // 补充tags和embeddingModel
            metadata.put(KnowledgeConstants.METADATA_TAGS, new ArrayList<String>());
            metadata.put(KnowledgeConstants.METADATA_EMBEDDING_MODEL, KnowledgeConstants.EMBEDDING_MODEL_TEXT_V1);

            // 创建 Document
            String vectorId = VectorIdUtil.ensureVectorId(metadata, redisVectorKeyPrefix);
            Document document = new Document(vectorId, content, metadata);

            // 向量化并保存
            vectorStore.add(List.of(document));

            // 保存向量元数据到数据库
            KnowledgeVector vector = buildKnowledgeVector(document);
            if (vector != null) {
                knowledgeVectorRepository.save(vector);
            }
        } catch (Exception e) {
            // 不抛出异常，避免影响聊天流程
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourseVectors(UUID courseId) {
        knowledgeVectorRepository.deleteByCourseId(courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteChapterVectors(UUID chapterId) {
        // 直接删除，避免先查询再删除的冗余操作
        knowledgeVectorRepository.deleteByChapterId(chapterId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCourseVectorCount(UUID courseId) {
        return knowledgeVectorRepository.countByCourseId(courseId);
    }

    private List<Document> fetchContentForVectorization(VectorizeRequestDTO request) {
        List<Document> documents = new ArrayList<>();

        documents.addAll(fetchChapterContent(request));
        documents.addAll(fetchQuestionContent(request));
        documents.addAll(fetchTaskContent(request));
        documents.addAll(fetchForumContent(request));

        return documents;
    }

    private List<Document> fetchChapterContent(VectorizeRequestDTO request) {
        if (!Objects.equals(ContentTypeEnum.CHAPTER.getCode(), request.getContentType())) {
            return new ArrayList<>();
        }

        List<CourseChapterVO> chapters = new ArrayList<>();
        Result<List<CourseChapterVO>> list;
        if (request.getCourseId() == null) {
            list = courseClient.listAllCourseChapter();
        } else {
            list = courseClient.listChaptersByCourseId(request.getCourseId());
        }
        if (list != null && list.isSuccess() && list.getData() != null) {
            chapters.addAll(list.getData());
        }

        List<Document> documents = new ArrayList<>();
        for (CourseChapterVO chapter : chapters) {
            if (chapter == null) {
                continue;
            }
            // 章节基础信息
            String id = chapter.getId() != null ? chapter.getId().toString() : KnowledgeConstants.DEFAULT_EMPTY_STRING;
            String courseId = chapter.getCourseId() != null ? chapter.getCourseId().toString() : KnowledgeConstants.DEFAULT_EMPTY_STRING;
            String title = chapter.getChapterName() != null ? chapter.getChapterName() : KnowledgeConstants.DEFAULT_EMPTY_STRING;
            String rawHtml = chapter.getContent() != null ? chapter.getContent() : KnowledgeConstants.DEFAULT_EMPTY_STRING;
            // 1) HTML -> 纯文本；2) 在正文前加入标题，增强检索语义；3) 长文进行分片
            String plain = HtmlTextUtil.stripHtmlPreserveLines(rawHtml);
            String enriched = HtmlTextUtil.buildChapterText(title, plain);

            List<String> chunks = HtmlTextUtil.chunkText(enriched,
                    KnowledgeConstants.DEFAULT_CHUNK_SIZE,
                    KnowledgeConstants.DEFAULT_CHUNK_OVERLAP);
            if (chunks.isEmpty()) {
                log.warn("Chapter produces no chunks: chapterId={}, title='{}', rawLen={}, plainLen={}",
                        id, title, rawHtml.length(), plain.length());
            } else {
                log.debug("Chapter chunked: chapterId={}, title='{}', rawLen={}, plainLen={}, chunkCount={}, chunkSize={}, overlap={}",
                        id, title, rawHtml.length(), plain.length(), chunks.size(),
                        KnowledgeConstants.DEFAULT_CHUNK_SIZE, KnowledgeConstants.DEFAULT_CHUNK_OVERLAP);
            }
            int chunkIndex = 0;
            for (String chunk : chunks) {
                if (chunk == null || chunk.trim().isEmpty()) {
                    continue; // 跳过空chunk
                }
                Map<String, Object> metadata = buildBaseMetadata(
                        ContentTypeEnum.CHAPTER.getCode(), id, courseId, id, title);
                metadata.put(KnowledgeConstants.METADATA_CHUNK_INDEX, chunkIndex++);
                String vectorId = VectorIdUtil.ensureVectorId(metadata, redisVectorKeyPrefix);
                documents.add(new Document(vectorId, chunk, metadata));
            }
        }
        log.debug("Prepared chapter documents: count={}, courseId={}",
                documents.size(),
                request.getCourseId());
        return documents;
    }

    private List<Document> fetchQuestionContent(VectorizeRequestDTO request) {
        if (!Objects.equals(ContentTypeEnum.QUESTION.getCode(), request.getContentType())) {
            return new ArrayList<>();
        }

        List<Document> documents = new ArrayList<>();
        Map<UUID, UUID> bankIdToCourseId = new HashMap<>();

        if (request.getCourseId() == null) {
            // courseId为空时，调用listAllQuestion获取所有问题
            Result<List<QuestionVO>> allQuestionsResult = courseClient.listAllQuestion();
            if (allQuestionsResult == null || !allQuestionsResult.isSuccess() || allQuestionsResult.getData() == null) {
                return new ArrayList<>();
            }

            // 获取所有问题的题库ID，然后批量查询题库信息以建立映射
            Set<UUID> bankIds = new HashSet<>();
            for (QuestionVO q : allQuestionsResult.getData()) {
                if (q != null && q.getQuestionBankId() != null) {
                    bankIds.add(q.getQuestionBankId());
                }
            }

            // 为每个题库ID查询对应的课程ID
            // TODO: 性能优化建议 - 如果 CourseClient 提供批量查询接口 listQuestionBanksByIds(List<UUID> bankIds)，
            // 应该使用批量查询替代循环中的单个查询，以避免 N+1 查询问题
            for (UUID bankId : bankIds) {
                Result<CourseQuestionBankVO> bankResult = courseClient.getQuestionBankById(bankId);
                if (bankResult != null && bankResult.isSuccess() && bankResult.getData() != null) {
                    CourseQuestionBankVO bank = bankResult.getData();
                    if (bank.getCourseId() != null) {
                        bankIdToCourseId.put(bankId, bank.getCourseId());
                    }
                }
            }

            // 处理所有问题
            for (QuestionVO q : allQuestionsResult.getData()) {
                Document doc = buildQuestionDocument(q, bankIdToCourseId);
                if (doc != null) {
                    documents.add(doc);
                }
            }
        } else {
            // courseId不为空时，使用原有逻辑
            Result<List<CourseQuestionBankVO>> bankResult = courseClient.listQuestionBanksByCourseId(request.getCourseId());
            if (bankResult == null || !bankResult.isSuccess() || bankResult.getData() == null) {
                return new ArrayList<>();
            }

            // 建立题库到课程ID的映射，便于题目元数据写入
            for (CourseQuestionBankVO bank : bankResult.getData()) {
                if (bank != null && bank.getId() != null) {
                    bankIdToCourseId.put(bank.getId(), bank.getCourseId());
                }
            }

            // TODO: 性能优化建议 - 如果 CourseClient 提供批量查询接口 listQuestionsByBankIds(List<UUID> bankIds)，
            // 应该使用批量查询替代循环中的单个查询，以避免 N+1 查询问题
            for (CourseQuestionBankVO bank : bankResult.getData()) {
                if (bank == null || bank.getId() == null) {
                    continue;
                }
                Result<List<QuestionVO>> questionsResult = courseClient.listQuestionsByBankId(bank.getId());
                if (questionsResult == null || !questionsResult.isSuccess() || questionsResult.getData() == null) {
                    continue;
                }
                for (QuestionVO q : questionsResult.getData()) {
                    Document doc = buildQuestionDocument(q, bankIdToCourseId);
                    if (doc != null) {
                        documents.add(doc);
                    }
                }
            }
        }
        return documents;
    }

    /**
     * 构建问题 Document（提取公共逻辑，避免代码重复）
     *
     * @param q                问题VO
     * @param bankIdToCourseId 题库ID到课程ID的映射
     * @return Document对象，如果问题无效则返回null
     */
    private Document buildQuestionDocument(QuestionVO q, Map<UUID, UUID> bankIdToCourseId) {
        if (q == null || q.getId() == null) {
            return null;
        }
        String content = buildQuestionText(q);
        if (content.trim().isEmpty()) {
            return null;
        }

        Map<String, Object> metadata = buildBaseMetadata(
                ContentTypeEnum.QUESTION.getCode(),
                q.getId().toString(),
                safeString(bankIdToCourseId.get(q.getQuestionBankId())),
                null,
                emptyToNull(q.getQuestionTitle()));
        // 添加问题相关的ID到metadata
        if (q.getQuestionBankId() != null) {
            metadata.put(KnowledgeConstants.METADATA_QUESTION_BANK_ID, q.getQuestionBankId().toString());
        }
        metadata.put(KnowledgeConstants.METADATA_QUESTION_ID, q.getId().toString());
        // 保存tags到metadata，过滤掉null元素
        if (q.getTags() != null && !q.getTags().isEmpty()) {
            List<String> filteredTags = q.getTags().stream()
                    .filter(tag -> tag != null && !tag.isEmpty())
                    .toList();
            if (!filteredTags.isEmpty()) {
                metadata.put(KnowledgeConstants.METADATA_TAGS, filteredTags);
            }
        }

        String vectorId = VectorIdUtil.ensureVectorId(metadata, redisVectorKeyPrefix);
        return new Document(vectorId, content, metadata);
    }

    private List<Document> fetchTaskContent(VectorizeRequestDTO request) {
        if (!Objects.equals(ContentTypeEnum.TASK.getCode(), request.getContentType())) {
            return new ArrayList<>();
        }

        Result<List<CourseTaskVO>> taskResult;
        if (request.getCourseId() == null) {
            taskResult = courseClient.listAllCourseTask();
        } else {
            taskResult = courseClient.listTasksByCourseId(request.getCourseId());
        }
        if (taskResult == null || !taskResult.isSuccess() || taskResult.getData() == null) {
            return new ArrayList<>();
        }

        List<Document> documents = new ArrayList<>();
        for (CourseTaskVO task : taskResult.getData()) {
            if (task == null || task.getId() == null) {
                continue;
            }
            String content = buildTaskText(task);
            if (content.trim().isEmpty()) {
                continue; // 跳过空内容
            }

            Map<String, Object> metadata = buildBaseMetadata(
                    ContentTypeEnum.TASK.getCode(),
                    task.getId().toString(),
                    safeString(task.getCourseId()),
                    null,
                    emptyToNull(task.getTaskName()));
            // 添加任务ID到metadata
            metadata.put(KnowledgeConstants.METADATA_TASK_ID, task.getId().toString());

            String vectorId = VectorIdUtil.ensureVectorId(metadata, redisVectorKeyPrefix);
            documents.add(new Document(vectorId, content, metadata));
        }
        return documents;
    }

    private List<Document> fetchForumContent(VectorizeRequestDTO request) {
        if (!Objects.equals(ContentTypeEnum.FORUM.getCode(), request.getContentType())) {
            return new ArrayList<>();
        }

        Result<List<ForumPostVO>> postResult;
        if (request.getCourseId() == null) {
            postResult = courseClient.listAllForumPost();
        } else {
            postResult = courseClient.listForumPostsByCourseId(request.getCourseId());
        }
        if (postResult == null || !postResult.isSuccess() || postResult.getData() == null) {
            return new ArrayList<>();
        }

        List<Document> documents = new ArrayList<>();
        for (ForumPostVO post : postResult.getData()) {
            if (post == null) {
                continue;
            }
            // 可选标签过滤
            if (!matchesTags(post.getTags(), request.getTags())) {
                continue;
            }

            String id = post.getId() != null ? post.getId().toString() : KnowledgeConstants.DEFAULT_EMPTY_STRING;
            String courseId = post.getCourseId() != null ? post.getCourseId().toString() : KnowledgeConstants.DEFAULT_EMPTY_STRING;
            String title = post.getTitle() != null ? post.getTitle() : KnowledgeConstants.DEFAULT_EMPTY_STRING;
            String content = post.getContent() != null ? post.getContent() : KnowledgeConstants.DEFAULT_EMPTY_STRING;

            if (content.trim().isEmpty()) {
                continue; // 跳过空内容
            }

            Map<String, Object> metadata = buildBaseMetadata(
                    ContentTypeEnum.FORUM.getCode(), id, courseId, null, title);
            // 添加论坛ID和帖子ID到metadata
            if (post.getForumId() != null) {
                metadata.put(KnowledgeConstants.METADATA_FORUM_ID, post.getForumId().toString());
            }
            metadata.put(KnowledgeConstants.METADATA_POST_ID, id);
            // 保存tags到metadata，过滤掉null元素
            if (post.getTags() != null && !post.getTags().isEmpty()) {
                List<String> filteredTags = post.getTags().stream()
                        .filter(tag -> tag != null && !tag.isEmpty())
                        .toList();
                if (!filteredTags.isEmpty()) {
                    metadata.put(KnowledgeConstants.METADATA_TAGS, filteredTags);
                }
            }

            String vectorId = VectorIdUtil.ensureVectorId(metadata, redisVectorKeyPrefix);
            documents.add(new Document(vectorId, content, metadata));
        }
        return documents;
    }

    /**
     * 构建KnowledgeVector对象（用于批量保存）
     */
    private KnowledgeVector buildKnowledgeVector(Document doc) {
        if (doc == null) {
            return null;
        } else {
            doc.getId();
        }

        Map<String, Object> metadata = doc.getMetadata();
        Object courseIdObj = metadata.get(KnowledgeConstants.METADATA_COURSE_ID);
        Object chapterIdObj = metadata.get(KnowledgeConstants.METADATA_CHAPTER_ID);
        Object contentIdObj = metadata.get(KnowledgeConstants.METADATA_CONTENT_ID);
        Object contentTypeObj = metadata.get(KnowledgeConstants.METADATA_CONTENT_TYPE);
        Object questionBankIdObj = metadata.get(KnowledgeConstants.METADATA_QUESTION_BANK_ID);
        Object questionIdObj = metadata.get(KnowledgeConstants.METADATA_QUESTION_ID);
        Object taskIdObj = metadata.get(KnowledgeConstants.METADATA_TASK_ID);
        Object forumIdObj = metadata.get(KnowledgeConstants.METADATA_FORUM_ID);
        Object postIdObj = metadata.get(KnowledgeConstants.METADATA_POST_ID);
        Object userIdObj = metadata.get(KnowledgeConstants.METADATA_USER_ID);
        Object sessionIdObj = metadata.get(KnowledgeConstants.METADATA_SESSION_ID);

        KnowledgeVector vector = new KnowledgeVector();
        vector.setId(UuidCreator.getTimeOrderedEpoch());
        // 优先使用我们在 metadata 中写入的自定义 vectorId，旧数据则回退为 Document.getId()
        Object customVectorId = metadata.get(KnowledgeConstants.METADATA_VECTOR_ID);
        String vectorId = customVectorId != null ? customVectorId.toString() : doc.getId();
        vector.setVectorId(vectorId);
        vector.setCourseId(safeParseUUID(courseIdObj));
        vector.setChapterId(safeParseUUID(chapterIdObj));
        vector.setContentType(safeParseInteger(contentTypeObj));
        vector.setContentId(safeParseUUID(contentIdObj));
        vector.setQuestionBankId(safeParseUUID(questionBankIdObj));
        vector.setQuestionId(safeParseUUID(questionIdObj));
        vector.setTaskId(safeParseUUID(taskIdObj));
        vector.setForumId(safeParseUUID(forumIdObj));
        vector.setPostId(safeParseUUID(postIdObj));
        vector.setUserId(safeParseUUID(userIdObj));
        vector.setSessionId(safeParseUUID(sessionIdObj));
        Object titleObj = metadata.get(KnowledgeConstants.METADATA_TITLE);
        vector.setTitle(titleObj == null ? KnowledgeConstants.DEFAULT_EMPTY_STRING : String.valueOf(titleObj));
        vector.setContent(doc.getFormattedContent());
        vector.setEmbeddingModel(KnowledgeConstants.EMBEDDING_MODEL_TEXT_V1);
        vector.setMetadata(metadata);
        // 从metadata中提取tags并保存
        Object tagsObj = metadata.get(KnowledgeConstants.METADATA_TAGS);
        if (tagsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) tagsObj;
            vector.setTags(tags);
        }
        vector.setStatus(StatusEnum.NORMAL.getCode());
        vector.setCreateTime(LocalDateTime.now());
        vector.setUpdateTime(LocalDateTime.now());

        return vector;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void vectorizeFileDocument(UUID fileId) {
        // 注意：此方法存在循环依赖问题（KnowledgeService <-> FileDocumentService）
        // 已通过 @Lazy 注解在 FileDocumentServiceImpl 中解决循环依赖
        // 实际的文件向量化逻辑在 FileDocumentServiceImpl 中通过 Kafka 异步处理
        // 如果需要直接调用，应该通过事件机制（Spring Events）或独立的服务类来解耦
    }

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgeSearchResultVO> searchKnowledge(KnowledgeSearchRequestDTO request) {
        if (request == null || !StringUtils.hasText(request.getQuery())) {
            throw new BusinessException(KnowledgeEnum.SEARCH_QUERY_REQUIRED);
        }

        try {
            UUID currentUserId = UserContextUtil.getCurrentUserId();

            int topK = normalizeTopK(request.getTopK());
            double threshold = request.getSimilarityThreshold() != null
                    ? request.getSimilarityThreshold()
                    : KnowledgeConstants.DEFAULT_SIMILARITY_THRESHOLD;

            log.debug("知识检索：规范化后的参数 - topK: {}, 相似度阈值: {}", topK, threshold);

            int fetchTopK = topK;
            boolean hasFilter = hasMetadataFilter();
            if (hasFilter) {
                fetchTopK = Math.min(topK * 3, 500);
                fetchTopK = Math.max(fetchTopK, topK);
                log.debug("知识检索：存在元数据过滤，调整 fetchTopK 从 {} 到 {}", topK, fetchTopK);
            }

            // 处理文件引用：如果有fileReferences，先查询对应的向量数据（带验证）
            List<KnowledgeVector> fileVectors = null;
            if (request.getFileReferences() != null && !request.getFileReferences().isEmpty()) {
                fileVectors = findVectorsByFileReferences(request.getFileReferences(), currentUserId, request.getSessionId());
                log.debug("知识检索：文件向量数据查询完成，找到 {} 条向量记录",
                        fileVectors != null ? fileVectors.size() : 0);
            }

            SearchRequest searchRequest = SearchRequest.builder()
                    .query(request.getQuery())
                    .topK(fetchTopK)
                    .similarityThreshold(threshold)
                    .build();

            log.debug("知识检索：开始向量相似度搜索，查询内容: '{}', fetchTopK: {}, threshold: {}",
                    request.getQuery(), fetchTopK, threshold);

            List<Document> documents = vectorStore.similaritySearch(searchRequest);

            log.debug("知识检索：向量相似度搜索完成，返回文档数量: {}",
                    documents != null ? documents.size() : 0);

            List<KnowledgeSearchResultVO> results = new ArrayList<>();

            // 处理向量相似度搜索结果
            if (documents != null && !documents.isEmpty()) {
                List<Document> candidateDocuments = new ArrayList<>();
                List<String> candidateVectorIds = new ArrayList<>();
                for (Document document : documents) {
                    String vectorId = resolveVectorId(document);
                    if (!StringUtils.hasText(vectorId)) {
                        continue;
                    }
                    candidateDocuments.add(document);
                    candidateVectorIds.add(vectorId);
                    if (candidateDocuments.size() >= topK) {
                        break;
                    }
                }


                if (!candidateDocuments.isEmpty()) {
                    Map<String, KnowledgeVector> vectorMap = buildVectorMap(candidateVectorIds);

                    for (int i = 0; i < candidateDocuments.size(); i++) {
                        Document document = candidateDocuments.get(i);
                        String vectorId = candidateVectorIds.get(i);
                        KnowledgeVector vector = vectorMap.get(vectorId);
                        if (vector == null) {
                            continue;
                        }
                        if (shouldIncludeVector(vector, request, currentUserId)) {
                            continue;
                        }
                        KnowledgeSearchResultVO vo = buildSearchResult(document, vector);
                        if (vo != null) {
                            results.add(vo);
                        }
                    }
                }
            }

            // 如果有文件引用，将文件向量数据转换为搜索结果并添加到结果中
            // 即使相似度搜索没有结果，也应该返回文件向量数据
            if (fileVectors != null && !fileVectors.isEmpty()) {
                for (KnowledgeVector fileVector : fileVectors) {
                    if (fileVector == null) {
                        continue;
                    }
                    // 检查是否已经存在于结果中（避免重复）
                    boolean alreadyExists = results.stream()
                            .anyMatch(vo -> vo.getVectorId() != null && vo.getVectorId().equals(fileVector.getVectorId()));
                    if (alreadyExists) {
                        continue;
                    }

                    // 检查文件向量是否满足包含条件（会话ID、用户ID等）
                    if (shouldIncludeVector(fileVector, request, currentUserId)) {
                        continue;
                    }

                    // 将文件向量转换为搜索结果
                    KnowledgeSearchResultVO fileVo = convertVectorToSearchResult(fileVector);
                    if (fileVo != null) {
                        results.add(fileVo);
                    }
                }
            }
            return results;
        } catch (Exception e) {
            log.error("知识检索失败，查询内容: '{}', 错误信息: {}", request.getQuery(), e.getMessage(), e);
            throw new BusinessException(KnowledgeEnum.SEARCH_FAILED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String retrieveFileContext(List<UUID> fileIds, String query, Integer topK) {
        if (fileIds == null || fileIds.isEmpty() || !StringUtils.hasText(query)) {
            return null;
        }

        int searchTopK = normalizeTopK(topK);
        double threshold = KnowledgeConstants.DEFAULT_SIMILARITY_THRESHOLD;

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(searchTopK)
                .similarityThreshold(threshold)
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);

        if (results == null || results.isEmpty()) {
            return null;
        }

        // 过滤出属于指定文件的内容
        StringBuilder context = new StringBuilder();
        int count = 0;
        for (Document doc : results) {
            Map<String, Object> metadata = doc.getMetadata();
            Object fileIdObj = metadata.get(KnowledgeConstants.METADATA_FILE_ID);
            if (fileIdObj != null) {
                try {
                    UUID docFileId = UUID.fromString(fileIdObj.toString());
                    if (fileIds.contains(docFileId) && count < searchTopK) {
                        String content = doc.getFormattedContent();
                        if (StringUtils.hasText(content)) {
                            if (count > 0) {
                                context.append(FileParserConstants.LINE_SEPARATOR)
                                        .append(FileParserConstants.LINE_SEPARATOR);
                            }
                            context.append(KnowledgeConstants.FILE_CHUNK_PREFIX)
                                    .append(count + 1)
                                    .append(KnowledgeConstants.FILE_CHUNK_SUFFIX);
                            context.append(content);
                            count++;
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse fileId from metadata: {}", fileIdObj, e);
                }
            }
        }

        return count > 0 ? context.toString() : null;
    }

    private KnowledgeSearchResultVO buildSearchResult(Document document, KnowledgeVector vector) {
        if (document == null || vector == null) {
            return null;
        }
        String vectorId = StringUtils.hasText(vector.getVectorId()) ? vector.getVectorId() : resolveVectorId(document);
        if (!StringUtils.hasText(vectorId)) {
            log.warn("searchKnowledge skip docId={} due to missing vectorId", document.getId());
            return null;
        }

        KnowledgeSearchResultVO vo = new KnowledgeSearchResultVO();
        vo.setDocumentId(document.getId());
        vo.setContent(sanitizeContent(document.getFormattedContent()));
        vo.setVectorId(vectorId);

        vo.setContentType(vector.getContentType());
        vo.setTitle(vector.getTitle());
        vo.setCourseId(vector.getCourseId());
        vo.setChapterId(vector.getChapterId());
        vo.setContentId(vector.getContentId());
        vo.setQuestionBankId(vector.getQuestionBankId());
        vo.setQuestionId(vector.getQuestionId());
        vo.setTaskId(vector.getTaskId());
        vo.setForumId(vector.getForumId());
        vo.setPostId(vector.getPostId());
        vo.setSessionId(vector.getSessionId());
        vo.setMessageId(safeParseUUID(vector.getMetadata() != null
                ? vector.getMetadata().get(KnowledgeConstants.METADATA_MESSAGE_ID)
                : null));
        vo.setUserId(vector.getUserId());
        vo.setFileId(vector.getMetadata() != null
                ? safeParseUUID(vector.getMetadata().get(KnowledgeConstants.METADATA_FILE_ID))
                : null);
        vo.setChunkIndex(vector.getMetadata() != null
                ? safeParseInteger(vector.getMetadata().get(KnowledgeConstants.METADATA_CHUNK_INDEX))
                : null);
        vo.setCreateTime(vector.getCreateTime());
        vo.setEmbeddingModel(vector.getEmbeddingModel());
        vo.setTags(vector.getTags());

        Map<String, Object> docMetadata = document.getMetadata();
        vo.setScore(firstNonNullDouble(docMetadata.get(KnowledgeConstants.METADATA_VECTOR_SCORE),
                docMetadata.get(KnowledgeConstants.METADATA_SCORE)));
        vo.setDistance(firstNonNullDouble(docMetadata.get(KnowledgeConstants.METADATA_DISTANCE), null));

        return vo;
    }

    /**
     * 将KnowledgeVector转换为KnowledgeSearchResultVO（用于文件向量直接索引）
     */
    private KnowledgeSearchResultVO convertVectorToSearchResult(KnowledgeVector vector) {
        if (vector == null) {
            return null;
        }

        String vectorId = vector.getVectorId();
        if (!StringUtils.hasText(vectorId)) {
            log.debug("知识检索：跳过向量转换，原因：缺少vectorId，向量ID: {}", vector.getId());
            return null;
        }

        KnowledgeSearchResultVO vo = new KnowledgeSearchResultVO();
        vo.setDocumentId(vectorId);
        vo.setContent(sanitizeContent(vector.getContent()));
        vo.setVectorId(vectorId);

        vo.setContentType(vector.getContentType());
        vo.setTitle(vector.getTitle());
        vo.setCourseId(vector.getCourseId());
        vo.setChapterId(vector.getChapterId());
        vo.setContentId(vector.getContentId());
        vo.setQuestionBankId(vector.getQuestionBankId());
        vo.setQuestionId(vector.getQuestionId());
        vo.setTaskId(vector.getTaskId());
        vo.setForumId(vector.getForumId());
        vo.setPostId(vector.getPostId());
        vo.setSessionId(vector.getSessionId());
        vo.setMessageId(vector.getMetadata() != null
                ? safeParseUUID(vector.getMetadata().get(KnowledgeConstants.METADATA_MESSAGE_ID))
                : null);
        vo.setUserId(vector.getUserId());
        vo.setFileId(vector.getMetadata() != null
                ? safeParseUUID(vector.getMetadata().get(KnowledgeConstants.METADATA_FILE_ID))
                : null);
        vo.setChunkIndex(vector.getMetadata() != null
                ? safeParseInteger(vector.getMetadata().get(KnowledgeConstants.METADATA_CHUNK_INDEX))
                : null);
        vo.setCreateTime(vector.getCreateTime());
        vo.setEmbeddingModel(vector.getEmbeddingModel());
        vo.setTags(vector.getTags());

        // 文件向量直接索引没有相似度分数和距离，设置为null
        vo.setScore(null);
        vo.setDistance(null);

        return vo;
    }

    private Map<String, KnowledgeVector> buildVectorMap(List<String> vectorIds) {
        if (vectorIds == null || vectorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<KnowledgeVector> vectors = knowledgeVectorRepository.findByVectorIdIn(new HashSet<>(vectorIds));
        Map<String, KnowledgeVector> vectorMap = new HashMap<>(vectors.size());
        for (KnowledgeVector vector : vectors) {
            if (vector == null || !StringUtils.hasText(vector.getVectorId())) {
                continue;
            }
            vectorMap.putIfAbsent(vector.getVectorId(), vector);
        }
        return vectorMap;
    }

    private String sanitizeContent(String content) {
        if (content == null) {
            return null;
        }
        String[] lines = content.split("\\r?\\n", -1);
        StringBuilder sb = new StringBuilder();
        boolean trimmingHead = true;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmingHead) {
                boolean isDebugLine = trimmed.startsWith(KnowledgeConstants.DEBUG_DISTANCE_PREFIX)
                        || trimmed.startsWith(KnowledgeConstants.DEBUG_VECTOR_SCORE_PREFIX);
                if (isDebugLine || trimmed.isEmpty()) {
                    continue;
                }
                trimmingHead = false;
            }
            if (!sb.isEmpty()) {
                sb.append(System.lineSeparator());
            }
            sb.append(line);
        }
        return sb.toString();
    }

    private String resolveVectorId(Document document) {
        if (document == null) {
            return null;
        }
        Map<String, Object> metadata = document.getMetadata();
        String vectorId = safeString(metadata.get(KnowledgeConstants.METADATA_VECTOR_ID));
        if (StringUtils.hasText(vectorId)) {
            return vectorId;
        }
        if (!StringUtils.hasText(document.getId())) {
            return null;
        }
        String docId = document.getId();
        String prefix = StringUtils.hasText(redisVectorKeyPrefix) ? redisVectorKeyPrefix : "vector";
        String prefixWithColon = prefix + ":";
        String candidate = null;
        if (StringUtils.hasText(prefix)) {
            if (docId.startsWith(prefixWithColon)) {
                candidate = docId.substring(prefixWithColon.length());
            } else if (docId.startsWith(prefix)) {
                candidate = docId.substring(prefix.length());
            }
        }
        if (!StringUtils.hasText(candidate)) {
            int colonIndex = docId.indexOf(':');
            if (colonIndex >= 0 && colonIndex < docId.length() - 1) {
                candidate = docId.substring(colonIndex + 1);
            } else {
                candidate = docId;
            }
        }
        return StringUtils.hasText(candidate) ? candidate : null;
    }

    private boolean shouldIncludeVector(KnowledgeVector vector, KnowledgeSearchRequestDTO request, UUID currentUserId) {
        if (vector == null) {
            log.debug("知识检索：向量包含检查失败，原因：向量为空");
            return true;
        }
        Integer contentType = vector.getContentType();
        boolean isFileVector = Objects.equals(contentType, KnowledgeConstants.CONTENT_TYPE_FILE);
        boolean isChatVector = Objects.equals(contentType, KnowledgeConstants.CONTENT_TYPE_CHAT);
        UUID requestSessionId = request != null ? request.getSessionId() : null;

        log.debug("知识检索：向量包含检查，向量ID: {}, 内容类型: {}, 是否为文件向量: {}, 是否为聊天向量: {}, 请求会话ID: {}, 当前用户ID: {}",
                vector.getVectorId(), contentType, isFileVector, isChatVector, requestSessionId, currentUserId);

        if (isFileVector) {
            if (requestSessionId == null) {
                log.debug("知识检索：跳过文件向量，原因：请求中缺少会话ID，向量ID: {}", vector.getVectorId());
                return true;
            }
            UUID vectorSessionId = vector.getSessionId();
            boolean matched = requestSessionId.equals(vectorSessionId);
            if (!matched) {
                log.debug("知识检索：跳过文件向量，原因：会话ID不匹配，向量ID: {}, 请求会话ID: {}, 向量会话ID: {}",
                        vector.getVectorId(), requestSessionId, vectorSessionId);
                return true;
            }
            if (currentUserId == null) {
                log.debug("知识检索：跳过文件向量，原因：当前用户ID为空，向量ID: {}", vector.getVectorId());
                return true;
            }
            UUID vectorUserId = vector.getUserId();
            boolean userMatched = currentUserId.equals(vectorUserId);
            if (!userMatched) {
                log.debug("知识检索：跳过文件向量，原因：用户ID不匹配，向量ID: {}, 当前用户ID: {}, 向量用户ID: {}",
                        vector.getVectorId(), currentUserId, vectorUserId);
                return true;
            }
            log.debug("知识检索：文件向量通过包含检查，向量ID: {}", vector.getVectorId());
            return false;
        }
        if (isChatVector) {
            if (currentUserId == null) {
                log.debug("知识检索：跳过聊天向量，原因：当前用户ID为空，向量ID: {}", vector.getVectorId());
                return true;
            }
            UUID vectorUserId = vector.getUserId();
            boolean matchedUser = currentUserId.equals(vectorUserId);
            if (!matchedUser) {
                log.debug("知识检索：跳过聊天向量，原因：用户ID不匹配，向量ID: {}, 当前用户ID: {}, 向量用户ID: {}",
                        vector.getVectorId(), currentUserId, vectorUserId);
                return true;
            }
            log.debug("知识检索：聊天向量通过包含检查，向量ID: {}", vector.getVectorId());
            return false;
        }
        // 其他类型（章节、问题、任务、论坛）直接通过
        log.debug("知识检索：非文件/聊天向量通过包含检查，向量ID: {}, 内容类型: {}", vector.getVectorId(), contentType);
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgeVector> findVectorsByFileIds(List<UUID> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            log.debug("查询文件向量数据：文件ID列表为空，返回空列表");
            return Collections.emptyList();
        }
        try {
            log.debug("开始查询文件向量数据，文件ID数量: {}, fileIds={}", fileIds.size(), fileIds);

            // 根据contentId查询向量数据（fileId作为contentId存储在KnowledgeVector中）
            List<KnowledgeVector> vectors = knowledgeVectorRepository.findByContentIdIn(fileIds);
            if (vectors == null) {
                log.debug("查询文件向量数据：未找到任何向量数据，fileIds={}", fileIds);
                return Collections.emptyList();
            }

            log.debug("查询文件向量数据：找到 {} 条向量记录，开始过滤无效状态", vectors.size());

            // 过滤掉状态为已失效的向量
            List<KnowledgeVector> validVectors = vectors.stream()
                    .filter(vector -> vector != null &&
                            (vector.getStatus() == null || vector.getStatus() == 0))
                    .toList();

            log.debug("查询文件向量数据：过滤后有效向量数量: {}, 原始数量: {}", validVectors.size(), vectors.size());
            return validVectors;
        } catch (Exception e) {
            log.error("查询文件向量数据失败, fileIds={}, 错误信息: {}", fileIds, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<KnowledgeVector> findVectorsByFileReferences(List<com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileReference> fileReferences, UUID currentUserId, UUID currentSessionId) {
        if (fileReferences == null || fileReferences.isEmpty()) {
            log.debug("查询文件向量数据：文件引用列表为空，返回空列表");
            return Collections.emptyList();
        }

        if (currentUserId == null) {
            log.warn("查询文件向量数据：当前用户ID为空，无法验证文件权限，返回空列表");
            return Collections.emptyList();
        }

        if (currentSessionId == null) {
            log.warn("查询文件向量数据：当前会话ID为空，无法验证文件权限，返回空列表");
            return Collections.emptyList();
        }

        try {
            log.debug("开始查询文件向量数据（带验证），文件引用数量: {}, 当前用户ID: {}, 当前会话ID: {}",
                    fileReferences.size(), currentUserId, currentSessionId);

            // 验证文件引用：检查sysUserId和sessionId是否匹配
            List<UUID> validFileIds = new ArrayList<>();
            int skippedCount = 0;
            for (com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileReference ref : fileReferences) {
                if (ref == null || ref.getId() == null) {
                    log.debug("查询文件向量数据：跳过空文件引用");
                    skippedCount++;
                    continue;
                }

                // 验证用户ID
                UUID refSysUserId = ref.getSysUserId();
                if (refSysUserId == null || !refSysUserId.equals(currentUserId)) {
                    log.debug("查询文件向量数据：跳过文件，原因：用户ID不匹配，文件ID: {}, 文件用户ID: {}, 当前用户ID: {}",
                            ref.getId(), refSysUserId, currentUserId);
                    skippedCount++;
                    continue;
                }

                // 验证会话ID
                UUID refSessionId = ref.getSessionId();
                if (refSessionId == null || !refSessionId.equals(currentSessionId)) {
                    log.debug("查询文件向量数据：跳过文件，原因：会话ID不匹配，文件ID: {}, 文件会话ID: {}, 当前会话ID: {}",
                            ref.getId(), refSessionId, currentSessionId);
                    skippedCount++;
                    continue;
                }

                // 通过验证，添加到有效文件ID列表
                validFileIds.add(ref.getId());
                log.debug("查询文件向量数据：文件通过验证，文件ID: {}, 文件名: {}", ref.getId(), ref.getFileName());
            }

            log.debug("查询文件向量数据：验证完成，有效文件数量: {}, 跳过数量: {}", validFileIds.size(), skippedCount);

            if (validFileIds.isEmpty()) {
                log.debug("查询文件向量数据：没有通过验证的文件，返回空列表");
                return Collections.emptyList();
            }

            // 查询通过验证的文件对应的向量数据
            return findVectorsByFileIds(validFileIds);
        } catch (Exception e) {
            log.error("查询文件向量数据失败（带验证）, fileReferences数量: {}, 当前用户ID: {}, 当前会话ID: {}, 错误信息: {}",
                    fileReferences.size(), currentUserId, currentSessionId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

}


