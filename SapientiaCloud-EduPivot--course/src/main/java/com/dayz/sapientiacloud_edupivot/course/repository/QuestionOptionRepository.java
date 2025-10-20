package com.dayz.sapientiacloud_edupivot.course.repository;

import com.dayz.sapientiacloud_edupivot.course.entity.po.QuestionOption;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionOptionRepository extends MongoRepository<QuestionOption, UUID> {

    List<QuestionOption> findByQuestionIdAndDeleted(UUID questionId, Integer deleted);

    QuestionOption findByQuestionIdAndOptionLabelAndDeleted(UUID questionId, String optionLabel, Integer deleted);

    List<QuestionOption> findByQuestionIdAndIsCorrectAndDeleted(UUID questionId, Integer isCorrect, Integer deleted);

    List<QuestionOption> findByQuestionIdInAndDeleted(List<UUID> questionIds, Integer deleted);
}
