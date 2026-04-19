package com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.tool;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KnowledgeSearchRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.KnowledgeSearchResultVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.ContentTypeEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.KnowledgeService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionAgentContext;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.AgentEvidenceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Wraps local knowledge retrieval as a generation tool.
 */
@Component
@RequiredArgsConstructor
public class KnowledgeSearchTool implements AgentTool {

    private final KnowledgeService knowledgeService;

    @Override
    public String name() {
        return "knowledgeSearch";
    }

    public List<AgentEvidenceDTO> search(QuestionAgentContext context) {
        QuestionGenerateRequestDTO request = context.getRequest();
        if (request == null || !Boolean.TRUE.equals(request.getUseRag())) {
            return List.of();
        }

        String query = buildQuery(request);
        if (!StringUtils.hasText(query)) {
            return List.of();
        }

        KnowledgeSearchRequestDTO searchRequest = new KnowledgeSearchRequestDTO();
        searchRequest.setQuery(query);
        searchRequest.setTopK(5);
        searchRequest.setSessionId(context.getSessionId());
        searchRequest.setFileReferences(request.getFileReferences());

        List<KnowledgeSearchResultVO> results = knowledgeService.searchKnowledge(searchRequest);
        if (results == null || results.isEmpty()) {
            return List.of();
        }

        List<AgentEvidenceDTO> evidences = new ArrayList<>();
        for (KnowledgeSearchResultVO result : results) {
            if (result == null || !StringUtils.hasText(result.getContent())) {
                continue;
            }
            AgentEvidenceDTO evidence = new AgentEvidenceDTO();
            ContentTypeEnum type = ContentTypeEnum.fromCode(result.getContentType());
            evidence.setSourceType(type != null ? type.getVectorNamespace() : "knowledge");
            evidence.setSourceId(StringUtils.hasText(result.getVectorId()) ? result.getVectorId() : result.getDocumentId());
            evidence.setTitle(StringUtils.hasText(result.getTitle()) ? result.getTitle() : "Knowledge Snippet");
            evidence.setExcerpt(result.getContent());
            evidence.setScore(result.getScore());
            HashMap<String, Object> metadata = new HashMap<>();
            metadata.put("contentType", result.getContentType());
            metadata.put("courseId", result.getCourseId());
            metadata.put("chapterId", result.getChapterId());
            metadata.put("questionBankId", result.getQuestionBankId());
            metadata.put("questionId", result.getQuestionId());
            metadata.put("fileId", result.getFileId());
            evidence.setMetadata(metadata);
            evidences.add(evidence);
        }
        return evidences;
    }

    private String buildQuery(QuestionGenerateRequestDTO request) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(request.getPaperName())) {
            sb.append(request.getPaperName()).append(' ');
        }
        if (request.getKnowledgePoints() != null && !request.getKnowledgePoints().isEmpty()) {
            sb.append(String.join(" ", request.getKnowledgePoints())).append(' ');
        }
        if (request.getAbilityGoals() != null && !request.getAbilityGoals().isEmpty()) {
            sb.append(String.join(" ", request.getAbilityGoals())).append(' ');
        }
        if (StringUtils.hasText(request.getRequirement())) {
            sb.append(request.getRequirement());
        }
        if (!StringUtils.hasText(sb.toString())) {
            sb.append("Generate ")
                    .append(request.getQuestionCount())
                    .append(" questions of type ")
                    .append(request.getQuestionType())
                    .append(" with difficulty ")
                    .append(request.getDifficulty());
        }
        return sb.toString().trim();
    }
}
