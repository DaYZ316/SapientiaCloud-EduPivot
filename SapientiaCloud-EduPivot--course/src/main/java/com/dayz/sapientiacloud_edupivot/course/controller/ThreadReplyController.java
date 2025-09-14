package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.ThreadReplyDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.ThreadReplyQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.ThreadReplyVO;
import com.dayz.sapientiacloud_edupivot.course.service.IThreadReplyService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "课程论坛回复管理", description = "用于管理课程论坛回复信息的API")
@RestController
@RequestMapping("/course-thread/reply")
@RequiredArgsConstructor
public class ThreadReplyController extends BaseController {

    private final IThreadReplyService threadReplyService;

    @HasPermission(
            summary = "listThreadReply",
            description = "根据传入的条件分页查询论坛回复信息。支持根据主贴ID、用户ID、父回复ID等字段进行查询。",
            permission = PermissionConstants.REPLY_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listThreadReply(@ParameterObject ThreadReplyQueryDTO threadReplyQueryDTO) {
        startPage();
        PageInfo<ThreadReplyVO> pageInfo = threadReplyService.listThreadReply(threadReplyQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "listThreadReplyTreeByThreadId",
            description = "获取指定课程ID下所有回复列表,并以树状结构返回。",
            permission = PermissionConstants.REPLY_QUERY
    )
    @GetMapping("/tree/Thread/{threadId}")
    public Result<List<ThreadReplyVO>> listThreadReplyTreeByThreadId(
            @Parameter(name = "threadId", description = "主贴ID", required = true) @PathVariable("threadId") UUID threadId
    ) {
        List<ThreadReplyVO> threadReplyVOList = threadReplyService.listThreadReplyTreeByThreadId(threadId);
        return Result.success(threadReplyVOList);
    }

    @HasPermission(
            summary = "getThreadReplyById",
            description = "通过回复的唯一ID获取其详细信息。",
            permission = PermissionConstants.REPLY_QUERY
    )
    @GetMapping("/{id}")
    public Result<ThreadReplyVO> getThreadReplyById(
            @Parameter(name = "id", description = "回复ID", required = true) @PathVariable("id") UUID id
    ) {
        ThreadReplyVO threadReplyVO = threadReplyService.getThreadReplyById(id);
        return Result.success(threadReplyVO);
    }

    @HasPermission(
            summary = "addThreadReply",
            description = "发布新的论坛回复。",
            permission = PermissionConstants.REPLY_ADD
    )
    @PostMapping("/add")
    public Result<ThreadReplyVO> addThreadReply(@Valid @RequestBody ThreadReplyDTO threadReplyDTO) {
        ThreadReplyVO threadReplyVO = threadReplyService.addThreadReply(threadReplyDTO);
        return Result.success(threadReplyVO);
    }

    @HasPermission(
            summary = "updateThreadReply",
            description = "修改现有论坛回复的信息。",
            permission = PermissionConstants.REPLY_EDIT
    )
    @PutMapping
    public Result<Boolean> updateThreadReply(@Valid @RequestBody ThreadReplyDTO threadReplyDTO) {
        return Result.success(threadReplyService.updateThreadReply(threadReplyDTO));
    }

    @HasPermission(
            summary = "removeThreadReplyById",
            description = "根据回复ID从系统中移除回复。",
            permission = PermissionConstants.REPLY_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeThreadReplyById(
            @Parameter(name = "id", description = "回复ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(threadReplyService.removeThreadReplyById(id));
    }

    @HasPermission(
            summary = "removeThreadReplyByIds",
            description = "根据回复ID列表批量删除回复。",
            permission = PermissionConstants.REPLY_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeThreadReplyByIds(
            @Parameter(name = "ids", description = "回复ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        return Result.success(threadReplyService.removeThreadReplyByIds(ids));
    }
}
