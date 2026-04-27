package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.alibaba.fastjson2.JSON;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.AIChatConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.QuestionConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatMessage;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.ChatSessionVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.AIChatEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.ChatRoleEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.QuestionGenerationMode;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.SessionTypeEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.ChatMessageRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.QuestionAgentOrchestrator;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionAgentStage;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionGenerationAggregate;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.ChatMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Kafka worker for AI question generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionGenerateWorker {

    private static final String QUESTION_IN_PROGRESS_MESSAGE = "正在出题中...";
    private static final String QUESTION_FAILURE_MESSAGE = "题目生成失败";
    private static final String PAPER_FAILURE_MESSAGE = "试卷生成失败";

    private final QuestionAgentOrchestrator questionAgentOrchestrator;
    private final KafkaQuestionService kafkaQuestionService;
    private final ChatMessageRepository chatMessageRepository;
    private final IChatSessionService chatSessionService;

    @Value("${kafka.question.stage-min-duration-ms:2000}")
    private long stageMinDurationMs;

    @KafkaListener(topics = "${spring.kafka.topic.question-request:question-request-topic}",
            groupId = "${spring.kafka.consumer.group-id:chat-group}-question-worker")
    public void consumeQuestionRequest(@Payload String message,
                                       @Header(KafkaHeaders.RECEIVED_KEY) String requestId,
                                       Acknowledgment acknowledgment) {
        String fallbackRequestId = StringUtils.hasText(requestId) ? requestId : UUID.randomUUID().toString();
        KafkaQuestionService.QuestionRequestMessage requestMessage =
                parseQuestionRequestMessage(message, fallbackRequestId);
        if (requestMessage == null) {
            sendProgress(fallbackRequestId, null, QuestionGenerationMode.QUESTION, QuestionAgentStage.FAILED, QUESTION_FAILURE_MESSAGE);
            kafkaQuestionService.sendQuestionResponse(fallbackRequestId, "[]");
            acknowledgeSafely(acknowledgment, fallbackRequestId);
            return;
        }

        String finalRequestId = Optional.ofNullable(requestMessage.getRequestId())
                .filter(StringUtils::hasText)
                .orElse(fallbackRequestId);
        QuestionGenerateRequestDTO request = requestMessage.getRequest();
        UUID requestUserId = requestMessage.getUserId();
        if (request == null) {
            log.warn("Question generation request ignored because body is empty. requestId={}", finalRequestId);
            sendProgress(finalRequestId, null, QuestionGenerationMode.QUESTION, QuestionAgentStage.FAILED, QUESTION_FAILURE_MESSAGE);
            kafkaQuestionService.sendQuestionResponse(finalRequestId, "[]");
            acknowledgeSafely(acknowledgment, finalRequestId);
            return;
        }

        QuestionGenerationMode generationMode = QuestionGenerationMode.resolve(request);
        request.setGenerationMode(generationMode.getCode());

        UUID resolvedSessionId = request.getSessionId();
        AtomicLong lastStageProgressAt = new AtomicLong(0L);
        try {
            UUID sessionId = getOrCreateSessionId(request, requestUserId);
            resolvedSessionId = sessionId;
            sendProgress(finalRequestId, sessionId, generationMode, null, resolveWarmupMessage(generationMode));

            ChatMessage existingResponseMessage = findExistingResponseMessage(sessionId, finalRequestId);
            if (existingResponseMessage != null && StringUtils.hasText(existingResponseMessage.getQuestionResponse())) {
                log.info("Question request already processed. requestId={}, sessionId={}", finalRequestId, sessionId);
                sendStageProgress(
                        finalRequestId,
                        sessionId,
                        generationMode,
                        QuestionAgentStage.RESPONDED,
                        resolveResultReplayMessage(generationMode),
                        lastStageProgressAt
                );
                kafkaQuestionService.sendQuestionResponse(finalRequestId, existingResponseMessage.getQuestionResponse());
                acknowledgeSafely(acknowledgment, finalRequestId);
                return;
            }

            persistRequestMessage(sessionId, request, finalRequestId, generationMode);
            sendProgress(finalRequestId, sessionId, generationMode, null, resolvePersistenceMessage(generationMode));

            QuestionGenerationAggregate aggregate = questionAgentOrchestrator.run(
                    request,
                    requestUserId,
                    finalRequestId,
                    stage -> sendStageProgress(
                            finalRequestId,
                            sessionId,
                            generationMode,
                            stage,
                            resolveStageMessage(stage, generationMode),
                            lastStageProgressAt
                    )
            );
            List<QuestionResponseDTO> questions = aggregate != null && !CollectionUtils.isEmpty(aggregate.getFinalQuestions())
                    ? aggregate.getFinalQuestions()
                    : List.of();

            persistResponseMessage(sessionId, request, finalRequestId, questions, generationMode);
            sendStageProgress(
                    finalRequestId,
                    sessionId,
                    generationMode,
                    QuestionAgentStage.RESPONDED,
                    resolveResultPersistenceMessage(generationMode),
                    lastStageProgressAt
            );
            kafkaQuestionService.sendQuestionResponse(finalRequestId, JSON.toJSONString(questions));
            acknowledgeSafely(acknowledgment, finalRequestId);
        } catch (Exception e) {
            log.error("Question generation worker failed. requestId={}, error={}", finalRequestId, e.getMessage(), e);
            sendStageProgress(
                    finalRequestId,
                    resolvedSessionId,
                    generationMode,
                    QuestionAgentStage.FAILED,
                    resolveFailureMessage(generationMode),
                    lastStageProgressAt
            );
            kafkaQuestionService.sendQuestionResponse(finalRequestId, "[]");
            acknowledgeSafely(acknowledgment, finalRequestId);
        }
    }

    private void sendStageProgress(String requestId,
                                   UUID sessionId,
                                   QuestionGenerationMode generationMode,
                                   QuestionAgentStage stage,
                                   String message,
                                   AtomicLong lastStageProgressAt) {
        waitForMinimumStageDuration(lastStageProgressAt);
        sendProgress(requestId, sessionId, generationMode, stage, message);
        if (lastStageProgressAt != null) {
            lastStageProgressAt.set(System.currentTimeMillis());
        }
    }

    private void waitForMinimumStageDuration(AtomicLong lastStageProgressAt) {
        if (lastStageProgressAt == null) {
            return;
        }
        long minimumDuration = Math.max(0L, stageMinDurationMs);
        if (minimumDuration <= 0L) {
            return;
        }

        long previousTimestamp = lastStageProgressAt.get();
        if (previousTimestamp <= 0L) {
            return;
        }

        long elapsed = System.currentTimeMillis() - previousTimestamp;
        long remaining = minimumDuration - elapsed;
        if (remaining <= 0L) {
            return;
        }

        try {
            Thread.sleep(remaining);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for minimum stage duration. remaining={}ms", remaining);
        }
    }

    private KafkaQuestionService.QuestionRequestMessage parseQuestionRequestMessage(String message, String requestId) {
        if (!StringUtils.hasText(message)) {
            log.warn("Question generation request payload is empty. requestId={}", requestId);
            return null;
        }
        try {
            return JSON.parseObject(message, KafkaQuestionService.QuestionRequestMessage.class);
        } catch (Exception e) {
            log.warn("Failed to parse question generation request payload. requestId={}, error={}", requestId, e.getMessage());
            return null;
        }
    }

    private void sendProgress(String requestId,
                              UUID sessionId,
                              QuestionGenerationMode generationMode,
                              QuestionAgentStage stage,
                              String message) {
        kafkaQuestionService.sendQuestionProgress(
                requestId,
                sessionId,
                "processing",
                stage != null ? stage.name() : null,
                message,
                null,
                generationMode != null ? generationMode.getCode() : QuestionGenerationMode.QUESTION.getCode()
        );
    }

    private String resolveStageMessage(QuestionAgentStage stage, QuestionGenerationMode generationMode) {
        if (stage == null) {
            return generationMode != null && generationMode.isPaper()
                    ? "正在生成试卷中..."
                    : QUESTION_IN_PROGRESS_MESSAGE;
        }
        if (stage == QuestionAgentStage.FAILED) {
            return resolveFailureMessage(generationMode);
        }
        if (generationMode == null || !generationMode.isPaper()) {
            return QUESTION_IN_PROGRESS_MESSAGE;
        }
        return switch (stage) {
            case RECEIVED -> "正在接收出卷请求";
            case CONTEXT_READY -> "正在准备出卷上下文";
            case PLANNED -> "正在规划试卷结构";
            case GENERATED -> "正在生成题目草稿";
            case VALIDATED -> "正在校验题目质量";
            case REPAIRED -> "正在修复题目问题";
            case ASSEMBLED -> "正在组装最终试卷";
            case RESPONDED -> "正在写入并返回结果";
            case FAILED -> resolveFailureMessage(generationMode);
        };
    }

    private String resolveWarmupMessage(QuestionGenerationMode generationMode) {
        return generationMode != null && generationMode.isPaper()
                ? "正在准备试卷生成任务"
                : QUESTION_IN_PROGRESS_MESSAGE;
    }

    private String resolvePersistenceMessage(QuestionGenerationMode generationMode) {
        return generationMode != null && generationMode.isPaper()
                ? "正在保存出卷请求"
                : QUESTION_IN_PROGRESS_MESSAGE;
    }

    private String resolveResultPersistenceMessage(QuestionGenerationMode generationMode) {
        return generationMode != null && generationMode.isPaper()
                ? "正在返回生成结果"
                : QUESTION_IN_PROGRESS_MESSAGE;
    }

    private String resolveResultReplayMessage(QuestionGenerationMode generationMode) {
        return generationMode != null && generationMode.isPaper()
                ? "正在回传历史试卷结果"
                : QUESTION_IN_PROGRESS_MESSAGE;
    }

    private String resolveFailureMessage(QuestionGenerationMode generationMode) {
        return generationMode != null && generationMode.isPaper()
                ? PAPER_FAILURE_MESSAGE
                : QUESTION_FAILURE_MESSAGE;
    }

    private void persistRequestMessage(UUID sessionId,
                                       QuestionGenerateRequestDTO request,
                                       String requestId,
                                       QuestionGenerationMode generationMode) {
        ChatMessage existingRequestMessage = chatMessageRepository.findFirstBySessionIdAndRoleAndRequestId(
                sessionId,
                ChatRoleEnum.QUESTION_REQUESTER.getCode(),
                requestId
        );
        if (existingRequestMessage != null) {
            return;
        }

        ChatMessage requestMessageEntity = new ChatMessage();
        requestMessageEntity.setId(UUID.randomUUID());
        requestMessageEntity.setSessionId(sessionId);
        requestMessageEntity.setRole(ChatRoleEnum.QUESTION_REQUESTER.getCode());
        requestMessageEntity.setContent(buildRequestSummary(request, generationMode));
        requestMessageEntity.setMessageType(AIChatConstants.MESSAGE_TYPE_TEXT);
        requestMessageEntity.setQuestionRequest(JSON.toJSONString(request));
        requestMessageEntity.setMetadata(buildGenerationMetadata(request, generationMode));
        requestMessageEntity.setModelName(AIChatConstants.MODEL_QWEN3_MAX);
        requestMessageEntity.setTokenCount(ChatMessageUtil.estimateTokens(requestMessageEntity.getContent()));
        requestMessageEntity.setIsFeedback(AIChatConstants.FEEDBACK_NONE);
        requestMessageEntity.setRequestId(requestId);
        requestMessageEntity.setCreateTime(LocalDateTime.now());
        requestMessageEntity.setUpdateTime(LocalDateTime.now());
        chatMessageRepository.save(requestMessageEntity);
    }

    private void persistResponseMessage(UUID sessionId,
                                        QuestionGenerateRequestDTO request,
                                        String requestId,
                                        List<QuestionResponseDTO> questions,
                                        QuestionGenerationMode generationMode) {
        ChatMessage responseMessageEntity = new ChatMessage();
        responseMessageEntity.setId(UUID.randomUUID());
        responseMessageEntity.setSessionId(sessionId);
        responseMessageEntity.setRole(ChatRoleEnum.QUESTION_GENERATOR.getCode());
        responseMessageEntity.setContent(String.format(QuestionConstants.QUESTION_COMPLETE_TEMPLATE, questions.size()));
        responseMessageEntity.setMessageType(AIChatConstants.MESSAGE_TYPE_TEXT);
        responseMessageEntity.setQuestionResponse(JSON.toJSONString(questions));
        responseMessageEntity.setMetadata(buildGenerationMetadata(request, generationMode));
        responseMessageEntity.setModelName(AIChatConstants.MODEL_QWEN3_MAX);
        responseMessageEntity.setTokenCount(ChatMessageUtil.estimateTokens(responseMessageEntity.getContent()));
        responseMessageEntity.setIsFeedback(AIChatConstants.FEEDBACK_NONE);
        responseMessageEntity.setRequestId(requestId);
        responseMessageEntity.setCreateTime(LocalDateTime.now());
        responseMessageEntity.setUpdateTime(LocalDateTime.now());
        chatMessageRepository.save(responseMessageEntity);
    }

    private Map<String, Object> buildGenerationMetadata(QuestionGenerateRequestDTO request,
                                                        QuestionGenerationMode generationMode) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("generationMode", generationMode != null ? generationMode.getCode() : QuestionGenerationMode.QUESTION.getCode());
        if (request != null && StringUtils.hasText(request.getPaperName())) {
            metadata.put("paperName", request.getPaperName().trim());
        }
        return metadata;
    }

    private ChatMessage findExistingResponseMessage(UUID sessionId, String requestId) {
        if (sessionId == null || !StringUtils.hasText(requestId)) {
            return null;
        }
        return chatMessageRepository.findFirstBySessionIdAndRoleAndRequestId(
                sessionId,
                ChatRoleEnum.QUESTION_GENERATOR.getCode(),
                requestId
        );
    }

    private void acknowledgeSafely(Acknowledgment acknowledgment, String requestId) {
        if (acknowledgment == null) {
            return;
        }
        try {
            acknowledgment.acknowledge();
        } catch (CommitFailedException e) {
            log.warn("Kafka ack failed after question generation. requestId={}, error={}", requestId, e.getMessage());
        } catch (Exception e) {
            log.warn("Unexpected Kafka ack failure after question generation. requestId={}", requestId, e);
        }
    }

    private UUID getOrCreateSessionId(QuestionGenerateRequestDTO request, UUID requestUserId) {
        if (request.getSessionId() != null) {
            return request.getSessionId();
        }
        if (requestUserId == null) {
            throw new BusinessException(AIChatEnum.SESSION_USER_ID_REQUIRED);
        }
        ChatSessionVO sessionVO = chatSessionService.addChatSession(
                requestUserId,
                request.getCourseId(),
                SessionTypeEnum.SMART_QUESTION.getCode(),
                QuestionConstants.SMART_QUESTION_SESSION_TITLE
        );
        UUID sessionId = sessionVO.getId();
        request.setSessionId(sessionId);
        return sessionId;
    }

    private String buildRequestSummary(QuestionGenerateRequestDTO request, QuestionGenerationMode generationMode) {
        StringBuilder sb = new StringBuilder(generationMode != null && generationMode.isPaper() ? "天枢出卷：" : "天枢出题：");
        sb.append("数量=").append(request.getQuestionCount());
        sb.append("，题型=").append(request.getQuestionType());
        sb.append("，难度=").append(request.getDifficulty());
        if (request.getCourseId() != null) {
            sb.append("，课程=").append(request.getCourseId());
        }
        if (request.getQuestionBankId() != null) {
            sb.append("，题库=").append(request.getQuestionBankId());
        }
        if (generationMode != null && generationMode.isPaper()) {
            if (StringUtils.hasText(request.getPaperName())) {
                sb.append("，试卷名称=").append(request.getPaperName().trim());
            }
            if (request.getTotalScore() != null) {
                sb.append("，总分=").append(request.getTotalScore());
            }
            if (request.getTotalEstimatedTime() != null) {
                sb.append("，总时长=").append(request.getTotalEstimatedTime()).append("分钟");
            }
        }
        if (StringUtils.hasText(request.getRequirement())) {
            sb.append("，要求=").append(request.getRequirement());
        }
        if (!CollectionUtils.isEmpty(request.getFileReferences())) {
            sb.append("，文件数=").append(request.getFileReferences().size());
        }
        return sb.toString();
    }
}
