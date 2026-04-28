package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.alibaba.fastjson2.JSON;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.AIChatConstants;
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
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.dto.QuestionGenerationTraceEntryDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.ChatMessageUtil;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.QuestionGenerationLocaleUtils;
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
            sendProgress(
                    fallbackRequestId,
                    null,
                    QuestionGenerationMode.QUESTION,
                    QuestionAgentStage.FAILED,
                    QuestionGenerationLocaleUtils.text(QuestionGenerationLocaleUtils.LOCALE_ZH_CN, QUESTION_FAILURE_MESSAGE, "Question generation failed")
            );
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
            sendProgress(
                    finalRequestId,
                    null,
                    QuestionGenerationMode.QUESTION,
                    QuestionAgentStage.FAILED,
                    QuestionGenerationLocaleUtils.text(QuestionGenerationLocaleUtils.LOCALE_ZH_CN, QUESTION_FAILURE_MESSAGE, "Question generation failed")
            );
            kafkaQuestionService.sendQuestionResponse(finalRequestId, "[]");
            acknowledgeSafely(acknowledgment, finalRequestId);
            return;
        }

        String locale = QuestionGenerationLocaleUtils.resolveLocale(request);
        QuestionGenerationMode generationMode = QuestionGenerationMode.resolve(request);
        request.setGenerationMode(generationMode.getCode());

        UUID resolvedSessionId = request.getSessionId();
        AtomicLong lastStageProgressAt = new AtomicLong(0L);
        try {
            UUID sessionId = getOrCreateSessionId(request, requestUserId);
            resolvedSessionId = sessionId;
            sendProgress(finalRequestId, sessionId, generationMode, null, resolveWarmupMessage(generationMode, locale));

            ChatMessage existingResponseMessage = findExistingResponseMessage(sessionId, finalRequestId);
            if (existingResponseMessage != null && StringUtils.hasText(existingResponseMessage.getQuestionResponse())) {
                log.info("Question request already processed. requestId={}, sessionId={}", finalRequestId, sessionId);
                sendStageProgress(
                        finalRequestId,
                        sessionId,
                        generationMode,
                        QuestionAgentStage.RESPONDED,
                        resolveResultReplayMessage(generationMode, locale),
                        lastStageProgressAt
                );
                kafkaQuestionService.sendQuestionResponse(finalRequestId, existingResponseMessage.getQuestionResponse());
                acknowledgeSafely(acknowledgment, finalRequestId);
                return;
            }

            persistRequestMessage(sessionId, request, finalRequestId, generationMode);
            sendProgress(finalRequestId, sessionId, generationMode, null, resolvePersistenceMessage(generationMode, locale));

            QuestionGenerationAggregate aggregate = questionAgentOrchestrator.run(
                    request,
                    requestUserId,
                    finalRequestId,
                    stage -> sendStageProgress(
                            finalRequestId,
                            sessionId,
                            generationMode,
                            stage,
                            resolveStageMessage(stage, generationMode, locale),
                            lastStageProgressAt
                    ),
                    traceEntry -> sendTraceProgress(
                            finalRequestId,
                            sessionId,
                            generationMode,
                            traceEntry
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
                    resolveResultPersistenceMessage(generationMode, locale),
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
                    resolveFailureMessage(generationMode, locale),
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
        sendProgress(requestId, sessionId, generationMode, stage, message, null);
    }

    private void sendTraceProgress(String requestId,
                                   UUID sessionId,
                                   QuestionGenerationMode generationMode,
                                   QuestionGenerationTraceEntryDTO traceEntry) {
        if (traceEntry == null) {
            return;
        }
        QuestionAgentStage stage = null;
        if (StringUtils.hasText(traceEntry.getStage())) {
            try {
                stage = QuestionAgentStage.valueOf(traceEntry.getStage());
            } catch (IllegalArgumentException ignored) {
                stage = null;
            }
        }
        sendProgress(requestId, sessionId, generationMode, stage, null, traceEntry);
    }

    private void sendProgress(String requestId,
                              UUID sessionId,
                              QuestionGenerationMode generationMode,
                              QuestionAgentStage stage,
                              String message,
                              QuestionGenerationTraceEntryDTO traceEntry) {
        kafkaQuestionService.sendQuestionProgress(
                requestId,
                sessionId,
                "processing",
                stage != null ? stage.name() : null,
                message,
                null,
                generationMode != null ? generationMode.getCode() : QuestionGenerationMode.QUESTION.getCode(),
                traceEntry
        );
    }

    private String resolveStageMessage(QuestionAgentStage stage,
                                       QuestionGenerationMode generationMode,
                                       String locale) {
        if (stage == null) {
            return generationMode != null && generationMode.isPaper()
                    ? QuestionGenerationLocaleUtils.text(locale, "正在生成试卷中...", "Generating paper...")
                    : QuestionGenerationLocaleUtils.text(locale, QUESTION_IN_PROGRESS_MESSAGE, "Generating questions...");
        }
        if (stage == QuestionAgentStage.FAILED) {
            return resolveFailureMessage(generationMode, locale);
        }
        if (generationMode == null || !generationMode.isPaper()) {
            return QuestionGenerationLocaleUtils.text(locale, QUESTION_IN_PROGRESS_MESSAGE, "Generating questions...");
        }
        return switch (stage) {
            case RECEIVED -> QuestionGenerationLocaleUtils.text(locale, "正在接收出卷请求", "Receiving paper generation request");
            case CONTEXT_READY -> QuestionGenerationLocaleUtils.text(locale, "正在准备出卷上下文", "Preparing paper generation context");
            case PLANNED -> QuestionGenerationLocaleUtils.text(locale, "正在规划试卷结构", "Planning paper structure");
            case GENERATED -> QuestionGenerationLocaleUtils.text(locale, "正在生成题目草稿", "Generating question drafts");
            case VALIDATED -> QuestionGenerationLocaleUtils.text(locale, "正在校验题目质量", "Validating question quality");
            case REPAIRED -> QuestionGenerationLocaleUtils.text(locale, "正在修复题目问题", "Repairing question issues");
            case ASSEMBLED -> QuestionGenerationLocaleUtils.text(locale, "正在组装最终试卷", "Assembling final paper");
            case RESPONDED -> QuestionGenerationLocaleUtils.text(locale, "正在写入并返回结果", "Persisting and returning result");
            case FAILED -> resolveFailureMessage(generationMode, locale);
        };
    }

    private String resolveWarmupMessage(QuestionGenerationMode generationMode, String locale) {
        return generationMode != null && generationMode.isPaper()
                ? QuestionGenerationLocaleUtils.text(locale, "正在准备试卷生成任务", "Preparing paper generation task")
                : QuestionGenerationLocaleUtils.text(locale, QUESTION_IN_PROGRESS_MESSAGE, "Generating questions...");
    }

    private String resolvePersistenceMessage(QuestionGenerationMode generationMode, String locale) {
        return generationMode != null && generationMode.isPaper()
                ? QuestionGenerationLocaleUtils.text(locale, "正在保存出卷请求", "Saving paper generation request")
                : QuestionGenerationLocaleUtils.text(locale, QUESTION_IN_PROGRESS_MESSAGE, "Generating questions...");
    }

    private String resolveResultPersistenceMessage(QuestionGenerationMode generationMode, String locale) {
        return generationMode != null && generationMode.isPaper()
                ? QuestionGenerationLocaleUtils.text(locale, "正在返回生成结果", "Returning generated result")
                : QuestionGenerationLocaleUtils.text(locale, QUESTION_IN_PROGRESS_MESSAGE, "Generating questions...");
    }

    private String resolveResultReplayMessage(QuestionGenerationMode generationMode, String locale) {
        return generationMode != null && generationMode.isPaper()
                ? QuestionGenerationLocaleUtils.text(locale, "正在回传历史试卷结果", "Replaying historical paper result")
                : QuestionGenerationLocaleUtils.text(locale, QUESTION_IN_PROGRESS_MESSAGE, "Generating questions...");
    }

    private String resolveFailureMessage(QuestionGenerationMode generationMode, String locale) {
        return generationMode != null && generationMode.isPaper()
                ? QuestionGenerationLocaleUtils.text(locale, PAPER_FAILURE_MESSAGE, "Paper generation failed")
                : QuestionGenerationLocaleUtils.text(locale, QUESTION_FAILURE_MESSAGE, "Question generation failed");
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
        responseMessageEntity.setContent(QuestionGenerationLocaleUtils.resolveResponseSummary(
                request != null ? request.getLocale() : null,
                generationMode,
                questions.size()
        ));
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
        metadata.put("locale", QuestionGenerationLocaleUtils.resolveLocale(request));
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
                QuestionGenerationLocaleUtils.resolveSessionTitle(
                        request != null ? request.getLocale() : null,
                        QuestionGenerationMode.resolve(request)
                )
        );
        UUID sessionId = sessionVO.getId();
        request.setSessionId(sessionId);
        return sessionId;
    }

    private String buildRequestSummary(QuestionGenerateRequestDTO request, QuestionGenerationMode generationMode) {
        String locale = QuestionGenerationLocaleUtils.resolveLocale(request);
        boolean isPaper = generationMode != null && generationMode.isPaper();
        StringBuilder sb = new StringBuilder(QuestionGenerationLocaleUtils.text(
                locale,
                isPaper ? "天枢出卷：" : "天枢出题：",
                isPaper ? "AI paper generation: " : "AI question generation: "
        ));
        sb.append(QuestionGenerationLocaleUtils.text(locale, "数量=", "count=")).append(request.getQuestionCount());
        sb.append(QuestionGenerationLocaleUtils.text(locale, "，题型=", ", type=")).append(request.getQuestionType());
        sb.append(QuestionGenerationLocaleUtils.text(locale, "，难度=", ", difficulty=")).append(request.getDifficulty());
        if (request.getCourseId() != null) {
            sb.append(QuestionGenerationLocaleUtils.text(locale, "，课程=", ", course=")).append(request.getCourseId());
        }
        if (request.getQuestionBankId() != null) {
            sb.append(QuestionGenerationLocaleUtils.text(locale, "，题库=", ", questionBank=")).append(request.getQuestionBankId());
        }
        if (isPaper) {
            if (StringUtils.hasText(request.getPaperName())) {
                sb.append(QuestionGenerationLocaleUtils.text(locale, "，试卷名称=", ", paperName=")).append(request.getPaperName().trim());
            }
            if (request.getTotalScore() != null) {
                sb.append(QuestionGenerationLocaleUtils.text(locale, "，总分=", ", totalScore=")).append(request.getTotalScore());
            }
            if (request.getTotalEstimatedTime() != null) {
                sb.append(QuestionGenerationLocaleUtils.text(locale, "，总时长=", ", totalTime="))
                        .append(request.getTotalEstimatedTime())
                        .append(QuestionGenerationLocaleUtils.text(locale, "分钟", " min"));
            }
        }
        if (StringUtils.hasText(request.getRequirement())) {
            sb.append(QuestionGenerationLocaleUtils.text(locale, "，要求=", ", requirement=")).append(request.getRequirement());
        }
        if (!CollectionUtils.isEmpty(request.getFileReferences())) {
            sb.append(QuestionGenerationLocaleUtils.text(locale, "，文件数=", ", fileCount=")).append(request.getFileReferences().size());
        }
        return sb.toString();
    }
}
