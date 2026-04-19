package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.assembler;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionAnswerSimpleDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateAnswerRecord;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateOptionRecord;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRecord;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionOptionSimpleDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Maps model records to API response DTOs.
 */
@Component
public class QuestionResponseAssembler {

    public List<QuestionResponseDTO> fromRecords(List<QuestionGenerateRecord> records, UUID userId, String requestId) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }

        List<QuestionResponseDTO> responses = new ArrayList<>(records.size());
        for (QuestionGenerateRecord record : records) {
            if (record == null) {
                continue;
            }
            responses.add(fromRecord(record, userId, requestId));
        }
        return responses;
    }

    public QuestionResponseDTO fromRecord(QuestionGenerateRecord record, UUID userId, String requestId) {
        QuestionResponseDTO dto = new QuestionResponseDTO();
        UUID questionId = UuidCreator.getTimeOrderedEpoch();
        dto.setId(questionId);
        dto.setSysUserId(userId);
        dto.setRequestId(requestId);
        dto.setQuestionTitle(record.questionTitle());
        dto.setQuestionContent(record.questionContent());
        dto.setQuestionType(record.questionType());
        dto.setDifficulty(record.difficulty());
        dto.setScore(record.score());
        dto.setEstimatedTime(record.estimatedTime());
        dto.setTags(record.tags());
        dto.setOptions(copyOptions(record.options(), questionId));
        dto.setAnswers(copyAnswers(record.answers(), questionId));
        return dto;
    }

    private List<QuestionOptionSimpleDTO> copyOptions(List<QuestionGenerateOptionRecord> options, UUID questionId) {
        if (options == null || options.isEmpty()) {
            return null;
        }

        List<QuestionOptionSimpleDTO> copies = new ArrayList<>(options.size());
        for (QuestionGenerateOptionRecord option : options) {
            if (option == null) {
                continue;
            }
            QuestionOptionSimpleDTO copy = new QuestionOptionSimpleDTO();
            copy.setId(UuidCreator.getTimeOrderedEpoch());
            copy.setQuestionId(questionId);
            copy.setOptionContent(option.optionContent());
            copy.setOptionLabel(option.optionLabel());
            copy.setIsCorrect(option.isCorrect());
            copy.setScore(option.score());
            copy.setImageUrls(option.imageUrls());
            copy.setExplanation(option.explanation());
            copies.add(copy);
        }
        return copies.isEmpty() ? null : copies;
    }

    private List<QuestionAnswerSimpleDTO> copyAnswers(List<QuestionGenerateAnswerRecord> answers, UUID questionId) {
        if (answers == null || answers.isEmpty()) {
            return null;
        }

        List<QuestionAnswerSimpleDTO> copies = new ArrayList<>(answers.size());
        for (QuestionGenerateAnswerRecord answer : answers) {
            if (answer == null) {
                continue;
            }
            QuestionAnswerSimpleDTO copy = new QuestionAnswerSimpleDTO();
            copy.setId(UuidCreator.getTimeOrderedEpoch());
            copy.setQuestionId(questionId);
            copy.setAnswerContent(answer.answerContent());
            copy.setExplanation(answer.explanation());
            copy.setScore(answer.score());
            copy.setSortOrder(answer.sortOrder());
            copies.add(copy);
        }
        return copies.isEmpty() ? null : copies;
    }
}
