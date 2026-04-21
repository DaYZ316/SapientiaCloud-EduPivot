package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.skill;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionAgentContext;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.AgentEvidenceDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.PaperBlueprintDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.PaperSectionPlanDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.tool.ExamConstraintTool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates a deterministic paper blueprint that downstream generation can follow.
 */
@Component
@RequiredArgsConstructor
public class PaperPlanningSkill implements AgentSkill<PaperBlueprintDTO> {

    private static final int MAX_SECTION_BATCH_SIZE = 5;
    private static final List<Integer> MIXED_TYPES = List.of(0, 1, 2, 3, 4);

    private final ExamConstraintTool examConstraintTool;

    @Override
    public String name() {
        return "paperPlanning";
    }

    @Override
    public PaperBlueprintDTO execute(QuestionAgentContext context) {
        QuestionGenerateRequestDTO request = context.getRequest();
        int totalQuestionCount = request != null && request.getQuestionCount() != null ? request.getQuestionCount() : 1;
        int normalizedCount = Math.max(1, totalQuestionCount);

        PaperBlueprintDTO blueprint = new PaperBlueprintDTO();
        blueprint.setBlueprintId(context.getRequestId());
        blueprint.setGenerationStrategy("local_blueprint_planner");
        blueprint.setTotalQuestionCount(normalizedCount);

        List<PaperSectionPlanDTO> sections = request != null && request.getQuestionType() != null && request.getQuestionType() == 5
                ? buildMixedSections(request, context.getEvidences())
                : buildSingleTypeSections(request, context.getEvidences());
        assignSectionEstimatedTimes(request, sections);

        blueprint.setSections(sections);
        blueprint.setTotalScore(resolveTotalScore(request, sections));
        blueprint.setTotalEstimatedTime(resolveTotalEstimatedTime(request, sections));
        return blueprint;
    }

    private List<PaperSectionPlanDTO> buildSingleTypeSections(QuestionGenerateRequestDTO request,
                                                              List<AgentEvidenceDTO> evidences) {
        int totalCount = request != null && request.getQuestionCount() != null ? request.getQuestionCount() : 1;
        int questionType = request != null && request.getQuestionType() != null ? request.getQuestionType() : 0;
        int difficulty = normalizeDifficulty(request != null ? request.getDifficulty() : null, 1);

        List<PaperSectionPlanDTO> sections = new ArrayList<>();
        int sectionNo = 1;
        int remaining = totalCount;
        while (remaining > 0) {
            int batchSize = Math.min(MAX_SECTION_BATCH_SIZE, remaining);
            sections.add(buildSection(sectionNo++, questionType, difficulty, batchSize, request, evidences));
            remaining -= batchSize;
        }
        return sections;
    }

    private List<PaperSectionPlanDTO> buildMixedSections(QuestionGenerateRequestDTO request,
                                                         List<AgentEvidenceDTO> evidences) {
        int totalCount = request != null && request.getQuestionCount() != null ? request.getQuestionCount() : 1;
        List<PaperSectionPlanDTO> sections = new ArrayList<>();
        int typeGroupCount = Math.min(totalCount, MIXED_TYPES.size());
        int base = totalCount / typeGroupCount;
        int remainder = totalCount % typeGroupCount;
        int sectionNo = 1;

        for (int i = 0; i < typeGroupCount; i++) {
            int allocation = base + (i < remainder ? 1 : 0);
            if (allocation <= 0) {
                continue;
            }

            int questionType = MIXED_TYPES.get(i);
            while (allocation > 0) {
                int batchSize = Math.min(MAX_SECTION_BATCH_SIZE, allocation);
                int difficulty = normalizeDifficulty(request != null ? request.getDifficulty() : null, sectionNo);
                sections.add(buildSection(sectionNo, questionType, difficulty, batchSize, request, evidences));
                allocation -= batchSize;
                sectionNo++;
            }
        }
        return sections;
    }

    private PaperSectionPlanDTO buildSection(int sectionNo,
                                             int questionType,
                                             int difficulty,
                                             int targetCount,
                                             QuestionGenerateRequestDTO request,
                                             List<AgentEvidenceDTO> evidences) {
        PaperSectionPlanDTO section = new PaperSectionPlanDTO();
        section.setSectionNo(sectionNo);
        section.setSectionTitle("Section " + sectionNo);
        section.setQuestionType(questionType);
        section.setDifficulty(difficulty);
        section.setTargetCount(targetCount);
        section.setScorePerQuestion(request != null ? request.getScorePerQuestion() : null);
        section.setEstimatedTimePerQuestion(examConstraintTool.recommendEstimatedTime(questionType, difficulty));
        if (request != null && !CollectionUtils.isEmpty(request.getKnowledgePoints())) {
            section.setKnowledgePoints(new ArrayList<>(request.getKnowledgePoints()));
        }
        if (!CollectionUtils.isEmpty(evidences)) {
            int evidenceLimit = Math.min(5, evidences.size());
            section.setEvidences(new ArrayList<>(evidences.subList(0, evidenceLimit)));
        }
        return section;
    }

