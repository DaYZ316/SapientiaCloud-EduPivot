package com.dayz.sapientiacloud_edupivot.course.repository;

import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseForum;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseForumRepository extends MongoRepository<CourseForum, UUID> {

    @Query("{'course_id': ?0, 'forum_name': {$regex: ?1, $options: 'i'}}")
    List<CourseForum> findByCourseIdAndForumNameContainingIgnoreCase(UUID courseId, String forumName);

    Optional<CourseForum> findByCourseIdAndForumName(UUID courseId, String forumName);

    boolean existsByCourseIdAndForumName(UUID courseId, String forumName);

    long countByCourseId(UUID courseId);

    long countByCourseIdAndStatus(UUID courseId, Integer status);

    long countByCourseIdAndForumType(UUID courseId, Integer forumType);

    void deleteByCourseId(UUID courseId);

    void deleteByCourseIdAndStatus(UUID courseId, Integer status);
}
