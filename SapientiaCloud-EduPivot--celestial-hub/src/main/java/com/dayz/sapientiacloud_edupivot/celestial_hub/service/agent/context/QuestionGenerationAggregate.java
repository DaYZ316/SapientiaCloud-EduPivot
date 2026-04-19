package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.PaperBlueprintDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.QuestionDraftDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.ValidationIssueDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Final orchestration output returned to the worker.
 */
@Data
public class QuestionGenerationAggregate {

    private String requestId;
    private UUID sessionId;
    private PaperBlueprintDTO blueprint;
    private List<QuestionDraftDTO> drafts = new ArrayList<>();
    private List<QuestionResponseDTO> finalQuestions = new ArrayList<>();
    private List<ValidationIssueDTO> issues = new ArrayList<>();
    private QuestionAgentStage stage;
}
