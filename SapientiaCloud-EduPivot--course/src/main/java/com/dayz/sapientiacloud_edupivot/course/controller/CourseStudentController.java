package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseStudentDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseStudentVO;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseStudentService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "课程学生管理", description = "用于管理课程学生选课、退课、成绩等信息的API")
@RestController
@RequestMapping("/course-student")
@RequiredArgsConstructor
public class CourseStudentController extends BaseController {

    private final ICourseStudentService courseStudentService;

    @HasPermission(
            summary = "listCourseStudent",
            description = "分页查询选课记录。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/course/list")
    public TableDataResult listCourseStudent(@ParameterObject CourseStudentQueryDTO courseStudentQueryDTO) {
        startPage();
        PageInfo<CourseStudentVO> pageInfo = courseStudentService.listCourseStudent(courseStudentQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "listAllCourseStudentByCourseId",
            description = "根据课程ID获取所有选课学生。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/course/{courseId}/all")
    public Result<List<CourseStudentVO>> listAllCourseStudentByCourseId(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId
    ) {
        List<CourseStudentVO> courseStudentVOList = courseStudentService.listAllCourseStudentByCourseId(courseId);
        return Result.success(courseStudentVOList);
    }

    @HasPermission(
            summary = "listAllCourseStudentByStudentId",
            description = "根据学生ID获取所有选课记录。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/student/{studentId}/all")
    public Result<List<CourseStudentVO>> listAllCourseStudentByStudentId(
            @Parameter(name = "studentId", description = "学生ID", required = true) @PathVariable("studentId") UUID studentId
    ) {
        List<CourseStudentVO> courseStudentVOList = courseStudentService.listAllCourseStudentByStudentId(studentId);
        return Result.success(courseStudentVOList);
    }

    @HasPermission(
            summary = "getStudentCourseById",
            description = "获取学生在指定课程中的选课信息。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/{studentId}")
    public Result<CourseStudentVO> getStudentCourseById(
            @Parameter(name = "studentId", description = "学生ID", required = true) @PathVariable("studentId") UUID studentId,
            @Parameter(name = "courseId", description = "课程ID", required = true) @RequestParam("courseId") UUID courseId
    ) {
        return Result.success(courseStudentService.getStudentCourseById(studentId, courseId));
    }

    @HasPermission(
            summary = "addCourseStudent",
            description = "学生选择某门课程。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @PostMapping("/add")
    public Result<Boolean> addCourseStudent(@Valid @RequestBody CourseStudentDTO courseStudentDTO) {
        return Result.success(courseStudentService.addCourseStudent(courseStudentDTO));
    }

    @HasPermission(
            summary = "updateCourseStudent",
            description = "修改选课信息。",
            permission = PermissionConstants.COURSE_EDIT
    )
    @PutMapping
    public Result<Boolean> updateCourseStudent(@Valid @RequestBody CourseStudentDTO courseStudentDTO) {
        return Result.success(courseStudentService.updateCourseStudent(courseStudentDTO));
    }

    @HasPermission(
            summary = "removeCourseStudentByStudentId",
            description = "学生退出已选的课程。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @DeleteMapping("/{studentId}")
    public Result<Boolean> removeCourseStudentByStudentId(
            @Parameter(name = "courseId", description = "课程ID", required = true) @RequestParam("courseId") UUID courseId,
            @Parameter(name = "studentId", description = "学生ID", required = true) @PathVariable("studentId") UUID studentId
    ) {
        return Result.success(courseStudentService.removeCourseStudentById(courseId, studentId));
    }

    @HasPermission(
            summary = "removeCourseStudentByStudentIds",
            description = "批量删除选课记录。",
            permission = PermissionConstants.COURSE_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeCourseStudentByStudentIds(
            @Parameter(name = "courseId", description = "课程ID", required = true) @RequestParam("courseId") UUID courseId,
            @Parameter(name = "studentIds", description = "选课记录ID列表", required = true) @RequestBody List<UUID> studentIds
    ) {
        return Result.success(courseStudentService.removeCourseStudentByIds(courseId, studentIds));
    }
}
