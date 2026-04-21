package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.registry;

import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.skill.PaperPlanningSkill;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.skill.PaperReviewSkill;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.skill.QuestionGenerationSkill;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Central access point for orchestration skills.
 */
@Getter
@Component
@RequiredArgsConstructor
public class SkillRegistry {

    private final PaperPlanningSkill paperPlanningSkill;
    private final QuestionGenerationSkill questionGenerationSkill;
    private final PaperReviewSkill paperReviewSkill;
}
