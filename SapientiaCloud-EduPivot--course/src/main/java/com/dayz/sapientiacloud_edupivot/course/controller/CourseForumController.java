package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseForumDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseForumQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseForumVO;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseForumService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "课程论坛管理", description = "用于管理课程论坛的API")
@RestController
@RequestMapping("/forum")
@RequiredArgsConstructor
public class CourseForumController extends BaseController {

    private final ICourseForumService courseForumService;

    @HasPermission(
            summary = "listCourseForum",
            description = "根据传入的条件分页查询课程论坛信息。支持根据论坛名称、论坛类型、课程ID等字段进行查询。",
            permission = PermissionConstants.FORUM_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listCourseForum(@ParameterObject CourseForumQueryDTO courseForumQueryDTO) {
        startPage();
        PageInfo<CourseForumVO> pageInfo = courseForumService.listCourseForum(courseForumQueryDTO);
        return getDataTable(pageInfo);
    }

    @HasPermission(
            summary = "listAllCourseForumByCourseId",
            description = "根据课程ID获取该课程下的所有论坛列表。",
            permission = PermissionConstants.FORUM_QUERY
    )
    @GetMapping("/course/{courseId}")
    public Result<List<CourseForumVO>> listAllCourseForumByCourseId(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId
    ) {
        List<CourseForumVO> forumVOList = courseForumService.listAllCourseForumByCourseId(courseId);
        return Result.success(forumVOList);
    }

    @HasPermission(
            summary = "getCourseForumById",
            description = "通过论坛的唯一ID获取其详细信息。",
            permission = PermissionConstants.FORUM_QUERY
    )
    @GetMapping("/{id}")
    public Result<CourseForumVO> getCourseForumById(
            @Parameter(name = "id", description = "论坛ID", required = true) @PathVariable("id") UUID id
    ) {
        CourseForumVO courseForumVO = courseForumService.getCourseForumById(id);
        return Result.success(courseForumVO);
    }

    @HasPermission(
            summary = "addCourseForum",
            description = "向课程中添加一个新的论坛。",
            permission = PermissionConstants.FORUM_ADD
    )
    @PostMapping
    public Result<CourseForumVO> addCourseForum(
            @RequestBody @Valid CourseForumDTO courseForumDTO
    ) {
        CourseForumVO courseForumVO = courseForumService.addCourseForum(courseForumDTO);
        return Result.success(courseForumVO);
    }

    @HasPermission(
            summary = "updateCourseForum",
            description = "更新现有论坛的信息。",
            permission = PermissionConstants.FORUM_EDIT
    )
    @PutMapping
    public Result<Boolean> updateCourseForum(
            @RequestBody @Valid CourseForumDTO courseForumDTO
    ) {
        Boolean result = courseForumService.updateCourseForum(courseForumDTO);
        return Result.success(result);
    }

    @HasPermission(
            summary = "removeCourseForumById",
            description = "通过论坛的唯一ID删除论坛。",
            permission = PermissionConstants.FORUM_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeCourseForumById(
            @Parameter(name = "id", description = "论坛ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = courseForumService.removeCourseForumById(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "removeCourseForumByIds",
            description = "根据论坛ID列表批量删除论坛。",
            permission = PermissionConstants.FORUM_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeCourseForumByIds(
            @RequestBody List<UUID> ids
    ) {
        Integer result = courseForumService.removeCourseForumByIds(ids);
        return Result.success(result);
    }

    @HasPermission(
            summary = "updateForumStatus",
            description = "更新论坛状态（正常/关闭/维护）。",
            permission = PermissionConstants.FORUM_EDIT
    )
    @PutMapping("/{id}/status")
    public Result<Boolean> updateForumStatus(
            @Parameter(name = "id", description = "论坛ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "status", description = "论坛状态 (0=正常, 1=关闭, 2=维护)", required = true) @RequestParam("status") Integer status
    ) {
        Boolean result = courseForumService.updateForumStatus(id, status);
        return Result.success(result);
    }

    @HasPermission(
            summary = "getForumStatistics",
            description = "获取论坛统计信息（帖子数、回复数等）。",
            permission = PermissionConstants.FORUM_QUERY
    )
    @GetMapping("/{id}/statistics")
    public Result<CourseForumVO> getForumStatistics(
            @Parameter(name = "id", description = "论坛ID", required = true) @PathVariable("id") UUID id
    ) {
        CourseForumVO statistics = courseForumService.getForumStatistics(id);
        return Result.success(statistics);
    }
}
