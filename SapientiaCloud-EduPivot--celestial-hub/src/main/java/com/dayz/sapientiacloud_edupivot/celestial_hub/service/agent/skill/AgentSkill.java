package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.skill;

import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionAgentContext;

/**
 * Generic skill contract for orchestration steps.
 */
public interface AgentSkill<T> {

    String name();

    T execute(QuestionAgentContext context);
}
