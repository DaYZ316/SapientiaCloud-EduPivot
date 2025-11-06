package com.dayz.sapientiacloud_edupivot.celestial_hub.repository;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.KnowledgeVector;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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
     * 根据课程ID删除向量
     */
    void deleteByCourseId(UUID courseId);

    /**
     * 统计课程的向量数量
     */
    long countByCourseId(UUID courseId);
}

