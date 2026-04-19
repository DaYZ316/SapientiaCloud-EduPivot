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
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.SessionTypeEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.ChatMessageRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.QuestionAgentOrchestrator;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.agent.context.QuestionGenerationAggregate;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.ChatMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Kafka worker for AI question generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionGenerateWorker {

    private final QuestionAgentOrchestrator questionAgentOrchestrator;
    private final KafkaQuestionService kafkaQuestionService;
    private final ChatMessageRepository chatMessageRepository;
    private final IChatSessionService chatSessionService;

    @KafkaListener(topics = "${spring.kafka.topic.question-request:question-request-topic}",
            groupId = "${spring.kafka.consumer.group-id:chat-group}-question-worker")
    public void consumeQuestionRequest(@Payload String message,
                                       @Header(KafkaHeaders.RECEIVED_KEY) String requestId,
                                       Acknowledgment acknowledgment) {
        KafkaQuestionService.QuestionRequestMessage requestMessage =
                JSON.parseObject(message, KafkaQuestionService.QuestionRequestMessage.class);
        String finalRequestId = Optional.ofNullable(requestMessage.getRequestId()).orElse(requestId);
        QuestionGenerateRequestDTO request = requestMessage.getRequest();
        UUID requestUserId = requestMessage.getUserId();
        if (request == null) {
            return;
        }

        try {
            UUID sessionId = getOrCreateSessionId(request, requestUserId);
            ChatMessage existingResponseMessage = findExistingResponseMessage(sessionId, finalRequestId);
            if (existingResponseMessage != null && StringUtils.hasText(existingResponseMessage.getQuestionResponse())) {
                log.info("Question request already processed. requestId={}, sessionId={}", finalRequestId, sessionId);
                kafkaQuestionService.sendQuestionResponse(finalRequestId, existingResponseMessage.getQuestionResponse());
                acknowledgeSafely(acknowledgment, finalRequestId);
                return;
            }

            persistRequestMessage(sessionId, request, finalRequestId);

            QuestionGenerationAggregate aggregate = questionAgentOrchestrator.run(request, requestUserId, finalRequestId);
            List<QuestionResponseDTO> questions = aggregate != null && !CollectionUtils.isEmpty(aggregate.getFinalQuestions())
                    ? aggregate.getFinalQuestions()
                    : List.of();

            persistResponseMessage(sessionId, finalRequestId, questions);
            kafkaQuestionService.sendQuestionResponse(finalRequestId, JSON.toJSONString(questions));
            acknowledgeSafely(acknowledgment, finalRequestId);
        } catch (Exception e) {
            log.error("Question generation worker failed. requestId={}, error={}", finalRequestId, e.getMessage(), e);
            kafkaQuestionService.sendQuestionResponse(finalRequestId, "[]");
            acknowledgeSafely(acknowledgment, finalRequestId);
        }
    }

    private void persistRequestMessage(UUID sessionId, QuestionGenerateRequestDTO request, String requestId) {
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
        requestMessageEntity.setContent(buildRequestSummary(request));
        requestMessageEntity.setMessageType(AIChatConstants.MESSAGE_TYPE_TEXT);
        requestMessageEntity.setQuestionRequest(JSON.toJSONString(request));
        requestMessageEntity.setModelName(AIChatConstants.MODEL_QWEN3_MAX);
        requestMessageEntity.setTokenCount(ChatMessageUtil.estimateTokens(requestMessageEntity.getContent()));
        requestMessageEntity.setIsFeedback(AIChatConstants.FEEDBACK_NONE);
        requestMessageEntity.setRequestId(requestId);
        requestMessageEntity.setCreateTime(LocalDateTime.now());
        requestMessageEntity.setUpdateTime(LocalDateTime.now());
        chatMessageRepository.save(requestMessageEntity);
    }

    private void persistResponseMessage(UUID sessionId, String requestId, List<QuestionResponseDTO> questions) {
        ChatMessage responseMessageEntity = new ChatMessage();
        responseMessageEntity.setId(UUID.randomUUID());
        responseMessageEntity.setSessionId(sessionId);
        responseMessageEntity.setRole(ChatRoleEnum.QUESTION_GENERATOR.getCode());
        responseMessageEntity.setContent(String.format(QuestionConstants.QUESTION_COMPLETE_TEMPLATE, questions.size()));
        responseMessageEntity.setMessageType(AIChatConstants.MESSAGE_TYPE_TEXT);
        responseMessageEntity.setQuestionResponse(JSON.toJSONString(questions));
        responseMessageEntity.setModelName(AIChatConstants.MODEL_QWEN3_MAX);
        responseMessageEntity.setTokenCount(ChatMessageUtil.estimateTokens(responseMessageEntity.getContent()));
        responseMessageEntity.setIsFeedback(AIChatConstants.FEEDBACK_NONE);
        responseMessageEntity.setRequestId(requestId);
        responseMessageEntity.setCreateTime(LocalDateTime.now());
        responseMessageEntity.setUpdateTime(LocalDateTime.now());
        chatMessageRepository.save(responseMessageEntity);
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

    private String buildRequestSummary(QuestionGenerateRequestDTO request) {
        StringBuilder sb = new StringBuilder(QuestionConstants.QUESTION_REQUEST_PREFIX);
        sb.append("数量=").append(request.getQuestionCount());
        sb.append("，题型=").append(request.getQuestionType());
        sb.append("，难度=").append(request.getDifficulty());
        if (request.getCourseId() != null) {
            sb.append("，课程=").append(request.getCourseId());
        }
        if (request.getQuestionBankId() != null) {
            sb.append("，题库=").append(request.getQuestionBankId());
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
