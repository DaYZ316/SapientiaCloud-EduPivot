package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.ForumPostDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.ForumPostQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.ForumPostVO;
import com.dayz.sapientiacloud_edupivot.course.service.IForumPostService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "论坛帖子管理", description = "用于管理论坛帖子的API")
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class ForumPostController extends BaseController {

    private final IForumPostService forumPostService;

    @HasPermission(
            summary = "listForumPost",
            description = "根据传入的条件分页查询论坛帖子信息。支持根据帖子标题、内容、作者、论坛ID等字段进行查询。",
            permission = PermissionConstants.POST_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listForumPost(@ParameterObject ForumPostQueryDTO forumPostQueryDTO) {
        startPage();
        PageInfo<ForumPostVO> pageInfo = forumPostService.listForumPost(forumPostQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "listForumPostByForumId",
            description = "根据论坛ID获取该论坛下的所有帖子列表。",
            permission = PermissionConstants.POST_QUERY
    )
    @GetMapping("/forum/{forumId}")
    public TableDataResult listForumPostByForumId(
            @Parameter(name = "forumId", description = "论坛ID", required = true) @PathVariable("forumId") UUID forumId,
            @ParameterObject ForumPostQueryDTO forumPostQueryDTO
    ) {
        startPage();
        PageInfo<ForumPostVO> pageInfo = forumPostService.listForumPostByForumId(forumId, forumPostQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "listForumPostByCourseId",
            description = "根据课程ID获取该课程下的所有帖子列表。",
            permission = PermissionConstants.POST_QUERY
    )
    @GetMapping("/course/{courseId}")
    public TableDataResult listForumPostByCourseId(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId,
            @ParameterObject ForumPostQueryDTO forumPostQueryDTO
    ) {
        startPage();
        PageInfo<ForumPostVO> pageInfo = forumPostService.listForumPostByCourseId(courseId, forumPostQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "getForumPostById",
            description = "通过帖子的唯一ID获取其详细信息。",
            permission = PermissionConstants.POST_QUERY
    )
    @GetMapping("/{id}")
    public Result<ForumPostVO> getForumPostById(
            @Parameter(name = "id", description = "帖子ID", required = true) @PathVariable("id") UUID id
    ) {
        ForumPostVO forumPostVO = forumPostService.getForumPostById(id);
        return Result.success(forumPostVO);
    }

    @HasPermission(
            summary = "addForumPost",
            description = "在论坛中发布一个新帖子。",
            permission = PermissionConstants.POST_ADD
    )
    @PostMapping
    public Result<ForumPostVO> addForumPost(
            @RequestBody @Valid ForumPostDTO forumPostDTO
    ) {
        ForumPostVO forumPostVO = forumPostService.addForumPost(forumPostDTO);
        return Result.success(forumPostVO);
    }

    @HasPermission(
            summary = "updateForumPost",
            description = "更新现有帖子的信息。",
            permission = PermissionConstants.POST_EDIT
    )
    @PutMapping
    public Result<Boolean> updateForumPost(
            @RequestBody @Valid ForumPostDTO forumPostDTO
    ) {
        Boolean result = forumPostService.updateForumPost(forumPostDTO);
        return Result.success(result);
    }

    @HasPermission(
            summary = "removeForumPostById",
            description = "通过帖子的唯一ID删除帖子。",
            permission = PermissionConstants.POST_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeForumPostById(
            @Parameter(name = "id", description = "帖子ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = forumPostService.removeForumPostById(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "removeForumPostByIds",
            description = "根据帖子ID列表批量删除帖子。",
            permission = PermissionConstants.POST_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeForumPostByIds(
            @RequestBody List<UUID> ids
    ) {
        Integer result = forumPostService.removeForumPostByIds(ids);
        return Result.success(result);
    }

    @HasPermission(
            summary = "updatePostStatus",
            description = "更新帖子状态（正常/删除/审核中/审核失败）。",
            permission = PermissionConstants.POST_EDIT
    )
    @PutMapping("/{id}/status")
    public Result<Boolean> updatePostStatus(
            @Parameter(name = "id", description = "帖子ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "status", description = "帖子状态 (0=正常, 1=删除, 2=审核中, 3=审核失败)", required = true) @RequestParam("status") Integer status
    ) {
        Boolean result = forumPostService.updatePostStatus(id, status);
        return Result.success(result);
    }

    @HasPermission(
            summary = "setPostTop",
            description = "设置帖子置顶状态。",
            permission = PermissionConstants.POST_EDIT
    )
    @PutMapping("/{id}/top")
    public Result<Boolean> setPostTop(
            @Parameter(name = "id", description = "帖子ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "isTop", description = "是否置顶 (0=否, 1=是)", required = true) @RequestParam("isTop") Integer isTop
    ) {
        Boolean result = forumPostService.setPostTop(id, isTop);
        return Result.success(result);
    }

    @HasPermission(
            summary = "setPostEssence",
            description = "设置帖子精华状态。",
            permission = PermissionConstants.POST_EDIT
    )
    @PutMapping("/{id}/essence")
    public Result<Boolean> setPostEssence(
            @Parameter(name = "id", description = "帖子ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "isEssence", description = "是否精华 (0=否, 1=是)", required = true) @RequestParam("isEssence") Integer isEssence
    ) {
        Boolean result = forumPostService.setPostEssence(id, isEssence);
        return Result.success(result);
    }

    @HasPermission(
            summary = "setPostLock",
            description = "设置帖子锁定状态。",
            permission = PermissionConstants.POST_EDIT
    )
    @PutMapping("/{id}/lock")
    public Result<Boolean> setPostLock(
            @Parameter(name = "id", description = "帖子ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "isLocked", description = "是否锁定 (0=否, 1=是)", required = true) @RequestParam("isLocked") Integer isLocked
    ) {
        Boolean result = forumPostService.setPostLock(id, isLocked);
        return Result.success(result);
    }

    @HasPermission(
            summary = "likePost",
            description = "点赞帖子。",
            permission = PermissionConstants.POST_QUERY
    )
    @PostMapping("/{id}/like")
    public Result<Boolean> likePost(
            @Parameter(name = "id", description = "帖子ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = forumPostService.likePost(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "unlikePost",
            description = "取消点赞帖子。",
            permission = PermissionConstants.POST_QUERY
    )
    @DeleteMapping("/{id}/like")
    public Result<Boolean> unlikePost(
            @Parameter(name = "id", description = "帖子ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = forumPostService.unlikePost(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "sharePost",
            description = "分享帖子。",
            permission = PermissionConstants.POST_QUERY
    )
    @PostMapping("/{id}/share")
    public Result<Boolean> sharePost(
            @Parameter(name = "id", description = "帖子ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = forumPostService.sharePost(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "viewPost",
            description = "浏览帖子（增加浏览次数）。",
            permission = PermissionConstants.POST_QUERY
    )
    @PostMapping("/{id}/view")
    public Result<Boolean> viewPost(
            @Parameter(name = "id", description = "帖子ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = forumPostService.viewPost(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "getHotPosts",
            description = "获取热门帖子列表。",
            permission = PermissionConstants.POST_QUERY
    )
    @GetMapping("/hot")
    public Result<List<ForumPostVO>> getHotPosts(
            @Parameter(name = "limit", description = "返回数量限制", required = false) @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {
        List<ForumPostVO> hotPosts = forumPostService.getHotPosts(limit);
        return Result.success(hotPosts);
    }

    @HasPermission(
            summary = "getLatestPosts",
            description = "获取最新帖子列表。",
            permission = PermissionConstants.POST_QUERY
    )
    @GetMapping("/latest")
    public Result<List<ForumPostVO>> getLatestPosts(
            @Parameter(name = "limit", description = "返回数量限制", required = false) @RequestParam(value = "limit", defaultValue = "10") Integer limit
    ) {
        List<ForumPostVO> latestPosts = forumPostService.getLatestPosts(limit);
        return Result.success(latestPosts);
    }
}
