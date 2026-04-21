package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.tool;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileReference;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.KnowledgeVector;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.KnowledgeService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionAgentContext;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.AgentEvidenceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Loads session-scoped file context as evidence.
 */
@Component
@RequiredArgsConstructor
public class FileContextTool implements AgentTool {

    private final KnowledgeService knowledgeService;

    @Override
    public String name() {
        return "fileContext";
    }

    public List<AgentEvidenceDTO> loadEvidence(QuestionAgentContext context) {
        QuestionGenerateRequestDTO request = context.getRequest();
        List<FileReference> fileReferences = request != null ? request.getFileReferences() : null;
        if (CollectionUtils.isEmpty(fileReferences) || context.getUserId() == null || context.getSessionId() == null) {
            return List.of();
        }

        List<KnowledgeVector> vectors = knowledgeService.findVectorsByFileReferences(
                fileReferences,
                context.getUserId(),
                context.getSessionId()
        );
        if (CollectionUtils.isEmpty(vectors)) {
            return List.of();
        }

        List<AgentEvidenceDTO> evidences = new ArrayList<>();
        int limit = Math.min(5, vectors.size());
        for (int i = 0; i < limit; i++) {
            KnowledgeVector vector = vectors.get(i);
            if (vector == null || !StringUtils.hasText(vector.getContent())) {
                continue;
            }
            AgentEvidenceDTO evidence = new AgentEvidenceDTO();
            evidence.setSourceType("file");
            evidence.setSourceId(vector.getContentId() != null ? vector.getContentId().toString() : vector.getVectorId());
            evidence.setTitle(StringUtils.hasText(vector.getTitle()) ? vector.getTitle() : "Uploaded File");
            evidence.setExcerpt(vector.getContent());
            HashMap<String, Object> metadata = new HashMap<>();
            metadata.put("fileId", vector.getContentId());
            metadata.put("sessionId", vector.getSessionId());
            metadata.put("userId", vector.getUserId());
            metadata.put("courseId", vector.getCourseId());
            evidence.setMetadata(metadata);
            evidences.add(evidence);
        }

        if (!evidences.isEmpty()) {
            return evidences;
        }

        String query = StringUtils.hasText(request.getRequirement())
                ? request.getRequirement()
                : "Generate questions from uploaded files";
        List<UUID> fileIds = new ArrayList<>();
        for (FileReference fileReference : fileReferences) {
            if (fileReference != null && fileReference.getId() != null) {
                fileIds.add(fileReference.getId());
            }
        }
        String fileContext = knowledgeService.retrieveFileContext(fileIds, query, 3);
        if (!StringUtils.hasText(fileContext)) {
            return List.of();
        }

        AgentEvidenceDTO fallback = new AgentEvidenceDTO();
        fallback.setSourceType("file");
        fallback.setSourceId("session-files");
        fallback.setTitle("Uploaded File Context");
        fallback.setExcerpt(fileContext);
        return List.of(fallback);
    }
}