    private void assignSectionEstimatedTimes(QuestionGenerateRequestDTO request, List<PaperSectionPlanDTO> sections) {
        if (CollectionUtils.isEmpty(sections)) {
            return;
        }

        List<Integer> weights = new ArrayList<>(sections.size());
        List<Integer> minimums = new ArrayList<>(sections.size());
        int recommendedTotal = 0;
        for (PaperSectionPlanDTO section : sections) {
            int count = section.getTargetCount() != null ? Math.max(1, section.getTargetCount()) : 1;
            int suggestedPerQuestion = examConstraintTool.recommendEstimatedTime(section.getQuestionType(), section.getDifficulty());
            int sectionWeight = count * suggestedPerQuestion;
            weights.add(sectionWeight);
            minimums.add(count);
            recommendedTotal += sectionWeight;
        }

        Integer requestedTotal = request != null ? request.getTotalEstimatedTime() : null;
        int targetTotal = requestedTotal != null ? requestedTotal : recommendedTotal;
        List<Integer> allocations = allocateSectionTimes(weights, minimums, targetTotal);

        for (int i = 0; i < sections.size(); i++) {
            PaperSectionPlanDTO section = sections.get(i);
            int allocated = allocations.get(i);
            int count = section.getTargetCount() != null ? Math.max(1, section.getTargetCount()) : 1;
            section.setTotalEstimatedTime(allocated);
            section.setEstimatedTimePerQuestion(Math.max(1, Math.round((float) allocated / count)));
        }
    }

    private BigDecimal resolveTotalScore(QuestionGenerateRequestDTO request, List<PaperSectionPlanDTO> sections) {
        if (request != null && request.getTotalScore() != null) {
            return request.getTotalScore();
        }
        if (request == null || request.getScorePerQuestion() == null) {
            return null;
        }
        int totalCount = 0;
        for (PaperSectionPlanDTO section : sections) {
            totalCount += section.getTargetCount() != null ? section.getTargetCount() : 0;
        }
        return request.getScorePerQuestion().multiply(BigDecimal.valueOf(totalCount));
    }

    private Integer resolveTotalEstimatedTime(QuestionGenerateRequestDTO request, List<PaperSectionPlanDTO> sections) {
        int total = 0;
        for (PaperSectionPlanDTO section : sections) {
            total += section.getTotalEstimatedTime() != null ? section.getTotalEstimatedTime() : 0;
        }
        return total > 0 ? total : null;
    }

    private int normalizeDifficulty(Integer requestDifficulty, int sectionNo) {
        if (requestDifficulty != null && requestDifficulty >= 1 && requestDifficulty <= 3) {
            return requestDifficulty;
        }
        return ((Math.max(1, sectionNo) - 1) % 3) + 1;
    }

    private List<Integer> allocateSectionTimes(List<Integer> weights, List<Integer> minimums, int targetTotal) {
        if (weights == null || weights.isEmpty()) {
            return List.of();
        }

        int minimumTotal = 0;
        for (Integer minimum : minimums) {
            minimumTotal += Math.max(1, minimum != null ? minimum : 1);
        }

        int effectiveTarget = Math.max(targetTotal, minimumTotal);
        int remaining = effectiveTarget - minimumTotal;
        long totalWeight = 0L;
        for (Integer weight : weights) {
            totalWeight += Math.max(1, weight != null ? weight : 1);
        }

        List<Integer> allocations = new ArrayList<>(weights.size());
        List<Double> remainders = new ArrayList<>(weights.size());
        int allocatedExtra = 0;
        for (int i = 0; i < weights.size(); i++) {
            int minimum = Math.max(1, minimums.get(i) != null ? minimums.get(i) : 1);
            allocations.add(minimum);
            if (remaining == 0 || totalWeight <= 0L) {
                remainders.add(0D);
                continue;
            }

            int weight = Math.max(1, weights.get(i) != null ? weights.get(i) : 1);
            double rawExtra = (double) remaining * weight / totalWeight;
            int extra = (int) Math.floor(rawExtra);
            allocations.set(i, minimum + extra);
            remainders.add(rawExtra - extra);
            allocatedExtra += extra;
        }

        int leftover = remaining - allocatedExtra;
        while (leftover > 0) {
            int targetIndex = 0;
            double maxRemainder = -1D;
            for (int i = 0; i < remainders.size(); i++) {
                if (remainders.get(i) > maxRemainder) {
                    maxRemainder = remainders.get(i);
                    targetIndex = i;
                }
            }
            allocations.set(targetIndex, allocations.get(targetIndex) + 1);
            remainders.set(targetIndex, 0D);
            leftover--;
        }

        return allocations;
    }
}
