package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.tool;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Central access point for orchestration tools.
 */
@Getter
@Component
@RequiredArgsConstructor
public class ToolRegistry {

    private final KnowledgeSearchTool knowledgeSearchTool;
    private final QuestionBankTool questionBankTool;
    private final FileContextTool fileContextTool;
    private final ExamConstraintTool examConstraintTool;
}
