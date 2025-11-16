package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KnowledgeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.VectorizeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.KnowledgeSearchVO;

import java.util.UUID;

public interface KnowledgeService {

    void vectorizeCourseContent(VectorizeRequestDTO request);

    void vectorizeChatContent(String userQuery, String aiResponse, UUID sessionId, UUID messageId, UUID courseId, UUID userId);

    KnowledgeSearchVO searchKnowledge(KnowledgeRequestDTO query);

    void deleteCourseVectors(UUID courseId);

    void deleteChapterVectors(UUID chapterId);

    Long getCourseVectorCount(UUID courseId);
}

