package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.AgentEvidenceDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.PaperBlueprintDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.QuestionDraftDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.QuestionGenerationTraceEntryDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.ValidationIssueDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.QuestionGenerationLocaleUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

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
    private List<QuestionGenerationTraceEntryDTO> traceEntries = new ArrayList<>();
    private QuestionAgentStage stage = QuestionAgentStage.RECEIVED;
    private transient Consumer<QuestionGenerationTraceEntryDTO> traceEntryListener;

    public String resolveLocale() {
        return QuestionGenerationLocaleUtils.resolveLocale(request);
    }

    public String localize(String zhCn, String enUs) {
        return QuestionGenerationLocaleUtils.text(resolveLocale(), zhCn, enUs);
    }

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

    public QuestionGenerationTraceEntryDTO appendTraceEntry(String source,
                                                            String detailType,
                                                            String title,
                                                            String summary,
                                                            Object payload) {
        QuestionGenerationTraceEntryDTO traceEntry = new QuestionGenerationTraceEntryDTO();
        traceEntry.setEntryId(UUID.randomUUID().toString());
        traceEntry.setStage(stage != null ? stage.name() : null);
        traceEntry.setSource(source);
        traceEntry.setDetailType(detailType);
        traceEntry.setTitle(title);
        traceEntry.setSummary(summary);
        traceEntry.setPayload(payload);
        traceEntry.setTimestamp(System.currentTimeMillis());
        traceEntries.add(traceEntry);
        if (traceEntryListener != null) {
            traceEntryListener.accept(traceEntry);
        }
        return traceEntry;
    }
}
