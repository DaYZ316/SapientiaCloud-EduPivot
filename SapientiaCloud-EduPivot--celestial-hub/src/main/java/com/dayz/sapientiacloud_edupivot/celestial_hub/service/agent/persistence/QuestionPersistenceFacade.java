package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.persistence;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.clients.CourseClient;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.dto.QuestionAnswerDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.dto.QuestionDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.dto.QuestionOptionDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo.QuestionVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionAnswerSimpleDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionOptionSimpleDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionAgentContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Optional persistence bridge for saving generated questions into the course question bank.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionPersistenceFacade {

    private final CourseClient courseClient;

    public List<QuestionVO> saveToQuestionBank(QuestionAgentContext context) {
        QuestionGenerateRequestDTO request = context.getRequest();
        if (request == null
                || !Boolean.TRUE.equals(request.getSaveToQuestionBank())
                || request.getCourseId() == null
                || request.getQuestionBankId() == null
                || context.getUserId() == null
                || CollectionUtils.isEmpty(context.getFinalQuestions())) {
            return Collections.emptyList();
        }

        List<QuestionVO> persistedQuestions = new ArrayList<>();
        for (QuestionResponseDTO question : context.getFinalQuestions()) {
            try {
                QuestionDTO dto = mapToCourseQuestionDTO(question, request, context.getUserId());
                Result<QuestionVO> result = courseClient.addQuestion(dto);
                if (result != null && result.isSuccess() && result.getData() != null) {
                    persistedQuestions.add(result.getData());
                } else {
                    log.warn("Failed to persist generated question. requestId={}, questionId={}",
                            context.getRequestId(), question != null ? question.getId() : null);
                }
            } catch (Exception e) {
                log.warn("Persist generated question failed. requestId={}, questionId={}, error={}",
                        context.getRequestId(),
                        question != null ? question.getId() : null,
                        e.getMessage());
            }
        }
        return persistedQuestions;
    }

    private QuestionDTO mapToCourseQuestionDTO(QuestionResponseDTO question,
                                               QuestionGenerateRequestDTO request,
                                               UUID userId) {
        QuestionDTO dto = new QuestionDTO();
        dto.setQuestionBankId(request.getQuestionBankId());
        dto.setCourseId(request.getCourseId());
        dto.setSysUserId(userId);
        dto.setQuestionTitle(question.getQuestionTitle());
        dto.setQuestionContent(question.getQuestionContent());
        dto.setQuestionType(question.getQuestionType());
        dto.setDifficulty(question.getDifficulty());
        dto.setScore(question.getScore());
        dto.setEstimatedTime(question.getEstimatedTime());
        dto.setTags(question.getTags());
        dto.setAllowPartialCredit(resolvePartialCredit(question));
        dto.setStatus(request.getSaveStatus() != null ? request.getSaveStatus() : 0);
        dto.setOptions(mapOptions(question.getOptions(), request.getCourseId()));
        dto.setAnswers(mapAnswers(question.getAnswers(), request.getCourseId()));
        dto.setCelestialQuestionId(question.getId());
        return dto;
    }

    private List<QuestionOptionDTO> mapOptions(List<QuestionOptionSimpleDTO> options, UUID courseId) {
        if (CollectionUtils.isEmpty(options)) {
            return null;
        }
        List<QuestionOptionDTO> mapped = new ArrayList<>(options.size());
        for (QuestionOptionSimpleDTO option : options) {
            if (option == null) {
                continue;
            }
            QuestionOptionDTO dto = new QuestionOptionDTO();
            dto.setId(option.getId());
            dto.setQuestionId(option.getQuestionId());
            dto.setCourseId(courseId);
            dto.setOptionContent(option.getOptionContent());
            dto.setOptionLabel(option.getOptionLabel());
            dto.setIsCorrect(option.getIsCorrect());
            dto.setScore(option.getScore());
            dto.setImageUrls(option.getImageUrls());
            dto.setExplanation(option.getExplanation());
            mapped.add(dto);
        }
        return mapped.isEmpty() ? null : mapped;
    }

    private List<QuestionAnswerDTO> mapAnswers(List<QuestionAnswerSimpleDTO> answers, UUID courseId) {
        if (CollectionUtils.isEmpty(answers)) {
            return null;
        }
        List<QuestionAnswerDTO> mapped = new ArrayList<>(answers.size());
        for (QuestionAnswerSimpleDTO answer : answers) {
            if (answer == null) {
                continue;
            }
            QuestionAnswerDTO dto = new QuestionAnswerDTO();
            dto.setId(answer.getId());
            dto.setQuestionId(answer.getQuestionId());
            dto.setCourseId(courseId);
            dto.setAnswerContent(answer.getAnswerContent());
            dto.setExplanation(answer.getExplanation());
            dto.setScore(answer.getScore());
            dto.setSortOrder(answer.getSortOrder());
            mapped.add(dto);
        }
        return mapped.isEmpty() ? null : mapped;
    }

    private Integer resolvePartialCredit(QuestionResponseDTO question) {
        if (question == null || question.getQuestionType() == null) {
            return 0;
        }
        return question.getQuestionType() == 1 ? 1 : 0;
    }
}
