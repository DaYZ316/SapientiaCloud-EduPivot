package com.dayz.sapientiacloud_edupivot.celestial_hub.service.impl;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.clients.CourseClient;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo.*;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.KnowledgeConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KnowledgeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.VectorizeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.KnowledgeVector;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.KnowledgeItemVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.KnowledgeSearchVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.ContentTypeEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.KnowledgeEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.KnowledgeVectorRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.KnowledgeService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.HtmlTextUtil;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeVectorRepository knowledgeVectorRepository;
    private final CourseClient courseClient;
    private final VectorStore vectorStore;

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

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static int normalizeTopK(Integer topK) {
        if (topK == null || topK <= 0) {
            return KnowledgeConstants.DEFAULT_TOP_K;
        }
        return topK;
    }

    private static double normalizeThreshold(Double threshold) {
        if (threshold == null) {
            return KnowledgeConstants.DEFAULT_SIMILARITY_THRESHOLD;
        }
        if (threshold < 0.0) {
            return 0.0;
        }
        if (threshold > 1.0) {
            return 1.0;
        }
        return threshold;
    }

    private static KnowledgeSearchVO buildEmptySearchResult(String queryText, long startTimeMs) {
        KnowledgeSearchVO empty = new KnowledgeSearchVO();
        empty.setQuery(queryText);
        empty.setItems(new ArrayList<>());
        empty.setTotal(0);
        empty.setQueryTime(System.currentTimeMillis() - startTimeMs);
        return empty;
    }

    private static String resolveTitle(Document doc) {
        if (doc == null) {
            return KnowledgeConstants.DEFAULT_EMPTY_STRING;
        }
        Map<String, Object> metadata = doc.getMetadata();
        if (metadata != null) {
            Object titleObj = metadata.get(KnowledgeConstants.METADATA_TITLE);
            if (titleObj != null) {
                String t = String.valueOf(titleObj).trim();
                if (!t.isEmpty()) {
                    return t;
                }
            }
        }
        String content = sanitizeContent(doc.getFormattedContent());
        if (content == null || content.isEmpty()) {
            return KnowledgeConstants.DEFAULT_EMPTY_STRING;
        }
        // 取首个非空行作为标题
        String[] lines = content.split("\\R");
        String firstLine = KnowledgeConstants.DEFAULT_EMPTY_STRING;
        for (String line : lines) {
            if (line != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    firstLine = trimmed;
                    break;
                }
            }
        }
        if (firstLine.isEmpty()) {
            return KnowledgeConstants.DEFAULT_EMPTY_STRING;
        }
        return firstLine.length() > 120 ? firstLine.substring(0, 120) : firstLine;
    }

    private static String sanitizeContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        String[] lines = content.split("\\R");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (line == null) {
                continue;
            }
            String trimmed = line.trim();
            // 过滤向量检索附带的调试分数行
            String lower = trimmed.toLowerCase();
            if (lower.startsWith("distance:") || lower.startsWith("vector_score:")) {
                continue;
            }
            sb.append(line).append("\n");
        }
        return sb.toString().trim();
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
            sb.append("\n选项：\n");
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
            sb.append("\n答案：\n");
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
                        sb.append("第").append(answer.getSortOrder()).append("空：");
                    }
                    sb.append(plainAnswerContent).append("\n");
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
        if (str.isEmpty() || KnowledgeConstants.DEFAULT_EMPTY_STRING.equals(str)) {
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
            log.warn("Failed to parse UUID from value: {}", str);
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
            log.warn("Failed to parse Integer from value: {}", str);
            return null;
        }
    }

    /**
     * 安全解析Double，处理null、空字符串和异常情况
     */
    private static Double safeParseDouble(Object value) {
        if (value == null) {
            return null;
        }
        // 直接是Double类型，直接返回
        if (value instanceof Double) {
            return (Double) value;
        }
        // 是其他Number类型，直接转换
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        // 字符串类型，需要解析
        String str = safeToString(value);
        if (str == null) {
            return null;
        }
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse Double from value: {}", str);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void vectorizeCourseContent(VectorizeRequestDTO request) {
        try {
            if (!ContentTypeEnum.isValidCode(request.getContentType())) {
                throw new BusinessException(KnowledgeEnum.VECTORIZE_FAILED);
            }
            long start = System.currentTimeMillis();
            List<Document> documents = fetchContentForVectorization(request);

            if (documents.isEmpty()) {
                log.warn("Vectorize skipped: no documents prepared for contentType='{}', courseId={}",
                        request.getContentType(), request.getCourseId());
                return;
            }

            log.info("Vectorizing documents: count={}, contentType='{}', courseId={}",
                    documents.size(), request.getContentType(), request.getCourseId());

            // 分批处理文档，避免超过 DashScope API 的批次限制（25个）
            int batchSize = KnowledgeConstants.EMBEDDING_BATCH_SIZE;
            int totalBatches = (documents.size() + batchSize - 1) / batchSize;
            log.debug("Processing documents in {} batches (batchSize={})", totalBatches, batchSize);

            // 批量保存向量元数据，避免N+1问题
            List<KnowledgeVector> vectors = new ArrayList<>();

            for (int i = 0; i < documents.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, documents.size());
                List<Document> batch = documents.subList(i, endIndex);
                int batchNumber = (i / batchSize) + 1;

                log.debug("Processing batch {}/{}: {} documents", batchNumber, totalBatches, batch.size());

                try {
                    vectorStore.add(batch);

                    // 为当前批次构建向量元数据
                    for (Document doc : batch) {
                        KnowledgeVector vector = buildKnowledgeVector(doc);
                        if (vector != null) {
                            vectors.add(vector);
                        }
                    }

                    log.debug("Batch {}/{} completed successfully", batchNumber, totalBatches);
                } catch (Exception e) {
                    log.error("Failed to process batch {}/{}: {}", batchNumber, totalBatches, e.getMessage(), e);
                    throw e;
                }
            }

            // 批量保存所有向量元数据
            if (!vectors.isEmpty()) {
                knowledgeVectorRepository.saveAll(vectors);
                log.debug("Batch saved vector metadata: count={}", vectors.size());
            }
            long cost = System.currentTimeMillis() - start;
            log.debug("Vectorize completed: totalDocs={}, timeMs={}", documents.size(), cost);
        } catch (Exception e) {
            log.error("Vectorize failed: contentType='{}', courseId={}, error={}",
                    request.getContentType(), request.getCourseId(), e.getMessage(), e);
            throw new BusinessException(KnowledgeEnum.VECTORIZE_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void vectorizeChatContent(String userQuery, String aiResponse, UUID sessionId, UUID messageId, UUID courseId, UUID userId) {
        try {
            // 验证输入参数
            if (userQuery == null || userQuery.trim().isEmpty()) {
                log.warn("Vectorize chat content skipped: userQuery is empty, sessionId={}, messageId={}", sessionId, messageId);
                return;
            }
            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                log.warn("Vectorize chat content skipped: aiResponse is empty, sessionId={}, messageId={}", sessionId, messageId);
                return;
            }
            if (userId == null) {
                log.warn("Vectorize chat content skipped: userId is null, sessionId={}, messageId={}", sessionId, messageId);
                return;
            }

            // 按照 Q&A 对格式构建内容
            String content = "User: " + userQuery + "\nAI: " + aiResponse;

            // 构建元数据
            Map<String, Object> metadata = buildBaseMetadata(
                    ContentTypeEnum.CHAT.getCode(),
                    messageId != null ? messageId.toString() : null,
                    courseId != null ? courseId.toString() : null,
                    null,
                    null
            );

            // 添加会话ID和消息ID到元数据
            if (sessionId != null) {
                metadata.put(KnowledgeConstants.METADATA_CHAT_SESSION_ID, sessionId.toString());
            }
            if (messageId != null) {
                metadata.put(KnowledgeConstants.METADATA_CHAT_MESSAGE_ID, messageId.toString());
            }
            // 添加用户ID到元数据，用于用户隔离
            metadata.put(KnowledgeConstants.METADATA_USER_ID, userId.toString());

            // 创建 Document
            Document document = new Document(content, metadata);

            // 向量化并保存
            vectorStore.add(List.of(document));

            // 保存向量元数据到数据库
            KnowledgeVector vector = buildKnowledgeVector(document);
            if (vector != null) {
                knowledgeVectorRepository.save(vector);
                log.debug("Vectorized chat content: sessionId={}, messageId={}, contentLength={}",
                        sessionId, messageId, content.length());
            }
        } catch (Exception e) {
            log.error("Vectorize chat content failed: sessionId={}, messageId={}, error={}",
                    sessionId, messageId, e.getMessage(), e);
            // 不抛出异常，避免影响聊天流程
        }
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeSearchVO searchKnowledge(KnowledgeRequestDTO query) {
        long startTime = System.currentTimeMillis();

        // 参数矫正：query 文本、topK 与阈值
        String queryText = query.getQuery();
        if (isBlank(queryText)) {
            return buildEmptySearchResult(queryText, startTime);
        }
        int topK = normalizeTopK(query.getTopK());
        double threshold = normalizeThreshold(query.getSimilarityThreshold());

        SearchRequest searchRequest = SearchRequest.builder()
                .query(queryText)
                .topK(topK)
                .similarityThreshold(threshold)
                .build();

        List<Document> results = vectorStore.similaritySearch(searchRequest);

        List<KnowledgeItemVO> items = new ArrayList<>();
        if (results != null && !results.isEmpty()) {
            // 批量查询数据库，避免N+1问题
            List<String> vectorIds = results.stream()
                    .map(Document::getId)
                    .filter(id -> id != null && !id.isEmpty())
                    .toList();
            Map<String, KnowledgeVector> vectorMap = new HashMap<>();
            if (!vectorIds.isEmpty()) {
                try {
                    List<KnowledgeVector> vectors = knowledgeVectorRepository.findByVectorIdIn(vectorIds);
                    for (KnowledgeVector vector : vectors) {
                        if (vector != null && vector.getVectorId() != null) {
                            vectorMap.put(vector.getVectorId(), vector);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to batch query KnowledgeVector by vectorIds, error={}", e.getMessage());
                }
            }

            // 获取当前用户ID，用于过滤CHAT类型内容
            UUID currentUserId = null;
            try {
                currentUserId = UserContextUtil.getCurrentUserId();
            } catch (Exception e) {
                log.debug("无法获取当前用户ID，将不过滤CHAT类型内容: {}", e.getMessage());
            }

            // 构建结果列表，并对CHAT类型内容进行用户隔离
            for (Document doc : results) {
                // 如果是CHAT类型内容，需要检查用户ID是否匹配
                Map<String, Object> metadata = doc.getMetadata();
                if (metadata != null) {
                    Object contentTypeObj = metadata.get(KnowledgeConstants.METADATA_CONTENT_TYPE);
                    if (Objects.equals(ContentTypeEnum.CHAT.getCode(), safeParseInteger(contentTypeObj))) {
                        // CHAT类型内容必须进行用户隔离
                        if (currentUserId == null) {
                            // 无法获取用户ID，跳过CHAT类型内容
                            log.debug("跳过CHAT类型内容：无法获取当前用户ID");
                            continue;
                        }
                        Object userIdObj = metadata.get(KnowledgeConstants.METADATA_USER_ID);
                        UUID docUserId = safeParseUUID(userIdObj);
                        if (docUserId == null || !docUserId.equals(currentUserId)) {
                            // 用户ID不匹配，跳过该结果
                            log.debug("跳过CHAT类型内容：用户ID不匹配，docUserId={}, currentUserId={}", docUserId, currentUserId);
                            continue;
                        }
                    }
                }

                KnowledgeItemVO item = buildKnowledgeItem(doc, vectorMap);
                items.add(item);
            }
        }

        long queryTime = System.currentTimeMillis() - startTime;

        // RAG 命中/未命中日志
        int hitCount = items.size();
        if (hitCount > 0) {
            // 汇总前3个分数用于观察
            StringBuilder topScores = new StringBuilder();
            int limit = Math.min(3, hitCount);
            for (int i = 0; i < limit; i++) {
                Double score = items.get(i).getScore();
                if (i > 0) {
                    topScores.append(", ");
                }
                topScores.append(score == null ? "null" : String.format("%.4f", score));
            }
            log.info("RAG HIT: query='{}', topK={}, threshold={}, hits={}, timeMs={}, topScores=[{}]",
                    queryText, topK, threshold, hitCount, queryTime, topScores);
        } else {
            log.info("RAG MISS: query='{}', topK={}, threshold={}, hits=0, timeMs={}",
                    queryText, topK, threshold, queryTime);
        }

        KnowledgeSearchVO searchVO = new KnowledgeSearchVO();
        searchVO.setQuery(queryText);
        searchVO.setItems(items);
        searchVO.setTotal(items.size());
        searchVO.setQueryTime(queryTime);
        return searchVO;
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

            List<String> chunks = HtmlTextUtil.chunkText(enriched, 1000, 150);
            if (chunks.isEmpty()) {
                log.warn("Chapter produces no chunks: chapterId={}, title='{}', rawLen={}, plainLen={}",
                        id, title, rawHtml.length(), plain.length());
            } else {
                log.debug("Chapter chunked: chapterId={}, title='{}', rawLen={}, plainLen={}, chunkCount={}, chunkSize={}, overlap={}",
                        id, title, rawHtml.length(), plain.length(), chunks.size(), 1000, 150);
            }
            int chunkIndex = 0;
            for (String chunk : chunks) {
                if (chunk == null || chunk.trim().isEmpty()) {
                    continue; // 跳过空chunk
                }
                Map<String, Object> metadata = buildBaseMetadata(
                        ContentTypeEnum.CHAPTER.getCode(), id, courseId, id, title);
                metadata.put("chunk_index", chunkIndex++);
                documents.add(new Document(chunk, metadata));
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
                if (q == null || q.getId() == null) {
                    continue;
                }
                String content = buildQuestionText(q);
                if (content == null || content.trim().isEmpty()) {
                    continue; // 跳过空内容
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
                        metadata.put("tags", filteredTags);
                    }
                }

                documents.add(new Document(content, metadata));
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

            for (CourseQuestionBankVO bank : bankResult.getData()) {
                if (bank == null || bank.getId() == null) {
                    continue;
                }
                Result<List<QuestionVO>> questionsResult = courseClient.listQuestionsByBankId(bank.getId());
                if (questionsResult == null || !questionsResult.isSuccess() || questionsResult.getData() == null) {
                    continue;
                }
                for (QuestionVO q : questionsResult.getData()) {
                    if (q == null || q.getId() == null) {
                        continue;
                    }
                    String content = buildQuestionText(q);
                    if (content == null || content.trim().isEmpty()) {
                        continue; // 跳过空内容
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
                            metadata.put("tags", filteredTags);
                        }
                    }

                    documents.add(new Document(content, metadata));
                }
            }
        }
        log.debug("Prepared question documents: count={}, courseId={}", documents.size(), request.getCourseId());
        return documents;
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
            if (content == null || content.trim().isEmpty()) {
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

            documents.add(new Document(content, metadata));
        }
        log.debug("Prepared task documents: count={}, courseId={}", documents.size(), request.getCourseId());
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

            if (content == null || content.trim().isEmpty()) {
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
                    metadata.put("tags", filteredTags);
                }
            }

            documents.add(new Document(content, metadata));
        }
        log.debug("Prepared forum documents: count={}, courseId={}", documents.size(), request.getCourseId());
        return documents;
    }

    /**
     * 构建KnowledgeVector对象（用于批量保存）
     */
    private KnowledgeVector buildKnowledgeVector(Document doc) {
        if (doc == null || doc.getId() == null) {
            return null;
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

        KnowledgeVector vector = new KnowledgeVector();
        vector.setId(UuidCreator.getTimeOrderedEpoch());
        vector.setVectorId(doc.getId());
        vector.setCourseId(safeParseUUID(courseIdObj));
        vector.setChapterId(safeParseUUID(chapterIdObj));
        vector.setContentType(safeParseInteger(contentTypeObj));
        vector.setContentId(safeParseUUID(contentIdObj));
        vector.setQuestionBankId(safeParseUUID(questionBankIdObj));
        vector.setQuestionId(safeParseUUID(questionIdObj));
        vector.setTaskId(safeParseUUID(taskIdObj));
        vector.setForumId(safeParseUUID(forumIdObj));
        vector.setPostId(safeParseUUID(postIdObj));
        Object titleObj = metadata.get(KnowledgeConstants.METADATA_TITLE);
        vector.setTitle(titleObj == null ? KnowledgeConstants.DEFAULT_EMPTY_STRING : String.valueOf(titleObj));
        vector.setContent(doc.getFormattedContent());
        vector.setEmbeddingModel(KnowledgeConstants.EMBEDDING_MODEL_TEXT_V1);
        vector.setMetadata(metadata);
        // 从metadata中提取tags并保存
        Object tagsObj = metadata.get("tags");
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

    /**
     * 构建知识项VO，从metadata和数据库补充完整信息
     *
     * @param doc       文档
     * @param vectorMap 向量数据映射（vectorId -> KnowledgeVector），用于批量查询优化
     */
    private KnowledgeItemVO buildKnowledgeItem(Document doc, Map<String, KnowledgeVector> vectorMap) {
        KnowledgeItemVO item = new KnowledgeItemVO();
        String vectorId = doc.getId();

        // 从metadata中提取信息
        Map<String, Object> metadata = doc.getMetadata();
        Object courseIdObj = metadata.get(KnowledgeConstants.METADATA_COURSE_ID);
        Object chapterIdObj = metadata.get(KnowledgeConstants.METADATA_CHAPTER_ID);
        Object contentTypeObj = metadata.get(KnowledgeConstants.METADATA_CONTENT_TYPE);
        Object contentIdObj = metadata.get(KnowledgeConstants.METADATA_CONTENT_ID);
        Object questionBankIdObj = metadata.get(KnowledgeConstants.METADATA_QUESTION_BANK_ID);
        Object questionIdObj = metadata.get(KnowledgeConstants.METADATA_QUESTION_ID);
        Object taskIdObj = metadata.get(KnowledgeConstants.METADATA_TASK_ID);
        Object forumIdObj = metadata.get(KnowledgeConstants.METADATA_FORUM_ID);
        Object postIdObj = metadata.get(KnowledgeConstants.METADATA_POST_ID);

        // 从批量查询的映射中获取向量数据
        KnowledgeVector vector = vectorMap != null ? vectorMap.get(vectorId) : null;

        // 设置ID（优先从metadata的contentId，其次从数据库，最后尝试vectorId）
        UUID contentId = safeParseUUID(contentIdObj);
        if (contentId == null && vector != null && vector.getContentId() != null) {
            contentId = vector.getContentId();
        }
        if (contentId == null) {
            try {
                contentId = UUID.fromString(vectorId);
            } catch (Exception e) {
                log.warn("Failed to parse vectorId as UUID: {}", vectorId);
            }
        }
        item.setId(contentId);

        // 设置contentType（优先从metadata，其次从数据库）
        Integer contentType = safeParseInteger(contentTypeObj);
        if (contentType == null && vector != null) {
            contentType = vector.getContentType();
        }
        item.setContentType(contentType);

        // 设置title（优先从metadata，其次从数据库）
        String title = resolveTitle(doc);
        if ((title == null || title.isEmpty()) && vector != null && vector.getTitle() != null) {
            title = vector.getTitle();
        }
        item.setTitle(title);

        // 设置content
        item.setContent(sanitizeContent(doc.getFormattedContent()));

        // 设置score
        Object scoreObj = metadata.get(KnowledgeConstants.METADATA_SCORE);
        Double score = safeParseDouble(scoreObj);
        if (score == null) {
            // 尝试从metadata中的distance或vector_score获取
            Object distanceObj = metadata.get("distance");
            Object vectorScoreObj = metadata.get("vector_score");
            if (distanceObj != null) {
                score = safeParseDouble(distanceObj);
            } else if (vectorScoreObj != null) {
                score = safeParseDouble(vectorScoreObj);
            }
        }
        item.setScore(score != null ? score : KnowledgeConstants.DEFAULT_SCORE);

        // 设置courseId（优先从metadata，其次从数据库）
        UUID courseId = safeParseUUID(courseIdObj);
        if (courseId == null && vector != null) {
            courseId = vector.getCourseId();
        }
        item.setCourseId(courseId);

        // 设置chapterId（优先从metadata，其次从数据库）
        UUID chapterId = safeParseUUID(chapterIdObj);
        if (chapterId == null && vector != null) {
            chapterId = vector.getChapterId();
        }
        item.setChapterId(chapterId);

        // 设置questionBankId（优先从metadata，其次从数据库）
        UUID questionBankId = safeParseUUID(questionBankIdObj);
        if (questionBankId == null && vector != null) {
            questionBankId = vector.getQuestionBankId();
        }
        item.setQuestionBankId(questionBankId);

        // 设置questionId（优先从metadata，其次从数据库）
        UUID questionId = safeParseUUID(questionIdObj);
        if (questionId == null && vector != null) {
            questionId = vector.getQuestionId();
        }
        item.setQuestionId(questionId);

        // 设置taskId（优先从metadata，其次从数据库）
        UUID taskId = safeParseUUID(taskIdObj);
        if (taskId == null && vector != null) {
            taskId = vector.getTaskId();
        }
        item.setTaskId(taskId);

        // 设置forumId（优先从metadata，其次从数据库）
        UUID forumId = safeParseUUID(forumIdObj);
        if (forumId == null && vector != null) {
            forumId = vector.getForumId();
        }
        item.setForumId(forumId);

        // 设置postId（优先从metadata，其次从数据库）
        UUID postId = safeParseUUID(postIdObj);
        if (postId == null && vector != null) {
            postId = vector.getPostId();
        }
        item.setPostId(postId);

        // 设置tags（优先从metadata，其次从数据库）
        Object tagsObj = metadata.get("tags");
        if (tagsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) tagsObj;
            if (tags != null && !tags.isEmpty()) {
                item.setTags(tags);
            }
        }
        if (item.getTags() == null && vector != null && vector.getTags() != null && !vector.getTags().isEmpty()) {
            item.setTags(vector.getTags());
        }

        // 设置metadata
        item.setMetadata(metadata);

        return item;
    }
}

