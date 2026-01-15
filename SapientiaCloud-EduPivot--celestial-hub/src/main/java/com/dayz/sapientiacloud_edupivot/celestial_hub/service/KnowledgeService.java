package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileReference;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KnowledgeSearchRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.VectorizeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.KnowledgeVector;
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

    /**
     * 根据文件ID列表查询对应的向量数据（通过contentId索引）
     *
     * @param fileIds 文件ID列表
     * @return 向量数据列表
     */
    List<KnowledgeVector> findVectorsByFileIds(List<UUID> fileIds);

    /**
     * 根据文件引用列表查询对应的向量数据（通过contentId索引），并验证用户ID和会话ID
     *
     * @param fileReferences   文件引用列表
     * @param currentUserId    当前用户ID
     * @param currentSessionId 当前会话ID
     * @return 向量数据列表（仅返回通过验证的文件对应的向量）
     */
    List<KnowledgeVector> findVectorsByFileReferences(List<FileReference> fileReferences, UUID currentUserId, UUID currentSessionId);
}

