package com.dayz.sapientiacloud_edupivot.student.repository;

import com.dayz.sapientiacloud_edupivot.student.entity.po.QuestionStudent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionStudentRepository extends MongoRepository<QuestionStudent, UUID> {

    List<QuestionStudent> findByClassroomIdAndStudentIdAndIsDeleted(UUID classroomId, UUID studentId, Integer isDeleted);

    QuestionStudent findByClassroomIdAndStudentIdAndQuestionIdAndIsDeleted(UUID classroomId, UUID studentId, UUID questionId, Integer isDeleted);

    List<QuestionStudent> findByStudentIdAndIsDeleted(UUID studentId, Integer isDeleted);

    List<QuestionStudent> findByClassroomIdAndIsDeleted(UUID classroomId, Integer isDeleted);
}