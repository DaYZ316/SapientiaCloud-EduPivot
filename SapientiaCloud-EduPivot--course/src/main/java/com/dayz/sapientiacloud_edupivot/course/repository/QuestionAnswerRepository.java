package com.dayz.sapientiacloud_edupivot.course.repository;

import com.dayz.sapientiacloud_edupivot.course.entity.po.QuestionAnswer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionAnswerRepository extends MongoRepository<QuestionAnswer, UUID> {

    List<QuestionAnswer> findByQuestionIdAndDeleted(UUID questionId, Integer deleted);

    QuestionAnswer findByQuestionIdAndSortOrderAndDeleted(UUID questionId, Integer sortOrder, Integer deleted);

    QuestionAnswer findTopByQuestionIdAndDeletedOrderBySortOrderDesc(UUID questionId, Integer deleted);

    List<QuestionAnswer> findByQuestionIdInAndDeleted(List<UUID> questionIds, Integer deleted);
}

