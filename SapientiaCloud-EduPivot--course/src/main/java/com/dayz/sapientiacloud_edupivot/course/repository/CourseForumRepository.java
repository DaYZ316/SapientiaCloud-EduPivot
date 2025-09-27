package com.dayz.sapientiacloud_edupivot.course.repository;

import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseForum;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 课程论坛MongoDB数据访问接口
 *
 * @author SapientiaCloud
 * @since 2024-01-01
 */
@Repository
public interface CourseForumRepository extends MongoRepository<CourseForum, UUID> {

    /**
     * 根据课程ID查询论坛列表
     *
     * @param courseId 课程ID
     * @return 论坛列表
     */
    List<CourseForum> findByCourseIdOrderBySortOrderAsc(UUID courseId);

    /**
     * 根据课程ID和论坛类型查询论坛列表
     *
     * @param courseId  课程ID
     * @param forumType 论坛类型
     * @return 论坛列表
     */
    List<CourseForum> findByCourseIdAndForumTypeOrderBySortOrderAsc(UUID courseId, Integer forumType);

    /**
     * 根据课程ID和状态查询论坛列表
     *
     * @param courseId 课程ID
     * @param status   状态
     * @return 论坛列表
     */
    List<CourseForum> findByCourseIdAndStatusOrderBySortOrderAsc(UUID courseId, Integer status);

    /**
     * 根据课程ID和论坛名称模糊查询
     *
     * @param courseId  课程ID
     * @param forumName 论坛名称
     * @return 论坛列表
     */
    @Query("{'course_id': ?0, 'forum_name': {$regex: ?1, $options: 'i'}}")
    List<CourseForum> findByCourseIdAndForumNameContainingIgnoreCase(UUID courseId, String forumName);

    /**
     * 根据课程ID和论坛名称查询
     *
     * @param courseId  课程ID
     * @param forumName 论坛名称
     * @return 论坛
     */
    Optional<CourseForum> findByCourseIdAndForumName(UUID courseId, String forumName);

    /**
     * 检查课程下是否存在指定名称的论坛
     *
     * @param courseId  课程ID
     * @param forumName 论坛名称
     * @return 是否存在
     */
    boolean existsByCourseIdAndForumName(UUID courseId, String forumName);

    /**
     * 统计课程下的论坛数量
     *
     * @param courseId 课程ID
     * @return 论坛数量
     */
    long countByCourseId(UUID courseId);

    /**
     * 统计课程下指定状态的论坛数量
     *
     * @param courseId 课程ID
     * @param status   状态
     * @return 论坛数量
     */
    long countByCourseIdAndStatus(UUID courseId, Integer status);

    /**
     * 统计课程下指定类型的论坛数量
     *
     * @param courseId  课程ID
     * @param forumType 论坛类型
     * @return 论坛数量
     */
    long countByCourseIdAndForumType(UUID courseId, Integer forumType);

    /**
     * 删除课程下的所有论坛
     *
     * @param courseId 课程ID
     */
    void deleteByCourseId(UUID courseId);

    /**
     * 根据课程ID和状态删除论坛
     *
     * @param courseId 课程ID
     * @param status   状态
     */
    void deleteByCourseIdAndStatus(UUID courseId, Integer status);

    /**
     * 根据版主ID查询论坛列表
     *
     * @param moderatorId 版主ID
     * @return 论坛列表
     */
    @Query("{'moderator_ids': ?0}")
    List<CourseForum> findByModeratorId(UUID moderatorId);
}
