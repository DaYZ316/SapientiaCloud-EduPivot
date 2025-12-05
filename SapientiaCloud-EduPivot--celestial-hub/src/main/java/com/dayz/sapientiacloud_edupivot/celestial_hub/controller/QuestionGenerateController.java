package com.dayz.sapientiacloud_edupivot.celestial_hub.controller;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.QuestionConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.ChatSessionVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.SessionTypeEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.ChatMessageRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.IChatSessionService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.KafkaQuestionService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.ChatMessageUtil;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * AI 出题控制器（基于 Kafka 的出题请求）
 */
@Slf4j
@Tag(name = "AI出题", description = "通过AI自动生成题目")
@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionGenerateController extends BaseController {

    private final KafkaQuestionService kafkaQuestionService;
    private final ChatMessageRepository chatMessageRepository;
    private final IChatSessionService chatSessionService;

    /**
     * 通过 AI 生成题目
     * 注意：提交表单中不需要 courseId 与 questionBankId，也不需要 sysUserId，仅需可选的 sessionId 以及出题要求相关参数。
     */
    @HasPermission(
            summary = "generateQuestions",
            description = "通过AI自动生成题目（Kafka转发）",
            permission = PermissionConstants.CELESTIAL_ADD
    )
    @PostMapping("/generate")
    public Result<List<QuestionResponseDTO>> generateQuestions(
            @Valid @RequestBody QuestionGenerateRequestDTO request) {
        // 生成一次 requestId，并在重试过程中始终复用，方便端到端链路追踪与幂等处理
        String requestId = UUID.randomUUID().toString();
        
        // 在发出题目请求之前，检查会话消息数量（参考 kafkaChatStream 的逻辑）
        boolean needGenerateTitle = false;
        UUID sessionId = request.getSessionId();
        if (sessionId != null) {
            try {
                long messageCountBefore = chatMessageRepository.countBySessionId(sessionId);
                // 如果会话中没有消息，标记需要生成标题（在出题完成后）
                if (messageCountBefore == 0) {
                    needGenerateTitle = true;
                }
            } catch (Exception e) {
                log.debug("检查会话消息数量失败: sessionId={}, error={}", sessionId, e.getMessage());
            }
        }
        
        // 最多尝试 3 次（含第一次），一旦生成了非空结果就立即返回
        List<QuestionResponseDTO> questions = null;
        int maxRetry = 3;
        for (int i = 0; i < maxRetry; i++) {
            questions = kafkaQuestionService.generateQuestions(request, requestId);
            if (questions != null && !questions.isEmpty()) {
                break;
            }
        }
        
        // 如果3次重试后仍然失败，存入系统角色的消息（类似对话超时）
        if (questions == null || questions.isEmpty()) {
            try {
                UUID finalSessionId = getOrCreateSessionId(request);
                String errorMessage = "题目生成失败，已重试3次，请稍后重试。";
                ChatMessageUtil.saveSystemMessage(finalSessionId, errorMessage, requestId, chatMessageRepository);
            } catch (Exception e) {
                log.debug("保存题目生成失败的系统消息时出错, requestId: {}", requestId, e);
            }
        }
        
        // 发出题目请求后，如果标记了需要生成标题，那么异步请求对话标题生成功能
        // 参考 kafkaChatStream 的逻辑：在请求发送前检查消息数量为0，在响应完成后生成标题
        UUID finalSessionId = request.getSessionId();
        if (finalSessionId != null && needGenerateTitle) {
            try {
                // 再次检查消息数量，确保只有本次出题产生的消息（<=2条：出题请求+出题完成）
                long messageCountAfter = chatMessageRepository.countBySessionId(finalSessionId);
                // 如果消息数量<=2，说明只有本次出题产生的消息，可以生成标题
                if (messageCountAfter <= 2) {
                    chatSessionService.generateSessionTitleAsync(finalSessionId);
                }
            } catch (Exception e) {
                log.debug("检查会话消息数量或生成标题失败: sessionId={}, error={}", finalSessionId, e.getMessage());
            }
        }
        
        return Result.success(questions == null ? List.of() : questions);
    }

    /**
     * TODO 此接口有问题，待修复
     * 检查 Kafka 请求状态
     *
     * @param requestId 请求ID
     * @return true：已完成（或未找到），false：进行中
     */
    @HasPermission(
            summary = "checkKafkaRequest",
            description = "通过requestId查看Kafka请求状态",
            permission = PermissionConstants.CELESTIAL_QUERY
    )
    @GetMapping("/status/{requestId}")
    public Result<Boolean> checkKafkaRequest(
            @Parameter(name = "requestId", description = "请求ID", required = true)
            @PathVariable("requestId") String requestId) {
        boolean isCompleted = kafkaQuestionService.checkRequestStatus(requestId);
        return Result.success(isCompleted);
    }

    /**
     * 获取或创建会话ID：
     * - 如果请求中已携带 sessionId，则直接使用；
     * - 如果为空，则通过会话服务创建一个新的会话，并返回其 ID。
     */
    private UUID getOrCreateSessionId(QuestionGenerateRequestDTO request) {
        if (request.getSessionId() != null) {
            return request.getSessionId();
        }
        // 出题场景下，必须有 userId 才能创建会话
        UUID userId = UserContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("无法获取当前用户ID，无法创建会话");
        }
        ChatSessionVO sessionVO = chatSessionService.addChatSession(
                userId,
                null,
                SessionTypeEnum.SMART_QUESTION.getCode(),
                QuestionConstants.SMART_QUESTION_SESSION_TITLE
        );
        UUID sessionId = sessionVO.getId();
        request.setSessionId(sessionId);
        return sessionId;
    }
}


