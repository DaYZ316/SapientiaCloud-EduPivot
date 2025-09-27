package com.dayz.sapientiacloud_edupivot.course.repository;

import com.dayz.sapientiacloud_edupivot.course.entity.po.ForumReply;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 论坛回复MongoDB数据访问接口
 *
 * @author SapientiaCloud
 * @since 2024-01-01
 */
@Repository
public interface ForumReplyRepository extends MongoRepository<ForumReply, UUID> {

    /**
     * 根据帖子ID查询回复列表
     *
     * @param postId 帖子ID
     * @return 回复列表
     */
    List<ForumReply> findByPostIdOrderByCreateTimeAsc(UUID postId);

    /**
     * 根据论坛ID查询回复列表
     *
     * @param forumId 论坛ID
     * @return 回复列表
     */
    List<ForumReply> findByForumIdOrderByCreateTimeDesc(UUID forumId);

    /**
     * 根据课程ID查询回复列表
     *
     * @param courseId 课程ID
     * @return 回复列表
     */
    List<ForumReply> findByCourseIdOrderByCreateTimeDesc(UUID courseId);

    /**
     * 根据作者ID查询回复列表
     *
     * @param sysUserId 作者ID
     * @return 回复列表
     */
    List<ForumReply> findBySysUserIdOrderByCreateTimeDesc(UUID sysUserId);

    /**
     * 根据父回复ID查询子回复列表
     *
     * @param parentReplyId 父回复ID
     * @return 子回复列表
     */
    List<ForumReply> findByParentReplyIdOrderByCreateTimeAsc(UUID parentReplyId);

    /**
     * 根据帖子ID和状态查询回复列表
     *
     * @param postId 帖子ID
     * @param status 状态
     * @return 回复列表
     */
    List<ForumReply> findByPostIdAndStatusOrderByCreateTimeAsc(UUID postId, Integer status);

    /**
     * 根据论坛ID和状态查询回复列表
     *
     * @param forumId 论坛ID
     * @param status  状态
     * @return 回复列表
     */
    List<ForumReply> findByForumIdAndStatusOrderByCreateTimeDesc(UUID forumId, Integer status);

    /**
     * 根据课程ID和状态查询回复列表
     *
     * @param courseId 课程ID
     * @param status   状态
     * @return 回复列表
     */
    List<ForumReply> findByCourseIdAndStatusOrderByCreateTimeDesc(UUID courseId, Integer status);

    /**
     * 根据帖子ID和父回复ID查询回复列表（构建回复树）
     *
     * @param postId        帖子ID
     * @param parentReplyId 父回复ID
     * @return 回复列表
     */
    List<ForumReply> findByPostIdAndParentReplyIdOrderByCreateTimeAsc(UUID postId, UUID parentReplyId);

    /**
     * 根据帖子ID查询根回复列表（父回复ID为null）
     *
     * @param postId 帖子ID
     * @return 根回复列表
     */
    List<ForumReply> findByPostIdAndParentReplyIdIsNullOrderByCreateTimeAsc(UUID postId);

    /**
     * 根据帖子ID和采纳状态查询回复列表
     *
     * @param postId     帖子ID
     * @param isAccepted 是否采纳
     * @return 回复列表
     */
    List<ForumReply> findByPostIdAndIsAcceptedOrderByCreateTimeAsc(UUID postId, Integer isAccepted);

    /**
     * 统计帖子下的回复数量
     *
     * @param postId 帖子ID
     * @return 回复数量
     */
    long countByPostId(UUID postId);

    /**
     * 统计论坛下的回复数量
     *
     * @param forumId 论坛ID
     * @return 回复数量
     */
    long countByForumId(UUID forumId);

    /**
     * 统计课程下的回复数量
     *
     * @param courseId 课程ID
     * @return 回复数量
     */
    long countByCourseId(UUID courseId);

    /**
     * 统计作者发布的回复数量
     *
     * @param sysUserId 作者ID
     * @return 回复数量
     */
    long countBySysUserId(UUID sysUserId);

    /**
     * 统计父回复下的子回复数量
     *
     * @param parentReplyId 父回复ID
     * @return 子回复数量
     */
    long countByParentReplyId(UUID parentReplyId);

    /**
     * 统计帖子下指定状态的回复数量
     *
     * @param postId 帖子ID
     * @param status 状态
     * @return 回复数量
     */
    long countByPostIdAndStatus(UUID postId, Integer status);

    /**
     * 统计论坛下指定状态的回复数量
     *
     * @param forumId 论坛ID
     * @param status  状态
     * @return 回复数量
     */
    long countByForumIdAndStatus(UUID forumId, Integer status);

    /**
     * 统计课程下指定状态的回复数量
     *
     * @param courseId 课程ID
     * @param status   状态
     * @return 回复数量
     */
    long countByCourseIdAndStatus(UUID courseId, Integer status);

    /**
     * 删除帖子下的所有回复
     *
     * @param postId 帖子ID
     */
    void deleteByPostId(UUID postId);

    /**
     * 删除论坛下的所有回复
     *
     * @param forumId 论坛ID
     */
    void deleteByForumId(UUID forumId);

    /**
     * 删除课程下的所有回复
     *
     * @param courseId 课程ID
     */
    void deleteByCourseId(UUID courseId);

    /**
     * 根据帖子ID和状态删除回复
     *
     * @param postId 帖子ID
     * @param status 状态
     */
    void deleteByPostIdAndStatus(UUID postId, Integer status);

    /**
     * 根据论坛ID和状态删除回复
     *
     * @param forumId 论坛ID
     * @param status  状态
     */
    void deleteByForumIdAndStatus(UUID forumId, Integer status);

    /**
     * 根据课程ID和状态删除回复
     *
     * @param courseId 课程ID
     * @param status   状态
     */
    void deleteByCourseIdAndStatus(UUID courseId, Integer status);

    /**
     * 根据帖子ID查询楼层号最大的回复
     *
     * @param postId 帖子ID
     * @return 楼层号最大的回复
     */
    @Query(value = "{'post_id': ?0}", sort = "{'floor_number': -1}")
    Optional<ForumReply> findTopByPostIdOrderByFloorNumberDesc(UUID postId);
}
