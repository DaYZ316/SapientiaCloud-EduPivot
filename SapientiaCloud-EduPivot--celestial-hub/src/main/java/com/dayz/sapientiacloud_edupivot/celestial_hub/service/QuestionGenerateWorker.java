package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.alibaba.fastjson2.JSON;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.AIChatConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.*;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatMessage;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.ChatSessionVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.AIChatEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.ChatRoleEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.SessionTypeEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.ChatMessageRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.ChatMessageUtil;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI 出题 Worker：
 * 从 Kafka 接收出题请求，调用大模型生成题目 JSON，
 * 同时将请求与结果写入 AI 对话记录。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionGenerateWorker {

    private final ChatClient chatClient;
    private final KafkaQuestionService kafkaQuestionService;
    private final ChatMessageRepository chatMessageRepository;
    private final IChatSessionService chatSessionService;

    @Value("${spring.kafka.topic.question-request:question-request-topic}")
    private String questionRequestTopic;

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
            log.warn("收到空的出题请求, requestId={}", finalRequestId);
            return;
        }

        try {
            log.info("开始处理AI出题请求, requestId={}", finalRequestId);
            // 1. 获取或创建会话 ID（如果 sessionId 为空，则通过会话服务创建新会话）
            UUID sessionId = getOrCreateSessionId(request, requestUserId);

            // 2. 记录出题请求 ChatMessage（幂等，避免 Kafka 重试时重复写入）
            ChatMessage existingRequestMessage = chatMessageRepository.findFirstBySessionIdAndRoleAndRequestId(
                    sessionId, ChatRoleEnum.QUESTION_REQUESTER.getCode(), finalRequestId);
            if (existingRequestMessage == null) {
                ChatMessage requestMessageEntity = new ChatMessage();
                requestMessageEntity.setId(UUID.randomUUID());
                requestMessageEntity.setSessionId(sessionId);
                requestMessageEntity.setRole(ChatRoleEnum.QUESTION_REQUESTER.getCode());
                requestMessageEntity.setContent(buildRequestSummary(request));
                requestMessageEntity.setMessageType(AIChatConstants.MESSAGE_TYPE_TEXT);
                requestMessageEntity.setQuestionRequest(JSON.toJSONString(request));
                // 统一补充元信息，避免 MongoDB 中字段为空
                requestMessageEntity.setModelName(AIChatConstants.MODEL_QWEN3_MAX);
                requestMessageEntity.setTokenCount(ChatMessageUtil.estimateTokens(requestMessageEntity.getContent()));
                requestMessageEntity.setIsFeedback(AIChatConstants.FEEDBACK_NONE);
                requestMessageEntity.setRequestId(finalRequestId);
                requestMessageEntity.setCreateTime(LocalDateTime.now());
                requestMessageEntity.setUpdateTime(LocalDateTime.now());
                chatMessageRepository.save(requestMessageEntity);
            } else {
                log.debug("请求消息已存在，跳过重复存储, requestId={}", finalRequestId);
            }

            // 3. 调用大模型生成题目结构化结果
            List<QuestionResponseDTO> questions = callModelForQuestions(request, requestUserId, finalRequestId);

            // 4. 记录出题结果 ChatMessage
            ChatMessage responseMessageEntity = new ChatMessage();
            responseMessageEntity.setId(UUID.randomUUID());
            responseMessageEntity.setSessionId(sessionId);
            responseMessageEntity.setRole(ChatRoleEnum.QUESTION_GENERATOR.getCode());
            responseMessageEntity.setContent("AI出题完成，共生成 " + (questions != null ? questions.size() : 0) + " 道题目：");
            responseMessageEntity.setMessageType(AIChatConstants.MESSAGE_TYPE_TEXT);
            responseMessageEntity.setQuestionResponse(JSON.toJSONString(questions));
            // 结果消息也补充模型与 token 信息
            responseMessageEntity.setModelName(AIChatConstants.MODEL_QWEN3_MAX);
            responseMessageEntity.setTokenCount(ChatMessageUtil.estimateTokens(responseMessageEntity.getContent()));
            responseMessageEntity.setIsFeedback(AIChatConstants.FEEDBACK_NONE);
            responseMessageEntity.setRequestId(finalRequestId);
            responseMessageEntity.setCreateTime(LocalDateTime.now());
            responseMessageEntity.setUpdateTime(LocalDateTime.now());
            chatMessageRepository.save(responseMessageEntity);

            // 5. 回写 Kafka 响应
            kafkaQuestionService.sendQuestionResponse(finalRequestId,
                    JSON.toJSONString(questions));


            // 处理成功，提交 Kafka offset，防止服务重启后重复消费
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error("处理AI出题请求失败, requestId={}", finalRequestId, e);
            // 出错时返回空数组，避免调用方一直等待
            kafkaQuestionService.sendQuestionResponse(finalRequestId, "[]");
            // 出现异常时不确认 offset，让 Kafka 进行重试
        }
    }

    private List<QuestionResponseDTO> callModelForQuestions(QuestionGenerateRequestDTO request, UUID requestUserId, String requestId) {
        // ==================== 前置处理：混合出题 & 随机难度 ====================
        // questionType = 5 表示“混合出题”，这里等价于“随机题型”，交给大模型自行决定题型；
        // difficulty = 0 表示“随机难度”，这里同样交给大模型在 1~3 内自由分配。
        // 为了便于模型理解，我们在 system prompt 中显式说明这两个取值的语义。

        String systemPrompt = """
                你是一名专业的出题老师，请根据用户提供的知识点、难度和数量要求，生成一组结构化的题目数据。
                                
                请严格按照下列规则生成题目对象（QuestionGenerateRecord），特别注意 options 与 answers 的使用方式：
                                
                1. 通用字段：
                   - questionTitle: 题目标题
                   - questionContent: 题目内容（可包含公式）
                   - questionType: 题目类型 (0=单选题, 1=多选题, 2=判断题, 3=填空题, 4=简答题)
                     说明：当用户请求中的 questionType=5 时，表示“混合出题/随机题型”，
                          你可以在 0~4 之间合理分配不同题型，生成多种题型的题目。
                   - difficulty: 难度 (1=简单, 2=中等, 3=困难)
                     说明：当用户请求中的 difficulty=0 时，表示“随机难度”，
                          你可以为不同题目设置 1~3 的不同难度，而不是全部相同。
                   - score: 分值
                   - estimatedTime: 预计作答时间（分钟）
                   - tags: 标签数组
                                
                2. 当 questionType 为 0 单选、1 多选、2 判断 时：
                   - 只使用 options 字段，生成若干选项对象；
                   - 每个选项包含：optionContent, optionLabel, isCorrect, score, imageUrls, explanation；
                   - answers 字段在这种题型下【不要使用】，置为空数组或省略。
                                
                3. 当 questionType 为 3 填空、4 简答 时：
                   - 只使用 answers 字段，生成若干答案对象；
                   - 每个答案包含：answerContent, explanation, score, sortOrder；
                   - options 字段在这种题型下【不要使用】，置为空数组或省略。
                                
                4. 请不要混用：
                   - 选择题、判断题时不要生成 answers；
                   - 填空题、简答题时不要生成 options。

                5. 当用户请求为混合出题（questionType=5）或随机难度（difficulty=0）时：
                   - 题目之间的 questionType 和/或 difficulty 可以不同；
                   - 但每一道题内部仍需满足上述关于 options / answers 的约束；
                   - 请保证生成的题目数量等于用户要求的 questionCount。
                """;

        String userPrompt = JSON.toJSONString(request);

        List<QuestionGenerateRecord> records = chatClient
                .prompt(new Prompt(systemPrompt + "\n用户出题要求(JSON)：\n" + userPrompt))
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });

        if (records == null) {
            return List.of();
        }

        // 用户ID从 Kafka 请求中透传，HTTP 侧已做未登录校验，这里不再回退读取 SecurityContext
        UUID finalUserId = requestUserId;

        // 将 Record 转为最终的 QuestionResponseDTO，填充生成的题目ID / 选项ID / 答案ID 与当前用户ID
        return records.stream().map(r -> {
            QuestionResponseDTO dto = new QuestionResponseDTO();
            UUID questionId = UuidCreator.getTimeOrderedEpoch();
            dto.setId(questionId);
            dto.setSysUserId(finalUserId);
            dto.setRequestId(requestId);
            dto.setQuestionTitle(r.questionTitle());
            dto.setQuestionContent(r.questionContent());
            dto.setQuestionType(r.questionType());
            dto.setDifficulty(r.difficulty());
            dto.setScore(r.score());
            dto.setEstimatedTime(r.estimatedTime());
            dto.setTags(r.tags());

            // 为每个选项生成唯一ID，并绑定题目ID
            if (r.options() != null && !r.options().isEmpty()) {
                List<QuestionOptionSimpleDTO> optionList = new ArrayList<>(r.options().size());
                for (QuestionOptionSimpleDTO opt : r.options()) {
                    if (opt == null) {
                        continue;
                    }
                    QuestionOptionSimpleDTO copy = new QuestionOptionSimpleDTO();
                    copy.setId(UuidCreator.getTimeOrderedEpoch());
                    copy.setQuestionId(questionId);
                    copy.setOptionContent(opt.getOptionContent());
                    copy.setOptionLabel(opt.getOptionLabel());
                    copy.setIsCorrect(opt.getIsCorrect());
                    copy.setScore(opt.getScore());
                    copy.setImageUrls(opt.getImageUrls());
                    copy.setExplanation(opt.getExplanation());
                    optionList.add(copy);
                }
                dto.setOptions(optionList);
            } else {
                dto.setOptions(null);
            }

            // 为每个答案生成唯一ID，并绑定题目ID
            if (r.answers() != null && !r.answers().isEmpty()) {
                List<QuestionAnswerSimpleDTO> answerList = new ArrayList<>(r.answers().size());
                for (QuestionAnswerSimpleDTO ans : r.answers()) {
                    if (ans == null) {
                        continue;
                    }
                    QuestionAnswerSimpleDTO copy = new QuestionAnswerSimpleDTO();
                    copy.setId(UuidCreator.getTimeOrderedEpoch());
                    copy.setQuestionId(questionId);
                    copy.setAnswerContent(ans.getAnswerContent());
                    copy.setExplanation(ans.getExplanation());
                    copy.setScore(ans.getScore());
                    copy.setSortOrder(ans.getSortOrder());
                    answerList.add(copy);
                }
                dto.setAnswers(answerList);
            } else {
                dto.setAnswers(null);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    /**
     * 获取或创建会话ID：
     * - 如果请求中已携带 sessionId，则直接使用；
     * - 如果为空，则通过会话服务创建一个新的会话，并返回其 ID。
     */
    private UUID getOrCreateSessionId(QuestionGenerateRequestDTO request, UUID requestUserId) {
        if (request.getSessionId() != null) {
            return request.getSessionId();
        }
        // 出题场景下，必须有 userId 才能创建会话
        if (requestUserId == null) {
            throw new BusinessException(AIChatEnum.SESSION_USER_ID_REQUIRED);
        }
        ChatSessionVO sessionVO = chatSessionService.addChatSession(
                requestUserId,
                null,
                SessionTypeEnum.SMART_QUESTION.getCode(),
                "智能出题"
        );
        UUID sessionId = sessionVO.getId();
        request.setSessionId(sessionId);
        return sessionId;
    }

    private String buildRequestSummary(QuestionGenerateRequestDTO request) {
        StringBuilder sb = new StringBuilder("用户发起AI出题请求：");
        sb.append("数量=").append(request.getQuestionCount());
        sb.append("，题型=").append(request.getQuestionType());
        sb.append("，难度=").append(request.getDifficulty());
        if (request.getRequirement() != null) {
            sb.append("，要求=").append(request.getRequirement());
        }
        return sb.toString();
    }
}


