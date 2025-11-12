package com.dayz.sapientiacloud_edupivot.celestial_hub.service.impl;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.clients.CourseClient;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo.CourseChapterVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo.CourseQuestionBankVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo.ForumPostVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo.QuestionAnswerVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo.QuestionVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.KnowledgeConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KnowledgeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.VectorizeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.KnowledgeVector;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.KnowledgeItemVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.KnowledgeSearchVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.KnowledgeEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.ContentTypeEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.KnowledgeVectorRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.KnowledgeService;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.HtmlTextUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeVectorRepository knowledgeVectorRepository;
    private final CourseClient courseClient;
    private final VectorStore vectorStore;

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
                log.warn("Vectorize skipped: no documents prepared for contentType='{}', courseId={}, chapterId={}",
                        request.getContentType(), request.getCourseId(), request.getChapterId());
                return;
            }

            log.info("Vectorizing documents: count={}, contentType='{}', courseId={}, chapterId={}",
                    documents.size(), request.getContentType(), request.getCourseId(), request.getChapterId());

            vectorStore.add(documents);

            for (Document doc : documents) {
                saveVectorMetadata(doc);
            }
            long cost = System.currentTimeMillis() - start;
            log.debug("Vectorize completed: totalDocs={}, timeMs={}", documents.size(), cost);
        } catch (Exception e) {
            log.error("Vectorize failed: contentType='{}', courseId={}, chapterId={}, error={}",
                    request.getContentType(), request.getCourseId(), request.getChapterId(), e.getMessage(), e);
            throw new BusinessException(KnowledgeEnum.VECTORIZE_FAILED);
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
        if (results != null) {
            for (Document doc : results) {
                // 将向量检索返回的得分透传到VO
                Object courseIdObj = doc.getMetadata().get(KnowledgeConstants.METADATA_COURSE_ID);
                Object chapterIdObj = doc.getMetadata().get(KnowledgeConstants.METADATA_CHAPTER_ID);
                Object contentTypeObj = doc.getMetadata().get(KnowledgeConstants.METADATA_CONTENT_TYPE);

                KnowledgeItemVO item = new KnowledgeItemVO();
                item.setId(UUID.fromString(doc.getId()));
                item.setContentType(contentTypeObj != null ? Integer.parseInt(contentTypeObj.toString()) : null);
                item.setTitle(resolveTitle(doc));
                item.setContent(sanitizeContent(doc.getFormattedContent()));
                item.setScore((Double) doc.getMetadata().getOrDefault(KnowledgeConstants.METADATA_SCORE,
                        KnowledgeConstants.DEFAULT_SCORE));
                item.setCourseId(courseIdObj != null ? UUID.fromString(courseIdObj.toString()) : null);
                item.setChapterId(chapterIdObj != null ? UUID.fromString(chapterIdObj.toString()) : null);
                item.setMetadata(doc.getMetadata());
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
        List<KnowledgeVector> vectors = knowledgeVectorRepository
                .findByChapterId(chapterId);

        knowledgeVectorRepository.deleteAll(vectors);
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
        documents.addAll(fetchAnswerContent(request));
        documents.addAll(fetchForumContent(request));

        return documents;
    }

    private List<Document> fetchChapterContent(VectorizeRequestDTO request) {
        if (!Integer.valueOf(ContentTypeEnum.CHAPTER.getCode()).equals(request.getContentType())) {
            return new ArrayList<>();
        }

        if (request.getCourseId() == null && request.getChapterId() == null) {
            return new ArrayList<>();
        }

        List<CourseChapterVO> chapters = new ArrayList<>();
        if (request.getChapterId() != null) {
            Result<CourseChapterVO> one = courseClient.getChapterById(request.getChapterId());
            if (one != null && one.isSuccess() && one.getData() != null) {
                chapters.add(one.getData());
            }
        } else if (request.getCourseId() != null) {
            Result<List<CourseChapterVO>> list = courseClient.listChaptersByCourseId(request.getCourseId());
            if (list != null && list.isSuccess() && list.getData() != null) {
                chapters.addAll(list.getData());
            }
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
                Map<String, Object> metadata = new HashMap<>();
                metadata.put(KnowledgeConstants.METADATA_CONTENT_TYPE, ContentTypeEnum.CHAPTER.getCode());
                metadata.put(KnowledgeConstants.METADATA_CONTENT_ID, id);
                metadata.put(KnowledgeConstants.METADATA_COURSE_ID, courseId);
                metadata.put(KnowledgeConstants.METADATA_CHAPTER_ID, id);
                metadata.put(KnowledgeConstants.METADATA_TITLE, title);
                metadata.put("chunk_index", chunkIndex++);
                documents.add(new Document(chunk, metadata));
            }
        }
        log.debug("Prepared chapter documents: count={}, courseId={}, chapterIdFilter={}",
                documents.size(),
                request.getCourseId(),
                request.getChapterId());
        return documents;
    }

    private List<Document> fetchQuestionContent(VectorizeRequestDTO request) {
        if (!Integer.valueOf(ContentTypeEnum.QUESTION.getCode()).equals(request.getContentType())) {
            return new ArrayList<>();
        }
		if (request.getCourseId() == null) {
            return new ArrayList<>();
        }

		Result<List<CourseQuestionBankVO>> bankResult = courseClient.listQuestionBanksByCourseId(request.getCourseId());
		if (bankResult == null || !bankResult.isSuccess() || bankResult.getData() == null) {
			return new ArrayList<>();
		}

		// 建立题库到课程ID的映射，便于题目元数据写入
		Map<UUID, UUID> bankIdToCourseId = new HashMap<>();
		for (CourseQuestionBankVO bank : bankResult.getData()) {
			if (bank != null && bank.getId() != null) {
				bankIdToCourseId.put(bank.getId(), bank.getCourseId());
			}
		}

		List<Document> documents = new ArrayList<>();
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

				Map<String, Object> metadata = new HashMap<>();
				metadata.put(KnowledgeConstants.METADATA_CONTENT_TYPE, ContentTypeEnum.QUESTION.getCode());
				metadata.put(KnowledgeConstants.METADATA_CONTENT_ID, q.getId().toString());
				metadata.put(KnowledgeConstants.METADATA_COURSE_ID, safeString(bankIdToCourseId.get(q.getQuestionBankId())));
				metadata.put(KnowledgeConstants.METADATA_CHAPTER_ID, null);
				metadata.put(KnowledgeConstants.METADATA_TITLE, emptyToNull(q.getQuestionTitle()));

				documents.add(new Document(content, metadata));
			}
		}
        log.debug("Prepared question documents: count={}, courseId={}", documents.size(), request.getCourseId());
		return documents;
    }

    private List<Document> fetchAnswerContent(VectorizeRequestDTO request) {
        if (!Integer.valueOf(ContentTypeEnum.ANSWER.getCode()).equals(request.getContentType())) {
            return new ArrayList<>();
        }
		if (request.getCourseId() == null) {
            return new ArrayList<>();
        }

		Result<List<CourseQuestionBankVO>> bankResult = courseClient.listQuestionBanksByCourseId(request.getCourseId());
		if (bankResult == null || !bankResult.isSuccess() || bankResult.getData() == null) {
			return new ArrayList<>();
		}

		// 建立题库到课程ID的映射
		Map<UUID, UUID> bankIdToCourseId = new HashMap<>();
		for (CourseQuestionBankVO bank : bankResult.getData()) {
			if (bank != null && bank.getId() != null) {
				bankIdToCourseId.put(bank.getId(), bank.getCourseId());
			}
		}

		List<Document> documents = new ArrayList<>();
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
				Result<List<QuestionAnswerVO>> answersResult = courseClient.listQuestionAnswersByQuestionId(q.getId());
				if (answersResult == null || !answersResult.isSuccess() || answersResult.getData() == null) {
					continue;
				}
				for (QuestionAnswerVO a : answersResult.getData()) {
					if (a == null || a.getId() == null) {
						continue;
					}
					String content = buildAnswerText(q, a);

					Map<String, Object> metadata = new HashMap<>();
					metadata.put(KnowledgeConstants.METADATA_CONTENT_TYPE, ContentTypeEnum.ANSWER.getCode());
					metadata.put(KnowledgeConstants.METADATA_CONTENT_ID, a.getId().toString());
					metadata.put(KnowledgeConstants.METADATA_COURSE_ID, safeString(bankIdToCourseId.get(q.getQuestionBankId())));
					metadata.put(KnowledgeConstants.METADATA_CHAPTER_ID, null);
					metadata.put(KnowledgeConstants.METADATA_TITLE, emptyToNull(a.getQuestionTitle()));

					documents.add(new Document(content, metadata));
				}
			}
		}
        log.debug("Prepared answer documents: count={}, courseId={}", documents.size(), request.getCourseId());
		return documents;
    }

    private List<Document> fetchForumContent(VectorizeRequestDTO request) {
        if (!Integer.valueOf(ContentTypeEnum.FORUM.getCode()).equals(request.getContentType())) {
            return new ArrayList<>();
        }
        if (request.getCourseId() == null) {
            return new ArrayList<>();
        }

        Result<List<ForumPostVO>> postResult = courseClient.listForumPostsByCourseId(request.getCourseId());
        if (postResult == null || !postResult.isSuccess() || postResult.getData() == null) {
            return new ArrayList<>();
        }

        List<Document> documents = new ArrayList<>();
        for (ForumPostVO post : postResult.getData()) {
            if (post == null) {
                continue;
            }
            // 可选标签过滤
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                List<String> tags = post.getTags();
                boolean match = false;
                if (tags != null) {
                    for (String t : request.getTags()) {
                        if (tags.contains(t)) {
                            match = true;
                            break;
                        }
                    }
                }
                if (!match) {
                    continue;
                }
            }

            String id = post.getId() != null ? post.getId().toString() : KnowledgeConstants.DEFAULT_EMPTY_STRING;
            String courseId = post.getCourseId() != null ? post.getCourseId().toString() : KnowledgeConstants.DEFAULT_EMPTY_STRING;
            String title = post.getTitle() != null ? post.getTitle() : KnowledgeConstants.DEFAULT_EMPTY_STRING;
            String content = post.getContent() != null ? post.getContent() : KnowledgeConstants.DEFAULT_EMPTY_STRING;

            Map<String, Object> metadata = new HashMap<>();
            metadata.put(KnowledgeConstants.METADATA_CONTENT_TYPE, ContentTypeEnum.FORUM.getCode());
            metadata.put(KnowledgeConstants.METADATA_CONTENT_ID, id);
            metadata.put(KnowledgeConstants.METADATA_COURSE_ID, courseId);
            metadata.put(KnowledgeConstants.METADATA_CHAPTER_ID, null);
            metadata.put(KnowledgeConstants.METADATA_TITLE, title);

            documents.add(new Document(content, metadata));
        }
        log.debug("Prepared forum documents: count={}, courseId={}", documents.size(), request.getCourseId());
        return documents;
    }

    private void saveVectorMetadata(Document doc) {
        Object courseIdObj = doc.getMetadata().get(KnowledgeConstants.METADATA_COURSE_ID);
        Object chapterIdObj = doc.getMetadata().get(KnowledgeConstants.METADATA_CHAPTER_ID);
        Object contentIdObj = doc.getMetadata().get(KnowledgeConstants.METADATA_CONTENT_ID);
        Object contentTypeObj = doc.getMetadata().get(KnowledgeConstants.METADATA_CONTENT_TYPE);

        KnowledgeVector vector = new KnowledgeVector();
        vector.setId(UuidCreator.getTimeOrderedEpoch());
        vector.setVectorId(doc.getId());
        vector.setCourseId(courseIdObj != null ? UUID.fromString(courseIdObj.toString()) : null);
        vector.setChapterId(chapterIdObj != null ? UUID.fromString(chapterIdObj.toString()) : null);
        vector.setContentType(contentTypeObj != null ? Integer.parseInt(contentTypeObj.toString()) : null);
        vector.setContentId(contentIdObj != null ? UUID.fromString(contentIdObj.toString()) : null);
        Object titleObj = doc.getMetadata().get(KnowledgeConstants.METADATA_TITLE);
        vector.setTitle(titleObj == null ? KnowledgeConstants.DEFAULT_EMPTY_STRING : String.valueOf(titleObj));
        vector.setContent(doc.getFormattedContent());
        vector.setEmbeddingModel(KnowledgeConstants.EMBEDDING_MODEL_TEXT_V1);
        vector.setMetadata(doc.getMetadata());
        vector.setStatus(StatusEnum.NORMAL.getCode());
        vector.setCreateTime(LocalDateTime.now());
        vector.setUpdateTime(LocalDateTime.now());

        knowledgeVectorRepository.save(vector);
        if (log.isDebugEnabled()) {
            log.debug("Vector metadata saved: vectorId={}, contentType={}, courseId={}, chapterId={}",
                    vector.getVectorId(), vector.getContentType(), vector.getCourseId(), vector.getChapterId());
        }
    }

    private static String safeString(Object value) {
        return value == null ? KnowledgeConstants.DEFAULT_EMPTY_STRING : String.valueOf(value);
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
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
			sb.append(q.getQuestionContent());
		}
		return sb.toString();
	}

	private static String buildAnswerText(QuestionVO q, QuestionAnswerVO a) {
		StringBuilder sb = new StringBuilder();
		// 使用题目标题作为上下文标题
		if (q != null && q.getQuestionTitle() != null && !q.getQuestionTitle().isEmpty()) {
			sb.append(q.getQuestionTitle()).append("\n");
		} else if (a.getQuestionTitle() != null && !a.getQuestionTitle().isEmpty()) {
			sb.append(a.getQuestionTitle()).append("\n");
		}
		// 答案文本
		if (a.getAnswerText() != null && !a.getAnswerText().isEmpty()) {
			sb.append(a.getAnswerText());
		} else if (a.getAnswerContent() != null && !a.getAnswerContent().isEmpty()) {
			sb.append(a.getAnswerContent());
		}
		return sb.toString();
	}
}

