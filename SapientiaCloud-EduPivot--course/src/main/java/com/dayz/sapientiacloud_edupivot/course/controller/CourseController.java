package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseVO;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
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
            summary = "分页查找课程",
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
            summary = "获取所有课程",
            description = "获取所有课程列表。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/all")
    public Result<List<CourseVO>> listAllCourse() {
        List<CourseVO> courseVOList = courseService.listAllCourse();
        return Result.success(courseVOList);
    }

    @HasPermission(
            summary = "根据ID获取课程",
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
            summary = "更新现有课程",
            description = "修改现有课程的信息。",
            permission = PermissionConstants.COURSE_EDIT
    )
    @PutMapping
    public Result<Boolean> updateCourse(@Valid @RequestBody CourseDTO courseDTO) {
        return Result.success(courseService.updateCourse(courseDTO));
    }

    @HasPermission(
            summary = "删除课程",
            description = "根据课程ID从系统中移除课程。",
            permission = PermissionConstants.COURSE_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeCourse(
            @Parameter(name = "id", description = "课程ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(courseService.removeCourse(id));
    }

    @HasPermission(
            summary = "批量删除课程",
            description = "根据课程ID列表批量删除课程。",
            permission = PermissionConstants.COURSE_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeCourses(
            @Parameter(name = "ids", description = "课程ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        return Result.success(courseService.removeCourses(ids));
    }

    @HasPermission(
            summary = "分配课程教师",
            description = "为指定课程分配教师。",
            permission = PermissionConstants.COURSE_EDIT
    )
    @PostMapping("/{courseId}/teacher")
    public Result<Boolean> assignTeacher(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId,
            @Parameter(name = "teacherId", description = "教师ID", required = true) @RequestParam("teacherId") UUID teacherId
    ) {
        return Result.success(courseService.assignTeacher(courseId, teacherId));
    }

    @HasPermission(
            summary = "管理员添加新课程",
            description = "管理员添加系统课程",
            permission = PermissionConstants.COURSE_ADD
    )
    @PostMapping("/add")
    public Result<CourseVO> addCourse(@Valid @RequestBody CourseDTO courseDTO) {
        CourseVO courseVO = courseService.addCourse(courseDTO);
        return Result.success(courseVO);
    }

    @HasPermission(
            summary = "根据教师ID获取课程列表",
            description = "获取指定教师的所有课程。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/teacher/{teacherId}")
    public Result<List<CourseVO>> listCourseByTeacherId(
            @Parameter(name = "teacherId", description = "教师ID", required = true) @PathVariable("teacherId") UUID teacherId
    ) {
        List<CourseVO> courseVOList = courseService.listCourseByTeacherId(teacherId);
        return Result.success(courseVOList);
    }

    @HasPermission(
            summary = "根据学生ID获取可选课程",
            description = "获取学生可以选择的课程列表。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/student/{studentId}/available")
    public Result<List<CourseVO>> listAvailableCourseByStudentId(
            @Parameter(name = "studentId", description = "学生ID", required = true) @PathVariable("studentId") UUID studentId
    ) {
        List<CourseVO> courseVOList = courseService.listAvailableCourseByStudentId(studentId);
        return Result.success(courseVOList);
    }

    @HasPermission(
            summary = "更新课程状态",
            description = "更新课程的当前状态。",
            permission = PermissionConstants.COURSE_EDIT
    )
    @PutMapping("/{id}/status")
    public Result<Boolean> updateCourseStatus(
            @Parameter(name = "id", description = "课程ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "status", description = "课程状态", required = true) @RequestParam("status") Integer status
    ) {
        return Result.success(courseService.updateCourseStatus(id, status));
    }
}
