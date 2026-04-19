package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context;

/**
 * Lifecycle stages for question generation orchestration.
 */
public enum QuestionAgentStage {
    RECEIVED,
    CONTEXT_READY,
    PLANNED,
    GENERATED,
    VALIDATED,
    REPAIRED,
    ASSEMBLED,
    RESPONDED,
    FAILED
}
