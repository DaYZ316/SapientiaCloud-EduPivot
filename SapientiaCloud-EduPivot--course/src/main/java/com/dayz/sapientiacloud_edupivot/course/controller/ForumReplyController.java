package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.ForumReplyDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.ForumReplyQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.ForumReplyVO;
import com.dayz.sapientiacloud_edupivot.course.service.IForumReplyService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "论坛回复管理", description = "用于管理论坛回复的API")
@RestController
@RequestMapping("/reply")
@RequiredArgsConstructor
public class ForumReplyController extends BaseController {

    private final IForumReplyService forumReplyService;

    @HasPermission(
            summary = "listForumReply",
            description = "根据传入的条件分页查询论坛回复信息。支持根据回复内容、作者、帖子ID等字段进行查询。",
            permission = PermissionConstants.REPLY_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listForumReply(@ParameterObject ForumReplyQueryDTO forumReplyQueryDTO) {
        startPage();
        PageInfo<ForumReplyVO> pageInfo = forumReplyService.listForumReply(forumReplyQueryDTO);
        return getDataTable(pageInfo);
    }

    @HasPermission(
            summary = "listAllForumReplyByPostId",
            description = "根据帖子ID获取该帖子下的所有回复列表。",
            permission = PermissionConstants.REPLY_QUERY
    )
    @GetMapping("/post/{postId}")
    public Result<List<ForumReplyVO>> listAllForumReplyByPostId(
            @Parameter(name = "postId", description = "帖子ID", required = true) @PathVariable("postId") UUID postId
    ) {
        List<ForumReplyVO> replyList = forumReplyService.listAllForumReplyByPostId(postId);
        return Result.success(replyList);
    }

    @HasPermission(
            summary = "listAllForumReplyByForumId",
            description = "根据论坛ID获取该论坛下的所有回复列表。",
            permission = PermissionConstants.REPLY_QUERY
    )
    @GetMapping("/forum/{forumId}")
    public Result<List<ForumReplyVO>> listAllForumReplyByForumId(
            @Parameter(name = "forumId", description = "论坛ID", required = true) @PathVariable("forumId") UUID forumId
    ) {
        List<ForumReplyVO> replyList = forumReplyService.listAllForumReplyByForumId(forumId);
        return Result.success(replyList);
    }

    @HasPermission(
            summary = "listAllForumReplyByCourseId",
            description = "根据课程ID获取该课程下的所有回复列表。",
            permission = PermissionConstants.REPLY_QUERY
    )
    @GetMapping("/course/{courseId}")
    public Result<List<ForumReplyVO>> listAllForumReplyByCourseId(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId
    ) {
        List<ForumReplyVO> replyList = forumReplyService.listAllForumReplyByCourseId(courseId);
        return Result.success(replyList);
    }

    @HasPermission(
            summary = "getForumReplyById",
            description = "通过回复的唯一ID获取其详细信息。",
            permission = PermissionConstants.REPLY_QUERY
    )
    @GetMapping("/{id}")
    public Result<ForumReplyVO> getForumReplyById(
            @Parameter(name = "id", description = "回复ID", required = true) @PathVariable("id") UUID id
    ) {
        ForumReplyVO forumReplyVO = forumReplyService.getForumReplyById(id);
        return Result.success(forumReplyVO);
    }

    @HasPermission(
            summary = "addForumReply",
            description = "在帖子中添加一个新回复。",
            permission = PermissionConstants.REPLY_ADD
    )
    @PostMapping
    public Result<ForumReplyVO> addForumReply(
            @RequestBody @Valid ForumReplyDTO forumReplyDTO
    ) {
        ForumReplyVO forumReplyVO = forumReplyService.addForumReply(forumReplyDTO);
        return Result.success(forumReplyVO);
    }

    @HasPermission(
            summary = "updateForumReply",
            description = "更新现有回复的信息。",
            permission = PermissionConstants.REPLY_EDIT
    )
    @PutMapping
    public Result<Boolean> updateForumReply(
            @RequestBody @Valid ForumReplyDTO forumReplyDTO
    ) {
        Boolean result = forumReplyService.updateForumReply(forumReplyDTO);
        return Result.success(result);
    }

    @HasPermission(
            summary = "removeForumReplyById",
            description = "通过回复的唯一ID删除回复。",
            permission = PermissionConstants.REPLY_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeForumReplyById(
            @Parameter(name = "id", description = "回复ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = forumReplyService.removeForumReplyById(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "removeForumReplyByIds",
            description = "根据回复ID列表批量删除回复。",
            permission = PermissionConstants.REPLY_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeForumReplyByIds(
            @RequestBody List<UUID> ids
    ) {
        Integer result = forumReplyService.removeForumReplyByIds(ids);
        return Result.success(result);
    }

    @HasPermission(
            summary = "updateReplyStatus",
            description = "更新回复状态（正常/删除/审核中/审核失败）。",
            permission = PermissionConstants.REPLY_EDIT
    )
    @PutMapping("/{id}/status")
    public Result<Boolean> updateReplyStatus(
            @Parameter(name = "id", description = "回复ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "status", description = "回复状态 (0=正常, 1=删除, 2=审核中, 3=审核失败)", required = true) @RequestParam("status") Integer status
    ) {
        Boolean result = forumReplyService.updateReplyStatus(id, status);
        return Result.success(result);
    }

    @HasPermission(
            summary = "likeReply",
            description = "点赞回复。",
            permission = PermissionConstants.REPLY_QUERY
    )
    @PostMapping("/{id}/like")
    public Result<Boolean> likeReply(
            @Parameter(name = "id", description = "回复ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = forumReplyService.likeReply(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "unlikeReply",
            description = "取消点赞回复。",
            permission = PermissionConstants.REPLY_QUERY
    )
    @DeleteMapping("/{id}/like")
    public Result<Boolean> unlikeReply(
            @Parameter(name = "id", description = "回复ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = forumReplyService.unlikeReply(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "acceptReply",
            description = "采纳回复（仅问答区有效）。",
            permission = PermissionConstants.REPLY_EDIT
    )
    @PutMapping("/{id}/accept")
    public Result<Boolean> acceptReply(
            @Parameter(name = "id", description = "回复ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = forumReplyService.acceptReply(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "unacceptReply",
            description = "取消采纳回复（仅问答区有效）。",
            permission = PermissionConstants.REPLY_EDIT
    )
    @PutMapping("/{id}/unaccept")
    public Result<Boolean> unacceptReply(
            @Parameter(name = "id", description = "回复ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = forumReplyService.unacceptReply(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "getReplyTree",
            description = "获取回复的树形结构（包含子回复）。",
            permission = PermissionConstants.REPLY_QUERY
    )
    @GetMapping("/{id}/tree")
    public Result<List<ForumReplyVO>> getReplyTree(
            @Parameter(name = "id", description = "父回复ID", required = true) @PathVariable("id") UUID id
    ) {
        List<ForumReplyVO> replyTree = forumReplyService.getReplyTree(id);
        return Result.success(replyTree);
    }

    @HasPermission(
            summary = "getAllRepliesByParentId",
            description = "获取父回复下的所有子回复（平铺的list形式）。",
            permission = PermissionConstants.REPLY_QUERY
    )
    @GetMapping("/{parentReplyId}/all")
    public Result<List<ForumReplyVO>> getAllRepliesByParentId(
            @Parameter(name = "parentReplyId", description = "父回复ID", required = true) @PathVariable("parentReplyId") UUID parentReplyId
    ) {
        List<ForumReplyVO> allReplies = forumReplyService.getAllRepliesByParentId(parentReplyId);
        return Result.success(allReplies);
    }

    @HasPermission(
            summary = "getReplyStatistics",
            description = "获取回复统计信息。",
            permission = PermissionConstants.REPLY_QUERY
    )
    @GetMapping("/{id}/statistics")
    public Result<ForumReplyVO> getReplyStatistics(
            @Parameter(name = "id", description = "回复ID", required = true) @PathVariable("id") UUID id
    ) {
        ForumReplyVO statistics = forumReplyService.getReplyStatistics(id);
        return Result.success(statistics);
    }
}
