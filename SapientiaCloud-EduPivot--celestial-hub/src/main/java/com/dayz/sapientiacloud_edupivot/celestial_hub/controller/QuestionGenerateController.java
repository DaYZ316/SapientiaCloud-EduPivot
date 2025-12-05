package com.dayz.sapientiacloud_edupivot.celestial_hub.controller;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.KafkaQuestionService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * AI 出题控制器（基于 Kafka 的出题请求）
 */
@Tag(name = "AI出题", description = "通过AI自动生成题目")
@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionGenerateController extends BaseController {

    private final KafkaQuestionService kafkaQuestionService;

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
        // 最多尝试 3 次（含第一次），一旦生成了非空结果就立即返回
        List<QuestionResponseDTO> questions = null;
        int maxRetry = 3;
        for (int i = 0; i < maxRetry; i++) {
            questions = kafkaQuestionService.generateQuestions(request, requestId);
            if (questions != null && !questions.isEmpty()) {
                break;
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
}


