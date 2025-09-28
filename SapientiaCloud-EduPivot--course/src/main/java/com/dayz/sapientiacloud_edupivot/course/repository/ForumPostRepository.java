package com.dayz.sapientiacloud_edupivot.course.repository;

import com.dayz.sapientiacloud_edupivot.course.entity.po.ForumPost;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 论坛帖子MongoDB数据访问接口
 *
 * @author SapientiaCloud
 * @since 2024-01-01
 */
@Repository
public interface ForumPostRepository extends MongoRepository<ForumPost, UUID> {

    /**
     * 根据论坛ID查询帖子列表
     *
     * @param forumId 论坛ID
     * @return 帖子列表
     */
    List<ForumPost> findByForumIdOrderByCreateTimeDesc(UUID forumId);

    /**
     * 根据课程ID查询帖子列表
     *
     * @param courseId 课程ID
     * @return 帖子列表
     */
    List<ForumPost> findByCourseIdOrderByCreateTimeDesc(UUID courseId);

    /**
     * 根据作者ID查询帖子列表
     *
     * @param sysUserId 作者ID
     * @return 帖子列表
     */
    List<ForumPost> findBySysUserIdOrderByCreateTimeDesc(UUID sysUserId);

    /**
     * 根据论坛ID和状态查询帖子列表
     *
     * @param forumId 论坛ID
     * @param status  状态
     * @return 帖子列表
     */
    List<ForumPost> findByForumIdAndStatusOrderByCreateTimeDesc(UUID forumId, Integer status);

    /**
     * 根据课程ID和状态查询帖子列表
     *
     * @param courseId 课程ID
     * @param status   状态
     * @return 帖子列表
     */
    List<ForumPost> findByCourseIdAndStatusOrderByCreateTimeDesc(UUID courseId, Integer status);

    /**
     * 根据论坛ID和置顶状态查询帖子列表
     *
     * @param forumId 论坛ID
     * @param isTop   是否置顶
     * @return 帖子列表
     */
    List<ForumPost> findByForumIdAndIsTopOrderByCreateTimeDesc(UUID forumId, Integer isTop);

    /**
     * 根据论坛ID和精华状态查询帖子列表
     *
     * @param forumId   论坛ID
     * @param isEssence 是否精华
     * @return 帖子列表
     */
    List<ForumPost> findByForumIdAndIsEssenceOrderByCreateTimeDesc(UUID forumId, Integer isEssence);

    /**
     * 根据论坛ID和帖子标题模糊查询
     *
     * @param forumId 论坛ID
     * @param title   帖子标题
     * @return 帖子列表
     */
    @Query("{'forum_id': ?0, 'title': {$regex: ?1, $options: 'i'}}")
    List<ForumPost> findByForumIdAndTitleContainingIgnoreCase(UUID forumId, String title);

    /**
     * 根据课程ID和帖子标题模糊查询
     *
     * @param courseId 课程ID
     * @param title    帖子标题
     * @return 帖子列表
     */
    @Query("{'course_id': ?0, 'title': {$regex: ?1, $options: 'i'}}")
    List<ForumPost> findByCourseIdAndTitleContainingIgnoreCase(UUID courseId, String title);

    /**
     * 根据章节ID查询帖子列表
     *
     * @param chapterId 章节ID
     * @return 帖子列表
     */
    List<ForumPost> findByChapterIdOrderByCreateTimeDesc(UUID chapterId);

    /**
     * 统计论坛下的帖子数量
     *
     * @param forumId 论坛ID
     * @return 帖子数量
     */
    long countByForumId(UUID forumId);

    /**
     * 统计课程下的帖子数量
     *
     * @param courseId 课程ID
     * @return 帖子数量
     */
    long countByCourseId(UUID courseId);

    /**
     * 统计作者发布的帖子数量
     *
     * @param sysUserId 作者ID
     * @return 帖子数量
     */
    long countBySysUserId(UUID sysUserId);

    /**
     * 统计论坛下指定状态的帖子数量
     *
     * @param forumId 论坛ID
     * @param status  状态
     * @return 帖子数量
     */
    long countByForumIdAndStatus(UUID forumId, Integer status);

    /**
     * 统计课程下指定状态的帖子数量
     *
     * @param courseId 课程ID
     * @param status   状态
     * @return 帖子数量
     */
    long countByCourseIdAndStatus(UUID courseId, Integer status);

    /**
     * 删除论坛下的所有帖子
     *
     * @param forumId 论坛ID
     */
    void deleteByForumId(UUID forumId);

    /**
     * 删除课程下的所有帖子
     *
     * @param courseId 课程ID
     */
    void deleteByCourseId(UUID courseId);

    /**
     * 根据论坛ID和状态删除帖子
     *
     * @param forumId 论坛ID
     * @param status  状态
     */
    void deleteByForumIdAndStatus(UUID forumId, Integer status);

    /**
     * 根据课程ID和状态删除帖子
     *
     * @param courseId 课程ID
     * @param status   状态
     */
    void deleteByCourseIdAndStatus(UUID courseId, Integer status);

    /**
     * 查询热门帖子（按点赞数排序）
     *
     * @param limit 限制数量
     * @return 热门帖子列表
     */
    @Query(value = "{}", sort = "{'like_count': -1, 'create_time': -1}")
    List<ForumPost> findTopPostsByLikeCount(int limit);

    /**
     * 查询最新帖子（按创建时间排序）
     *
     * @param limit 限制数量
     * @return 最新帖子列表
     */
    @Query(value = "{}", sort = "{'create_time': -1}")
    List<ForumPost> findLatestPosts(int limit);
}
