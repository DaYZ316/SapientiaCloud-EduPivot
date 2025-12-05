package com.dayz.sapientiacloud_edupivot.celestial_hub.repository;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.KnowledgeVector;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 知识向量Repository
 */
@Repository
public interface KnowledgeVectorRepository extends MongoRepository<KnowledgeVector, UUID> {

    /**
     * 根据章节ID查询向量
     */
    List<KnowledgeVector> findByChapterId(UUID chapterId);

    /**
     * 根据章节ID删除向量
     */
    void deleteByChapterId(UUID chapterId);

    /**
     * 根据课程ID删除向量
     */
    void deleteByCourseId(UUID courseId);

    /**
     * 统计课程的向量数量
     */
    long countByCourseId(UUID courseId);

    /**
     * 根据向量ID查询记录（用于补充Redis缺失的元数据）
     */
    Optional<KnowledgeVector> findFirstByVectorId(String vectorId);

    /**
     * 批量根据向量ID查询记录（用于RAG检索结果）
     */
    List<KnowledgeVector> findByVectorIdIn(Collection<String> vectorIds);

    /**
     * 根据内容ID查询向量（用于文件向量索引）
     */
    List<KnowledgeVector> findByContentId(UUID contentId);

    /**
     * 批量根据内容ID查询向量（用于文件向量索引）
     */
    List<KnowledgeVector> findByContentIdIn(Collection<UUID> contentIds);

}

