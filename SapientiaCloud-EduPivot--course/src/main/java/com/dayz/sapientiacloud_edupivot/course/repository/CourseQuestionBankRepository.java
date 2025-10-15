package com.dayz.sapientiacloud_edupivot.course.repository;

import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseQuestionBank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface CourseQuestionBankRepository extends MongoRepository<CourseQuestionBank, UUID> {


    List<CourseQuestionBank> findByCourseIdAndDeleted(UUID courseId, Integer deleted);

    List<CourseQuestionBank> findBySysUserIdAndDeleted(UUID sysUserId, Integer deleted);

    List<CourseQuestionBank> findByCourseIdAndSysUserIdAndDeleted(UUID courseId, UUID sysUserId, Integer deleted);

    List<CourseQuestionBank> findByBankTypeAndDeleted(Integer bankType, Integer deleted);

    List<CourseQuestionBank> findByIsPublicAndDeleted(Integer isPublic, Integer deleted);

    List<CourseQuestionBank> findByDifficultyAndDeleted(Integer difficulty, Integer deleted);
}
