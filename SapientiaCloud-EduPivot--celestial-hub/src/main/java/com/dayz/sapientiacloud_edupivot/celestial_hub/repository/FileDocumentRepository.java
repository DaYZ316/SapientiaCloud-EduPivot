package com.dayz.sapientiacloud_edupivot.celestial_hub.repository;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.FileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileDocumentRepository extends MongoRepository<FileDocument, UUID> {

    @Query("{'sessionId': ?0, 'status': {$ne: 1}}")
    List<FileDocument> findActiveBySessionId(UUID sessionId);

    @Query("{'id': {$in: ?0}, 'status': {$ne: 1}}")
    List<FileDocument> findActiveByIds(List<UUID> ids);
}

