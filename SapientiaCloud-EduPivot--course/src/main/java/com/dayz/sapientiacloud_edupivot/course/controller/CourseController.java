package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseTeacherQueryDTO;
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
    public Result<Boolean> removeCourseById(
            @Parameter(name = "id", description = "课程ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(courseService.removeCourseById(id));
    }

    @HasPermission(
            summary = "批量删除课程",
            description = "根据课程ID列表批量删除课程。",
            permission = PermissionConstants.COURSE_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeCourseByIds(
            @Parameter(name = "ids", description = "课程ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        return Result.success(courseService.removeCourseByIds(ids));
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
            summary = "学生加入课程",
            description = "学生根据课程ID加入课程。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @PostMapping("/{courseId}/enroll")
    public Result<Boolean> enrollStudentToCourse(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId,
            @Parameter(name = "studentId", description = "学生ID", required = true) @RequestParam("studentId") UUID studentId
    ) {
        return Result.success(courseService.enrollStudentToCourse(courseId, studentId));
    }

    @HasPermission(
            summary = "分配课程教师团队",
            description = "为指定课程分配教师团队。",
            permission = PermissionConstants.COURSE_EDIT
    )
    @PostMapping("/{courseId}/teachers")
    public Result<Boolean> assignCourseTeacherTeam(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId,
            @Parameter(name = "teacherIds", description = "教师ID列表", required = true) @RequestBody List<UUID> teacherIds
    ) {
        return Result.success(courseService.assignCourseTeacherTeam(courseId, teacherIds));
    }

    @HasPermission(
            summary = "根据学生ID查询课程",
            description = "获取学生已选择的所有课程。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/student/{studentId}/all")
    public Result<List<CourseVO>> listAllCourseByStudentId(
            @Parameter(name = "studentId", description = "学生ID", required = true) @PathVariable("studentId") UUID studentId
    ) {
        List<CourseVO> courseVOList = courseService.listAllCourseByStudentId(studentId);
        return Result.success(courseVOList);
    }

    @HasPermission(
            summary = "根据教师ID查询课程",
            description = "获取教师作为负责人或教学团队成员的所有课程。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/teacher/{teacherId}/all")
    public Result<List<CourseVO>> listAllCourseByTeacherId(
            @Parameter(name = "teacherId", description = "教师ID", required = true) @PathVariable("teacherId") UUID teacherId
    ) {
        List<CourseVO> courseVOList = courseService.listAllCourseByTeacherId(teacherId);
        return Result.success(courseVOList);
    }

    @HasPermission(
            summary = "根据教师ID分页查询课程",
            description = "分页获取教师作为负责人或教学团队成员的所有课程。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/teacher/page")
    public TableDataResult listCourseByTeacherId(@ParameterObject CourseTeacherQueryDTO courseTeacherQueryDTO) {
        startPage();
        PageInfo<CourseVO> pageInfo = courseService.listCourseByTeacherId(courseTeacherQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "根据学生ID分页查询课程",
            description = "分页获取学生已选择的所有课程。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/student/page")
    public TableDataResult listCourseByStudentId(@ParameterObject CourseStudentQueryDTO courseStudentQueryDTO) {
        startPage();
        PageInfo<CourseVO> pageInfo = courseService.listCourseByStudentId(courseStudentQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "批量分配课程教师团队",
            description = "为指定课程批量分配教师团队，支持添加和移除教师。",
            permission = PermissionConstants.COURSE_EDIT
    )
    @PostMapping("/{courseId}/teachers/assign")
    public Result<Boolean> assignCourseTeachers(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId,
            @Parameter(name = "teacherIds", description = "教师ID列表", required = true) @RequestBody List<UUID> teacherIds
    ) {
        return Result.success(courseService.assignCourseTeachers(courseId, teacherIds));
    }
}
