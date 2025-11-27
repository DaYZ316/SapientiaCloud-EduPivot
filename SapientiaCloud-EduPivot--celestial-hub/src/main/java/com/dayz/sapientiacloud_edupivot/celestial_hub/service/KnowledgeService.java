package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KnowledgeSearchRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.VectorizeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.KnowledgeSearchResultVO;

import java.util.List;
import java.util.UUID;

public interface KnowledgeService {

    void vectorizeCourseContent(VectorizeRequestDTO request);

    void vectorizeChatContent(String userQuery, String aiResponse, UUID sessionId, UUID messageId, UUID courseId, UUID userId);

    void deleteCourseVectors(UUID courseId);

    void deleteChapterVectors(UUID chapterId);

    Long getCourseVectorCount(UUID courseId);

    void vectorizeFileDocument(UUID fileId);

    String retrieveFileContext(List<UUID> fileIds, String query, Integer topK);

    List<KnowledgeSearchResultVO> searchKnowledge(KnowledgeSearchRequestDTO request);
}

