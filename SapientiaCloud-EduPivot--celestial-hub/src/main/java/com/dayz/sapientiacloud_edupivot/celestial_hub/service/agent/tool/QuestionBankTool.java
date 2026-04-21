package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.tool;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.clients.CourseClient;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo.CourseQuestionBankVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo.QuestionVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionAgentContext;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.AgentEvidenceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Reads question bank metadata and sample questions as evidence.
 */
@Component
@RequiredArgsConstructor
public class QuestionBankTool implements AgentTool {

    private final CourseClient courseClient;

    @Override
    public String name() {
        return "questionBank";
    }

    public List<AgentEvidenceDTO> loadEvidence(QuestionAgentContext context) {
        QuestionGenerateRequestDTO request = context.getRequest();
        if (request == null) {
            return List.of();
        }

        List<AgentEvidenceDTO> evidences = new ArrayList<>();
        UUID questionBankId = request.getQuestionBankId();
        if (questionBankId != null) {
            appendQuestionBankEvidence(questionBankId, evidences);
            appendQuestionSamples(questionBankId, evidences, 5);
            return evidences;
        }

        UUID courseId = request.getCourseId();
        if (courseId == null) {
            return List.of();
        }

        Result<List<CourseQuestionBankVO>> banksResult = courseClient.listQuestionBanksByCourseId(courseId);
        if (banksResult == null || !banksResult.isSuccess() || CollectionUtils.isEmpty(banksResult.getData())) {
            return List.of();
        }

        int bankLimit = Math.min(2, banksResult.getData().size());
        for (int i = 0; i < bankLimit; i++) {
            CourseQuestionBankVO bank = banksResult.getData().get(i);
            if (bank == null || bank.getId() == null) {
                continue;
            }
            evidences.add(toQuestionBankEvidence(bank));
            appendQuestionSamples(bank.getId(), evidences, 3);
        }
        return evidences;
    }

    private void appendQuestionBankEvidence(UUID questionBankId, List<AgentEvidenceDTO> evidences) {
        Result<CourseQuestionBankVO> result = courseClient.getQuestionBankById(questionBankId);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return;
        }
        evidences.add(toQuestionBankEvidence(result.getData()));
    }

    private AgentEvidenceDTO toQuestionBankEvidence(CourseQuestionBankVO bank) {
        AgentEvidenceDTO evidence = new AgentEvidenceDTO();
        evidence.setSourceType("question_bank");
        evidence.setSourceId(bank.getId() != null ? bank.getId().toString() : null);
        evidence.setTitle(StringUtils.hasText(bank.getBankName()) ? bank.getBankName() : "Question Bank");
        evidence.setExcerpt(StringUtils.hasText(bank.getDescription()) ? bank.getDescription() : "Question bank metadata");
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("courseId", bank.getCourseId());
        metadata.put("difficulty", bank.getDifficulty());
        metadata.put("tags", bank.getTags());
        metadata.put("questionCount", bank.getQuestionCount());
        evidence.setMetadata(metadata);
        return evidence;
    }

    private void appendQuestionSamples(UUID questionBankId, List<AgentEvidenceDTO> evidences, int limit) {
        Result<List<QuestionVO>> result = courseClient.listQuestionsByBankId(questionBankId);
        if (result == null || !result.isSuccess() || CollectionUtils.isEmpty(result.getData())) {
            return;
        }

        int sampleLimit = Math.min(limit, result.getData().size());
        for (int i = 0; i < sampleLimit; i++) {
            QuestionVO question = result.getData().get(i);
            if (question == null) {
                continue;
            }
            AgentEvidenceDTO evidence = new AgentEvidenceDTO();
            evidence.setSourceType("question_sample");
            evidence.setSourceId(question.getId() != null ? question.getId().toString() : null);
            evidence.setTitle(StringUtils.hasText(question.getQuestionTitle()) ? question.getQuestionTitle() : "Question Sample");
            evidence.setExcerpt(StringUtils.hasText(question.getQuestionContent())
                    ? question.getQuestionContent()
                    : question.getQuestionTitle());
            HashMap<String, Object> metadata = new HashMap<>();
            metadata.put("questionType", question.getQuestionType());
            metadata.put("difficulty", question.getDifficulty());
            metadata.put("score", question.getScore());
            metadata.put("tags", question.getTags());
            evidence.setMetadata(metadata);
            evidences.add(evidence);
        }
    }
}
