package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Generated draft question with evidence and review state.
 */
@Data
public class QuestionDraftDTO {

    private Integer sectionNo;
    private Integer draftNo;
    private QuestionResponseDTO question;
    private List<AgentEvidenceDTO> evidences = new ArrayList<>();
    private String reviewStatus;
    private List<ValidationIssueDTO> issues = new ArrayList<>();
}
