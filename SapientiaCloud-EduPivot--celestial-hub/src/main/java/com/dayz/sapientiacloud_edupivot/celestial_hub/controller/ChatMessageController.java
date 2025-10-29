package com.dayz.sapientiacloud_edupivot.celestial_hub.controller;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.ChatRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.ChatMessage;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.ChatResponseVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.IChatMessageService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Tag(name = "AI对话消息管理", description = "用于管理AI对话消息的API")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class ChatMessageController extends BaseController {

    private final IChatMessageService chatMessageService;

    @HasPermission(
            summary = "chat",
            description = "发送消息给AI助手并获取回复",
            permission = PermissionConstants.CELESTIAL_ADD
    )
    @PostMapping("/send")
    public Result<ChatResponseVO> chat(@Valid @RequestBody ChatRequestDTO request) {
        ChatResponseVO response = chatMessageService.chat(request);
        return Result.success(response);
    }

    @HasPermission(
            summary = "chatStream",
            description = "发送消息并以流式方式接收AI回复",
            permission = PermissionConstants.CELESTIAL_ADD
    )
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@Valid @RequestBody ChatRequestDTO request) {
        return chatMessageService.chatStream(request);
    }

    @HasPermission(
            summary = "listMessagesBySessionId",
            description = "获取指定会话的消息列表",
            permission = PermissionConstants.CELESTIAL_QUERY
    )
    @GetMapping("/session/{sessionId}")
    public Result<List<ChatMessage>> listMessagesBySessionId(
            @Parameter(name = "sessionId", description = "会话ID", required = true) @PathVariable("sessionId") UUID sessionId,
            @Parameter(name = "limit", description = "限制数量") @RequestParam(value = "limit", required = false) Integer limit
    ) {
        List<ChatMessage> messages = chatMessageService.listMessagesBySessionId(sessionId, limit);
        return Result.success(messages);
    }

    @HasPermission(
            summary = "getChatMessageById",
            description = "通过消息的唯一ID获取其详细信息。",
            permission = PermissionConstants.CELESTIAL_QUERY
    )
    @GetMapping("/{id}")
    public Result<ChatMessage> getChatMessageById(
            @Parameter(name = "id", description = "消息ID", required = true) @PathVariable("id") UUID id
    ) {
        ChatMessage chatMessage = chatMessageService.getChatMessageById(id);
        return Result.success(chatMessage);
    }

    @HasPermission(
            summary = "feedbackMessage",
            description = "对AI回复进行反馈（有用/无用）",
            permission = PermissionConstants.CELESTIAL_EDIT
    )
    @PostMapping("/{id}/feedback")
    public Result<Boolean> feedbackMessage(
            @Parameter(name = "id", description = "消息ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "feedback", description = "反馈类型: 1-有用, -1-无用", required = true) @RequestParam("feedback") Integer feedback
    ) {
        Boolean result = chatMessageService.feedbackMessage(id, feedback);
        return Result.success(result);
    }
}

