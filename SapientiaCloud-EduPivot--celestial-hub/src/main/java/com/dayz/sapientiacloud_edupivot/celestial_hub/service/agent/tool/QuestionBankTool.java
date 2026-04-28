package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.tool;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.clients.CourseClient;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo.CourseQuestionBankVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo.QuestionVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionAgentContext;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.AgentEvidenceDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.trace.QuestionGenerationTracePayloads;
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
            appendQuestionBankEvidence(questionBankId, evidences, context);
            appendQuestionSamples(questionBankId, evidences, 5, context);
            emitTrace(context, evidences, context.localize("已直接从所选题库加载参考资料。", "Loaded reference materials directly from the selected question bank."));
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
            evidences.add(toQuestionBankEvidence(bank, context));
            appendQuestionSamples(bank.getId(), evidences, 3, context);
        }
        emitTrace(context, evidences, evidences.isEmpty()
                ? context.localize("当前课程下暂无可用题库参考资料。", "No question bank references are available for the current course.")
                : context.localize("已从当前课程加载题库参考信息和样题。", "Loaded question bank references and sample questions from the current course."));
        return evidences;
    }

    private void appendQuestionBankEvidence(UUID questionBankId,
                                            List<AgentEvidenceDTO> evidences,
                                            QuestionAgentContext context) {
        Result<CourseQuestionBankVO> result = courseClient.getQuestionBankById(questionBankId);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return;
        }
        evidences.add(toQuestionBankEvidence(result.getData(), context));
    }

    private AgentEvidenceDTO toQuestionBankEvidence(CourseQuestionBankVO bank, QuestionAgentContext context) {
        AgentEvidenceDTO evidence = new AgentEvidenceDTO();
        evidence.setSourceType("question_bank");
        evidence.setSourceId(bank.getId() != null ? bank.getId().toString() : null);
        evidence.setTitle(StringUtils.hasText(bank.getBankName()) ? bank.getBankName() : context.localize("题库", "Question bank"));
        evidence.setExcerpt(StringUtils.hasText(bank.getDescription()) ? bank.getDescription() : context.localize("题库基础信息", "Question bank metadata"));
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("courseId", bank.getCourseId());
        metadata.put("difficulty", bank.getDifficulty());
        metadata.put("tags", bank.getTags());
        metadata.put("questionCount", bank.getQuestionCount());
        evidence.setMetadata(metadata);
        return evidence;
    }

    private void appendQuestionSamples(UUID questionBankId,
                                       List<AgentEvidenceDTO> evidences,
                                       int limit,
                                       QuestionAgentContext context) {
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
            evidence.setTitle(StringUtils.hasText(question.getQuestionTitle()) ? question.getQuestionTitle() : context.localize("题库样题", "Question bank sample"));
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

    private void emitTrace(QuestionAgentContext context, List<AgentEvidenceDTO> evidences, String summary) {
        if (context == null) {
            return;
        }
        context.appendTraceEntry(
                "questionBank",
                "evidence_batch",
                context.localize("题库参考", "Question bank reference"),
                summary,
                QuestionGenerationTracePayloads.evidenceBatch(null, evidences)
        );
    }
}
