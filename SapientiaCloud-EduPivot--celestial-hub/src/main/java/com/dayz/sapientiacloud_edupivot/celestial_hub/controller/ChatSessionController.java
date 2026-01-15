package com.dayz.sapientiacloud_edupivot.celestial_hub.controller;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.ChatSessionQueryDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.ChatSessionVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.IChatSessionService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "AI对话会话管理", description = "用于管理AI对话会话的API")
@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class ChatSessionController extends BaseController {

    private final IChatSessionService chatSessionService;

    @HasPermission(
            summary = "listChatSession",
            description = "根据传入的条件分页查询AI对话会话信息。支持根据用户ID、会话标题、会话类型等字段进行查询。",
            permission = PermissionConstants.CELESTIAL_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listChatSession(@ParameterObject ChatSessionQueryDTO chatSessionQueryDTO) {
        startPage();
        PageInfo<ChatSessionVO> pageInfo = chatSessionService.listChatSession(chatSessionQueryDTO);
        return getDataTable(pageInfo);
    }

    @HasPermission(
            summary = "listAllChatSessionByUserId",
            description = "根据用户ID获取该用户的所有会话列表。",
            permission = PermissionConstants.CELESTIAL_QUERY
    )
    @GetMapping("/user/{sysUserId}")
    public Result<List<ChatSessionVO>> listAllChatSessionByUserId(
            @Parameter(name = "sysUserId", description = "用户ID", required = true) @PathVariable("sysUserId") UUID sysUserId
    ) {
        List<ChatSessionVO> sessionList = chatSessionService.listAllChatSessionByUserId(sysUserId);
        return Result.success(sessionList);
    }

    @HasPermission(
            summary = "getUserSessions",
            description = "获取当前用户的对话会话列表。",
            permission = PermissionConstants.CELESTIAL_QUERY
    )
    @GetMapping("/my")
    public Result<List<ChatSessionVO>> getUserSessions() {
        UUID userId = UserContextUtil.getCurrentUserId();
        List<ChatSessionVO> sessionList = chatSessionService.listAllChatSessionByUserId(userId);
        return Result.success(sessionList);
    }

    @HasPermission(
            summary = "getChatSessionById",
            description = "通过会话的唯一ID获取其详细信息。",
            permission = PermissionConstants.CELESTIAL_QUERY
    )
    @GetMapping("/{id}")
    public Result<ChatSessionVO> getChatSessionById(
            @Parameter(name = "id", description = "会话ID", required = true) @PathVariable("id") UUID id
    ) {
        ChatSessionVO chatSessionVO = chatSessionService.getChatSessionById(id);
        return Result.success(chatSessionVO);
    }

    @HasPermission(
            summary = "addChatSession",
            description = "创建一个新的AI对话会话。",
            permission = PermissionConstants.CELESTIAL_ADD
    )
    @PostMapping
    public Result<ChatSessionVO> addChatSession(
            @Parameter(name = "courseId", description = "课程ID") @RequestParam(value = "courseId", required = false) UUID courseId,
            @Parameter(name = "sessionType", description = "会话类型") @RequestParam(value = "sessionType", required = false) Integer sessionType,
            @Parameter(name = "title", description = "会话标题") @RequestParam(value = "title", required = false) String title
    ) {
        ChatSessionVO chatSessionVO = chatSessionService.addChatSession(courseId, sessionType, title);
        return Result.success(chatSessionVO);
    }

    @HasPermission(
            summary = "updateChatSessionTitle",
            description = "修改会话的标题。",
            permission = PermissionConstants.CELESTIAL_EDIT
    )
    @PutMapping("/{id}/title")
    public Result<Boolean> updateChatSessionTitle(
            @Parameter(name = "id", description = "会话ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "title", description = "新标题", required = true) @RequestParam("title") String title
    ) {
        Boolean result = chatSessionService.updateChatSessionTitle(id, title);
        return Result.success(result);
    }

    @HasPermission(
            summary = "removeChatSessionById",
            description = "通过会话的唯一ID删除会话。",
            permission = PermissionConstants.CELESTIAL_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeChatSessionById(
            @Parameter(name = "id", description = "会话ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = chatSessionService.removeChatSessionById(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "removeChatSessionByIds",
            description = "根据会话ID列表批量删除会话。",
            permission = PermissionConstants.CELESTIAL_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeChatSessionByIds(
            @RequestBody List<UUID> ids
    ) {
        Integer result = chatSessionService.removeChatSessionByIds(ids);
        return Result.success(result);
    }

    @HasPermission(
            summary = "pinSession",
            description = "置顶或取消置顶会话。",
            permission = PermissionConstants.CELESTIAL_EDIT
    )
    @PutMapping("/{id}/pin")
    public Result<Boolean> pinSession(
            @Parameter(name = "id", description = "会话ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "isPinned", description = "是否置顶", required = true) @RequestParam("isPinned") Boolean isPinned
    ) {
        Boolean result = chatSessionService.pinSession(id, isPinned);
        return Result.success(result);
    }

    @HasPermission(
            summary = "favoriteSession",
            description = "收藏或取消收藏会话。",
            permission = PermissionConstants.CELESTIAL_EDIT
    )
    @PutMapping("/{id}/favorite")
    public Result<Boolean> favoriteSession(
            @Parameter(name = "id", description = "会话ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "isFavorite", description = "是否收藏", required = true) @RequestParam("isFavorite") Boolean isFavorite
    ) {
        Boolean result = chatSessionService.favoriteSession(id, isFavorite);
        return Result.success(result);
    }

    @HasPermission(
            summary = "archiveSession",
            description = "将指定会话归档。",
            permission = PermissionConstants.CELESTIAL_EDIT
    )
    @PutMapping("/{id}/archive")
    public Result<Boolean> archiveSession(
            @Parameter(name = "id", description = "会话ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = chatSessionService.archiveSession(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "generateSessionTitle",
            description = "根据对话内容使用AI生成会话标题。",
            permission = PermissionConstants.CELESTIAL_EDIT
    )
    @PostMapping("/{id}/generate-title")
    public Result<String> generateSessionTitle(
            @Parameter(name = "id", description = "会话ID", required = true) @PathVariable("id") UUID id
    ) {
        String title = chatSessionService.generateSessionTitle(id);
        return Result.success(title);
    }
}

