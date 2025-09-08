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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Tag(name = "课程学生管理", description = "用于管理课程学生选课、退课、成绩等信息的API")
@RestController
@RequestMapping("/course-student")
@RequiredArgsConstructor
public class CourseStudentController extends BaseController {

    private final ICourseStudentService courseStudentService;

    @HasPermission(
            summary = "enrollCourse",
            description = "学生选择某门课程。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @PostMapping("/enroll")
    public Result<Boolean> enrollCourse(@Valid @RequestBody CourseStudentDTO courseStudentDTO) {
        return Result.success(courseStudentService.enrollCourse(courseStudentDTO));
    }

    @HasPermission(
            summary = "dropCourse",
            description = "学生退出已选的课程。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @PostMapping("/drop")
    public Result<Boolean> dropCourse(
            @Parameter(name = "studentId", description = "学生ID", required = true) @RequestParam("studentId") UUID studentId,
            @Parameter(name = "courseId", description = "课程ID", required = true) @RequestParam("courseId") UUID courseId
    ) {
        return Result.success(courseStudentService.dropCourse(studentId, courseId));
    }

    @HasPermission(
            summary = "updateGrade",
            description = "更新学生在某门课程的成绩。",
            permission = PermissionConstants.COURSE_EDIT
    )
    @PutMapping("/grade")
    public Result<Boolean> updateGrade(
            @Parameter(name = "studentId", description = "学生ID", required = true) @RequestParam("studentId") UUID studentId,
            @Parameter(name = "courseId", description = "课程ID", required = true) @RequestParam("courseId") UUID courseId,
            @Parameter(name = "grade", description = "成绩", required = true) @RequestParam("grade") BigDecimal grade
    ) {
        return Result.success(courseStudentService.updateGrade(studentId, courseId, grade));
    }

    @HasPermission(
            summary = "batchUpdateGrade",
            description = "批量更新多个学生的课程成绩。",
            permission = PermissionConstants.COURSE_EDIT
    )
    @PutMapping("/grade/batch")
    public Result<Integer> batchUpdateGrade(
            @Parameter(name = "courseStudentDTOList", description = "课程学生信息列表", required = true) @RequestBody List<CourseStudentDTO> courseStudentDTOList
    ) {
        return Result.success(courseStudentService.batchUpdateGrade(courseStudentDTOList));
    }

    @HasPermission(
            summary = "listCourseStudentByStudentId",
            description = "根据学生ID分页查询选课记录。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/student/page")
    public TableDataResult listCourseStudentByStudentId(@ParameterObject CourseStudentQueryDTO courseStudentQueryDTO) {
        startPage();
        PageInfo<CourseStudentVO> pageInfo = courseStudentService.listCourseStudentByStudentId(courseStudentQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "listCourseStudentByCourseId",
            description = "根据课程ID分页查询选课学生。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/course/page")
    public TableDataResult listCourseStudentByCourseId(@ParameterObject CourseStudentQueryDTO courseStudentQueryDTO) {
        startPage();
        PageInfo<CourseStudentVO> pageInfo = courseStudentService.listCourseStudentByCourseId(courseStudentQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "listCourseStudentByStudentId",
            description = "根据学生ID获取所有选课记录。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/student/{studentId}/all")
    public Result<List<CourseStudentVO>> listCourseStudentByStudentId(
            @Parameter(name = "studentId", description = "学生ID", required = true) @PathVariable("studentId") UUID studentId
    ) {
        List<CourseStudentVO> courseStudentVOList = courseStudentService.listAllCourseStudentByStudentId(studentId);
        return Result.success(courseStudentVOList);
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
            summary = "isEnrolled",
            description = "检查学生是否已经选择了指定的课程。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/check-enrolled")
    public Result<Boolean> isEnrolled(
            @Parameter(name = "studentId", description = "学生ID", required = true) @RequestParam("studentId") UUID studentId,
            @Parameter(name = "courseId", description = "课程ID", required = true) @RequestParam("courseId") UUID courseId
    ) {
        return Result.success(courseStudentService.isEnrolled(studentId, courseId));
    }

    @HasPermission(
            summary = "getStudentGrade",
            description = "获取学生在指定课程中的成绩。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/grade")
    public Result<BigDecimal> getStudentGrade(
            @Parameter(name = "studentId", description = "学生ID", required = true) @RequestParam("studentId") UUID studentId,
            @Parameter(name = "courseId", description = "课程ID", required = true) @RequestParam("courseId") UUID courseId
    ) {
        return Result.success(courseStudentService.getStudentGrade(studentId, courseId));
    }
}
