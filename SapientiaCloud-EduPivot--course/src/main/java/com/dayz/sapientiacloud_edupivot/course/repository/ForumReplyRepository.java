package com.dayz.sapientiacloud_edupivot.course.repository;

import com.dayz.sapientiacloud_edupivot.course.entity.po.ForumReply;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ForumReplyRepository extends MongoRepository<ForumReply, UUID> {

    List<ForumReply> findByPostIdOrderByCreateTimeAsc(UUID postId);

    List<ForumReply> findByForumIdOrderByCreateTimeDesc(UUID forumId);

    List<ForumReply> findByCourseIdOrderByCreateTimeDesc(UUID courseId);

    List<ForumReply> findBySysUserIdOrderByCreateTimeDesc(UUID sysUserId);

    List<ForumReply> findByParentReplyIdOrderByCreateTimeAsc(UUID parentReplyId);

    List<ForumReply> findByPostIdAndStatusOrderByCreateTimeAsc(UUID postId, Integer status);

    List<ForumReply> findByForumIdAndStatusOrderByCreateTimeDesc(UUID forumId, Integer status);

    List<ForumReply> findByCourseIdAndStatusOrderByCreateTimeDesc(UUID courseId, Integer status);

    List<ForumReply> findByPostIdAndParentReplyIdOrderByCreateTimeAsc(UUID postId, UUID parentReplyId);

    List<ForumReply> findByPostIdAndParentReplyIdIsNullOrderByCreateTimeAsc(UUID postId);

    List<ForumReply> findByPostIdAndIsAcceptedOrderByCreateTimeAsc(UUID postId, Integer isAccepted);

    long countByPostId(UUID postId);

    long countByForumId(UUID forumId);

    long countByCourseId(UUID courseId);

    long countBySysUserId(UUID sysUserId);

    long countByParentReplyId(UUID parentReplyId);

    long countByPostIdAndStatus(UUID postId, Integer status);

    long countByForumIdAndStatus(UUID forumId, Integer status);

    long countByCourseIdAndStatus(UUID courseId, Integer status);

    void deleteByPostId(UUID postId);

    void deleteByForumId(UUID forumId);

    void deleteByCourseId(UUID courseId);

    void deleteByPostIdAndStatus(UUID postId, Integer status);

    void deleteByForumIdAndStatus(UUID forumId, Integer status);

    void deleteByCourseIdAndStatus(UUID courseId, Integer status);

    @Query(value = "{'post_id': ?0}", sort = "{'floor_number': -1}")
    Optional<ForumReply> findTopByPostIdOrderByFloorNumberDesc(UUID postId);
}
