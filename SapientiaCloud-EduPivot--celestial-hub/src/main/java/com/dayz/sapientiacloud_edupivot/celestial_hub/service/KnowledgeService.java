package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KnowledgeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.VectorizeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.KnowledgeSearchVO;

import java.util.List;
import java.util.UUID;

public interface KnowledgeService {

    void vectorizeCourseContent(VectorizeRequestDTO request);

    void vectorizeChatContent(String userQuery, String aiResponse, UUID sessionId, UUID messageId, UUID courseId, UUID userId);

    KnowledgeSearchVO searchKnowledge(KnowledgeRequestDTO query);

    void deleteCourseVectors(UUID courseId);

    void deleteChapterVectors(UUID chapterId);

    Long getCourseVectorCount(UUID courseId);

    /**
     * 向量化文件内容
     *
     * @param fileId 文件ID
     */
    void vectorizeFileDocument(UUID fileId);

    /**
     * 根据文件ID列表检索文件内容
     *
     * @param fileIds 文件ID列表
     * @param query   查询文本
     * @param topK    返回数量
     * @return 检索到的文件内容片段
     */
    String retrieveFileContext(List<UUID> fileIds, String query, Integer topK);
}

