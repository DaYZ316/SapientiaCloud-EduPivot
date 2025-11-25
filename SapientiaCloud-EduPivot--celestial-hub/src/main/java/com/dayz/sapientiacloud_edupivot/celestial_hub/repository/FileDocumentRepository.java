package com.dayz.sapientiacloud_edupivot.celestial_hub.repository;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.FileDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileDocumentRepository extends MongoRepository<FileDocument, UUID> {

    List<FileDocument> findBySessionId(UUID sessionId);

    List<FileDocument> findBySessionIdAndStatus(UUID sessionId, Integer status);

    List<FileDocument> findByCourseId(UUID courseId);

    List<FileDocument> findByCourseIdAndStatus(UUID courseId, Integer status);

    List<FileDocument> findBySysUserId(UUID sysUserId);

    List<FileDocument> findBySysUserIdAndStatus(UUID sysUserId, Integer status);

    @Query("{'sessionId': ?0, 'status': {$ne: 1}}")
    List<FileDocument> findActiveBySessionId(UUID sessionId);

    @Query("{'courseId': ?0, 'status': {$ne: 1}}")
    List<FileDocument> findActiveByCourseId(UUID courseId);

    @Query("{'id': {$in: ?0}, 'status': {$ne: 1}}")
    List<FileDocument> findActiveByIds(List<UUID> ids);

    Page<FileDocument> findBySysUserIdAndStatus(UUID sysUserId, Integer status, Pageable pageable);
}

