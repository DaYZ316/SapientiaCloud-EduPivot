package com.dayz.sapientiacloud_edupivot.course.repository;

import com.dayz.sapientiacloud_edupivot.course.entity.po.Question;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends MongoRepository<Question, UUID> {

    List<Question> findByQuestionBankIdAndDeleted(UUID questionBankId, Integer deleted);

    List<Question> findBySysUserIdAndDeleted(UUID sysUserId, Integer deleted);

    List<Question> findByQuestionTypeAndDeleted(Integer questionType, Integer deleted);

    List<Question> findByDifficultyAndDeleted(Integer difficulty, Integer deleted);

    List<Question> findByStatusAndDeleted(Integer status, Integer deleted);

    List<Question> findByTagsContainingAndDeleted(String tag, Integer deleted);

    List<Question> findByQuestionBankIdAndQuestionTypeAndDeleted(UUID questionBankId, Integer questionType, Integer deleted);

    List<Question> findByQuestionBankIdAndDifficultyAndDeleted(UUID questionBankId, Integer difficulty, Integer deleted);
}
