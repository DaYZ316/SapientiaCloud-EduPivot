package com.dayz.sapientiacloud_edupivot.celestial_hub.service.impl;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.KnowledgeConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KnowledgeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.VectorizeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.KnowledgeVector;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.KnowledgeItemVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.KnowledgeSearchVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.KnowledgeEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.KnowledgeVectorRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.KnowledgeService;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private final KnowledgeVectorRepository knowledgeVectorRepository;
    private final MongoTemplate mongoTemplate;
    private final VectorStore vectorStore;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void vectorizeCourseContent(VectorizeRequestDTO request) {
        try {
            List<Document> documents = fetchContentForVectorization(request);

            if (documents.isEmpty()) {
                return;
            }

            vectorStore.add(documents);

            for (Document doc : documents) {
                saveVectorMetadata(doc, request);
            }
        } catch (Exception e) {
            throw new BusinessException(KnowledgeEnum.VECTORIZE_FAILED);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public KnowledgeSearchVO searchKnowledge(KnowledgeRequestDTO query) {
        long startTime = System.currentTimeMillis();

        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query.getQuery())
                    .topK(query.getTopK() != null ? query.getTopK() : KnowledgeConstants.DEFAULT_TOP_K)
                    .similarityThreshold(query.getSimilarityThreshold() != null ?
                            query.getSimilarityThreshold() : KnowledgeConstants.DEFAULT_SIMILARITY_THRESHOLD)
                    .build();

            List<Document> results = vectorStore.similaritySearch(searchRequest);

            List<KnowledgeItemVO> items = new ArrayList<>();
            if (results != null) {
                for (Document doc : results) {
                    Object courseIdObj = doc.getMetadata().get(KnowledgeConstants.METADATA_COURSE_ID);
                    Object chapterIdObj = doc.getMetadata().get(KnowledgeConstants.METADATA_CHAPTER_ID);
                    Object contentTypeObj = doc.getMetadata().get(KnowledgeConstants.METADATA_CONTENT_TYPE);

                    KnowledgeItemVO item = new KnowledgeItemVO();
                    item.setId(UUID.fromString(doc.getId()));
                    item.setContentType(contentTypeObj != null ? Integer.parseInt(contentTypeObj.toString()) : null);
                    item.setTitle(doc.getMetadata().getOrDefault(KnowledgeConstants.METADATA_TITLE,
                            KnowledgeConstants.DEFAULT_EMPTY_STRING).toString());
                    item.setContent(doc.getFormattedContent());
                    item.setScore((Double) doc.getMetadata().getOrDefault(KnowledgeConstants.METADATA_SCORE,
                            KnowledgeConstants.DEFAULT_SCORE));
                    item.setCourseId(courseIdObj != null ? UUID.fromString(courseIdObj.toString()) : null);
                    item.setChapterId(chapterIdObj != null ? UUID.fromString(chapterIdObj.toString()) : null);
                    item.setMetadata(doc.getMetadata());
                    items.add(item);
                }
            }

            long queryTime = System.currentTimeMillis() - startTime;

            KnowledgeSearchVO searchVO = new KnowledgeSearchVO();
            searchVO.setQuery(query.getQuery());
            searchVO.setItems(items);
            searchVO.setTotal(items.size());
            searchVO.setQueryTime(queryTime);
            return searchVO;

        } catch (Exception e) {
            throw new BusinessException(KnowledgeEnum.SEARCH_FAILED);
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

    // TODO: 实现章节内容的向量化
    private List<Document> fetchChapterContent(VectorizeRequestDTO request) {
        return new ArrayList<>();
    }

    // TODO: 实现问题内容的向量化
    private List<Document> fetchQuestionContent(VectorizeRequestDTO request) {
        return new ArrayList<>();
    }

    // TODO: 实现答案内容的向量化
    private List<Document> fetchAnswerContent(VectorizeRequestDTO request) {
        return new ArrayList<>();
    }

    // TODO: 实现论坛内容的向量化
    private List<Document> fetchForumContent(VectorizeRequestDTO request) {
        return new ArrayList<>();
    }

    private void saveVectorMetadata(Document doc, VectorizeRequestDTO request) {
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
        vector.setTitle(doc.getMetadata().getOrDefault(KnowledgeConstants.METADATA_TITLE,
                KnowledgeConstants.DEFAULT_EMPTY_STRING).toString());
        vector.setContent(doc.getFormattedContent());
        vector.setEmbeddingModel(KnowledgeConstants.EMBEDDING_MODEL_TEXT_V1);
        vector.setMetadata(doc.getMetadata());
        vector.setStatus(StatusEnum.NORMAL.getCode());
        vector.setCreateTime(LocalDateTime.now());
        vector.setUpdateTime(LocalDateTime.now());

        knowledgeVectorRepository.save(vector);
    }
}

