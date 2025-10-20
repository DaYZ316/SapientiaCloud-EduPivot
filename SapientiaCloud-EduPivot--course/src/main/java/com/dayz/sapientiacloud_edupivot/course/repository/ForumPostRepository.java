package com.dayz.sapientiacloud_edupivot.course.repository;

import com.dayz.sapientiacloud_edupivot.course.entity.po.ForumPost;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ForumPostRepository extends MongoRepository<ForumPost, UUID> {


    List<ForumPost> findByForumIdOrderByCreateTimeDesc(UUID forumId);

    List<ForumPost> findByCourseIdOrderByCreateTimeDesc(UUID courseId);

    List<ForumPost> findBySysUserIdOrderByCreateTimeDesc(UUID sysUserId);

    List<ForumPost> findByForumIdAndStatusOrderByCreateTimeDesc(UUID forumId, Integer status);

    List<ForumPost> findByCourseIdAndStatusOrderByCreateTimeDesc(UUID courseId, Integer status);

    List<ForumPost> findByForumIdAndIsTopOrderByCreateTimeDesc(UUID forumId, Integer isTop);

    List<ForumPost> findByForumIdAndIsEssenceOrderByCreateTimeDesc(UUID forumId, Integer isEssence);

    @Query("{'forum_id': ?0, 'title': {$regex: ?1, $options: 'i'}}")
    List<ForumPost> findByForumIdAndTitleContainingIgnoreCase(UUID forumId, String title);

    @Query("{'course_id': ?0, 'title': {$regex: ?1, $options: 'i'}}")
    List<ForumPost> findByCourseIdAndTitleContainingIgnoreCase(UUID courseId, String title);

    List<ForumPost> findByChapterIdOrderByCreateTimeDesc(UUID chapterId);

    long countByForumId(UUID forumId);

    long countByCourseId(UUID courseId);

    long countBySysUserId(UUID sysUserId);

    long countByForumIdAndStatus(UUID forumId, Integer status);

    long countByCourseIdAndStatus(UUID courseId, Integer status);

    void deleteByForumId(UUID forumId);

    void deleteByCourseId(UUID courseId);

    void deleteByForumIdAndStatus(UUID forumId, Integer status);

    void deleteByCourseIdAndStatus(UUID courseId, Integer status);

    @Query(value = "{}", sort = "{'like_count': -1, 'create_time': -1}")
    List<ForumPost> findTopPostsByLikeCount(int limit);

    @Query(value = "{}", sort = "{'create_time': -1}")
    List<ForumPost> findLatestPosts(int limit);
}
