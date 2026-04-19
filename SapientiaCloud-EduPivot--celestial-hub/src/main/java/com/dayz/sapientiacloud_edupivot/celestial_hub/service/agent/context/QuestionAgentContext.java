package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.AgentEvidenceDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.PaperBlueprintDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.QuestionDraftDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.ValidationIssueDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shared mutable context for a single orchestration run.
 */
@Data
public class QuestionAgentContext {

    private String requestId;
    private UUID userId;
    private UUID sessionId;
    private QuestionGenerateRequestDTO request;
    private List<AgentEvidenceDTO> evidences = new ArrayList<>();
    private PaperBlueprintDTO blueprint;
    private List<QuestionDraftDTO> drafts = new ArrayList<>();
    private List<ValidationIssueDTO> issues = new ArrayList<>();
    private List<QuestionResponseDTO> finalQuestions = new ArrayList<>();
    private QuestionAgentStage stage = QuestionAgentStage.RECEIVED;

    public List<QuestionResponseDTO> getDraftQuestions() {
        List<QuestionResponseDTO> questions = new ArrayList<>();
        for (QuestionDraftDTO draft : drafts) {
            if (draft == null || draft.getQuestion() == null) {
                continue;
            }
            questions.add(draft.getQuestion());
        }
        return questions;
    }
}
