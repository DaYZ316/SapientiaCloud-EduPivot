package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseVO;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "课程管理", description = "用于管理课程信息的API")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CourseController extends BaseController {

    private final ICourseService courseService;

    @HasPermission(
            summary = "listCourse",
            description = "根据传入的条件分页查询课程信息。支持根据课程名称、课程代码、课程类型、学期、学年、教师等字段进行模糊查询。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listCourse(@ParameterObject CourseQueryDTO courseQueryDTO) {
        startPage();
        PageInfo<CourseVO> pageInfo = courseService.listCoursePage(courseQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "listAllCourse",
            description = "获取所有课程列表。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/all")
    public Result<List<CourseVO>> listAllCourse() {
        List<CourseVO> courseVOList = courseService.listAllCourse();
        return Result.success(courseVOList);
    }

    @HasPermission(
            summary = "getCourseById",
            description = "通过课程的唯一ID获取其详细信息。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/{id}")
    public Result<CourseVO> getCourseById(
            @Parameter(name = "id", description = "课程ID", required = true) @PathVariable("id") UUID id
    ) {
        CourseVO courseVO = courseService.getCourseById(id);
        return Result.success(courseVO);
    }

    @HasPermission(
            summary = "addCourse",
            description = "管理员添加系统课程",
            permission = PermissionConstants.COURSE_ADD
    )
    @PostMapping("/add")
    public Result<CourseVO> addCourse(@Valid @RequestBody CourseDTO courseDTO) {
        CourseVO courseVO = courseService.addCourse(courseDTO);
        return Result.success(courseVO);
    }

    @HasPermission(
            summary = "updateCourse",
            description = "修改现有课程的信息。",
            permission = PermissionConstants.COURSE_EDIT
    )
    @PutMapping
    public Result<Boolean> updateCourse(@Valid @RequestBody CourseDTO courseDTO) {
        return Result.success(courseService.updateCourse(courseDTO));
    }

    @HasPermission(
            summary = "removeCourseById",
            description = "根据课程ID从系统中移除课程。",
            permission = PermissionConstants.COURSE_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeCourseById(
            @Parameter(name = "id", description = "课程ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(courseService.removeCourseById(id));
    }

    @HasPermission(
            summary = "removeCourseByIds",
            description = "根据课程ID列表批量删除课程。",
            permission = PermissionConstants.COURSE_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeCourseByIds(
            @Parameter(name = "ids", description = "课程ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        return Result.success(courseService.removeCourseByIds(ids));
    }
}
