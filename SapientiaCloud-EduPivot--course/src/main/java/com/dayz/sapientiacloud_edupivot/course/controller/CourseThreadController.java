package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseThreadDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseThreadQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseThreadVO;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseThreadService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "课程论坛主贴管理", description = "用于管理课程论坛主贴信息的API")
@RestController
@RequestMapping("/course-thread")
@RequiredArgsConstructor
public class CourseThreadController extends BaseController {

    private final ICourseThreadService courseThreadService;

    @HasPermission(
            summary = "listCourseThread",
            description = "根据传入的条件分页查询论坛主贴信息。支持根据课程ID、用户ID、标题等字段进行查询。",
            permission = PermissionConstants.THREAD_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listCourseThread(@ParameterObject CourseThreadQueryDTO courseThreadQueryDTO) {
        startPage();
        PageInfo<CourseThreadVO> pageInfo = courseThreadService.listCourseThread(courseThreadQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "listAllCourseThread",
            description = "获取所有论坛主贴列表。",
            permission = PermissionConstants.THREAD_QUERY
    )
    @GetMapping("/all")
    public Result<List<CourseThreadVO>> listAllCourseThread() {
        List<CourseThreadVO> courseThreadVOList = courseThreadService.listAllCourseThread();
        return Result.success(courseThreadVOList);
    }

    @HasPermission(
            summary = "getCourseThreadById",
            description = "通过主贴的唯一ID获取其详细信息。",
            permission = PermissionConstants.THREAD_QUERY
    )
    @GetMapping("/{id}")
    public Result<CourseThreadVO> getCourseThreadById(
            @Parameter(name = "id", description = "主贴ID", required = true) @PathVariable("id") UUID id
    ) {
        CourseThreadVO courseThreadVO = courseThreadService.getCourseThreadById(id);
        return Result.success(courseThreadVO);
    }

    @HasPermission(
            summary = "addCourseThread",
            description = "发布新的论坛主贴。",
            permission = PermissionConstants.THREAD_ADD
    )
    @PostMapping("/add")
    public Result<CourseThreadVO> addCourseThread(@Valid @RequestBody CourseThreadDTO courseThreadDTO) {
        CourseThreadVO courseThreadVO = courseThreadService.addCourseThread(courseThreadDTO);
        return Result.success(courseThreadVO);
    }

    @HasPermission(
            summary = "updateCourseThread",
            description = "修改现有论坛主贴的信息。",
            permission = PermissionConstants.THREAD_EDIT
    )
    @PutMapping
    public Result<Boolean> updateCourseThread(@Valid @RequestBody CourseThreadDTO courseThreadDTO) {
        return Result.success(courseThreadService.updateCourseThread(courseThreadDTO));
    }

    @HasPermission(
            summary = "removeCourseThreadById",
            description = "根据主贴ID从系统中移除主贴。",
            permission = PermissionConstants.THREAD_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeCourseThreadById(
            @Parameter(name = "id", description = "主贴ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(courseThreadService.removeCourseThreadById(id));
    }

    @HasPermission(
            summary = "removeCourseThreadByIds",
            description = "根据主贴ID列表批量删除主贴。",
            permission = PermissionConstants.THREAD_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeCourseThreadByIds(
            @Parameter(name = "ids", description = "主贴ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        return Result.success(courseThreadService.removeCourseThreadByIds(ids));
    }

    @HasPermission(
            summary = "pinThread",
            description = "置顶或取消置顶主贴。",
            permission = PermissionConstants.THREAD_EDIT
    )
    @PutMapping("/pin/{id}")
    public Result<Boolean> pinThread(
            @Parameter(name = "id", description = "主贴ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "isPinned", description = "是否置顶", required = true) @RequestParam("isPinned") Boolean pinned
    ) {
        return Result.success(courseThreadService.pinThread(id, pinned));
    }

    @HasPermission(
            summary = "closeThread",
            description = "关闭或开启主贴。",
            permission = PermissionConstants.THREAD_EDIT
    )
    @PutMapping("/close/{id}")
    public Result<Boolean> closeThread(
            @Parameter(name = "id", description = "主贴ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "isClosed", description = "是否关闭", required = true) @RequestParam("isClosed") Boolean closed
    ) {
        return Result.success(courseThreadService.closeThread(id, closed));
    }

    @HasPermission(
            summary = "viewThread",
            description = "浏览主贴，增加浏览次数。",
            permission = PermissionConstants.THREAD_QUERY
    )
    @PostMapping("/view/{id}")
    public Result<Boolean> viewThread(
            @Parameter(name = "id", description = "主贴ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(courseThreadService.viewThread(id));
    }
}
