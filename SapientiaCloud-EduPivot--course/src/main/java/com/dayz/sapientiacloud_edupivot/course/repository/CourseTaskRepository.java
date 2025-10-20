package com.dayz.sapientiacloud_edupivot.course.repository;

import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseTask;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface CourseTaskRepository extends MongoRepository<CourseTask, UUID> {

    List<CourseTask> findByCourseIdOrderByCreateTimeDesc(UUID courseId);

    List<CourseTask> findByCourseIdAndStatusOrderByCreateTimeDesc(UUID courseId, Integer status);

    List<CourseTask> findBySysUserIdOrderByCreateTimeDesc(UUID sysUserId);

    List<CourseTask> findBySysUserIdAndStatusOrderByCreateTimeDesc(UUID sysUserId, Integer status);

    List<CourseTask> findByCourseIdAndTaskTypeOrderByCreateTimeDesc(UUID courseId, Integer taskType);

    List<CourseTask> findByCourseIdAndTaskTypeAndStatusOrderByCreateTimeDesc(UUID courseId, Integer taskType, Integer status);

    List<CourseTask> findByCourseIdAndDifficultyOrderByCreateTimeDesc(UUID courseId, Integer difficulty);

    List<CourseTask> findByCourseIdAndDifficultyAndStatusOrderByCreateTimeDesc(UUID courseId, Integer difficulty, Integer status);

    List<CourseTask> findByStatusOrderByCreateTimeDesc(Integer status);

    List<CourseTask> findByTaskTypeOrderByCreateTimeDesc(Integer taskType);

    List<CourseTask> findByDifficultyOrderByCreateTimeDesc(Integer difficulty);

    long countByCourseId(UUID courseId);

    long countByCourseIdAndStatus(UUID courseId, Integer status);

    long countBySysUserId(UUID sysUserId);

    long countBySysUserIdAndStatus(UUID sysUserId, Integer status);

    @Query("{'course_id': ?0, 'is_deleted': 0}")
    List<CourseTask> findTop10ByCourseIdOrderByViewCountDesc(UUID courseId);

    @Query("{'course_id': ?0, 'is_deleted': 0}")
    List<CourseTask> findTop10ByCourseIdOrderByCreateTimeDesc(UUID courseId);

    @Query("{'tags': ?0, 'is_deleted': 0}")
    List<CourseTask> findByTagsContainingOrderByCreateTimeDesc(String tag);

    @Query("{'task_name': {$regex: ?0, $options: 'i'}, 'is_deleted': 0}")
    List<CourseTask> findByTaskNameContainingIgnoreCaseOrderByCreateTimeDesc(String taskName);
}
