package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.StudentCourseDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.StudentCourseVO;
import com.dayz.sapientiacloud_edupivot.course.service.IStudentCourseService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Tag(name = "学生课程管理", description = "用于管理学生选课、退课、成绩等信息的API")
@RestController
@RequestMapping("/student-course")
@RequiredArgsConstructor
public class StudentCourseController extends BaseController {

    private final IStudentCourseService studentCourseService;

    @HasPermission(
            summary = "学生选课",
            description = "学生选择某门课程。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @PostMapping("/enroll")
    public Result<Boolean> enrollCourse(@Valid @RequestBody StudentCourseDTO studentCourseDTO) {
        return Result.success(studentCourseService.enrollCourse(studentCourseDTO));
    }

    @HasPermission(
            summary = "学生退课",
            description = "学生退出已选的课程。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @PostMapping("/drop")
    public Result<Boolean> dropCourse(
            @Parameter(name = "studentId", description = "学生ID", required = true) @RequestParam("studentId") UUID studentId,
            @Parameter(name = "courseId", description = "课程ID", required = true) @RequestParam("courseId") UUID courseId
    ) {
        return Result.success(studentCourseService.dropCourse(studentId, courseId));
    }

    @HasPermission(
            summary = "更新学生成绩",
            description = "更新学生在某门课程的成绩。",
            permission = PermissionConstants.COURSE_EDIT
    )
    @PutMapping("/grade")
    public Result<Boolean> updateGrade(
            @Parameter(name = "studentId", description = "学生ID", required = true) @RequestParam("studentId") UUID studentId,
            @Parameter(name = "courseId", description = "课程ID", required = true) @RequestParam("courseId") UUID courseId,
            @Parameter(name = "grade", description = "成绩", required = true) @RequestParam("grade") BigDecimal grade
    ) {
        return Result.success(studentCourseService.updateGrade(studentId, courseId, grade));
    }

    @HasPermission(
            summary = "批量更新学生成绩",
            description = "批量更新多个学生的课程成绩。",
            permission = PermissionConstants.COURSE_EDIT
    )
    @PutMapping("/grade/batch")
    public Result<Integer> batchUpdateGrade(
            @Parameter(name = "studentCourseDTOList", description = "学生课程信息列表", required = true) @RequestBody List<StudentCourseDTO> studentCourseDTOList
    ) {
        return Result.success(studentCourseService.batchUpdateGrade(studentCourseDTOList));
    }

    @HasPermission(
            summary = "分页查询学生选课记录",
            description = "根据学生ID分页查询选课记录。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/student/{studentId}")
    public TableDataResult listStudentCourseByStudentId(
            @Parameter(name = "studentId", description = "学生ID", required = true) @PathVariable("studentId") UUID studentId,
            @Parameter(name = "pageNum", description = "页码", required = true) @RequestParam("pageNum") Integer pageNum,
            @Parameter(name = "pageSize", description = "页大小", required = true) @RequestParam("pageSize") Integer pageSize
    ) {
        startPage();
        PageInfo<StudentCourseVO> pageInfo = studentCourseService.listStudentCoursePageByStudentId(studentId, pageNum, pageSize);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "分页查询课程选课学生",
            description = "根据课程ID分页查询选课学生。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/course/{courseId}")
    public TableDataResult listStudentCourseByCourseId(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId,
            @Parameter(name = "pageNum", description = "页码", required = true) @RequestParam("pageNum") Integer pageNum,
            @Parameter(name = "pageSize", description = "页大小", required = true) @RequestParam("pageSize") Integer pageSize
    ) {
        startPage();
        PageInfo<StudentCourseVO> pageInfo = studentCourseService.listStudentCoursePageByCourseId(courseId, pageNum, pageSize);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "获取学生所有选课记录",
            description = "根据学生ID获取所有选课记录。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/student/{studentId}/all")
    public Result<List<StudentCourseVO>> listStudentCourseByStudentId(
            @Parameter(name = "studentId", description = "学生ID", required = true) @PathVariable("studentId") UUID studentId
    ) {
        List<StudentCourseVO> studentCourseVOList = studentCourseService.listStudentCourseByStudentId(studentId);
        return Result.success(studentCourseVOList);
    }

    @HasPermission(
            summary = "获取课程所有选课学生",
            description = "根据课程ID获取所有选课学生。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/course/{courseId}/all")
    public Result<List<StudentCourseVO>> listStudentCourseByCourseId(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId
    ) {
        List<StudentCourseVO> studentCourseVOList = studentCourseService.listStudentCourseByCourseId(courseId);
        return Result.success(studentCourseVOList);
    }

    @HasPermission(
            summary = "检查学生是否已选某门课程",
            description = "检查学生是否已经选择了指定的课程。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/check-enrolled")
    public Result<Boolean> isEnrolled(
            @Parameter(name = "studentId", description = "学生ID", required = true) @RequestParam("studentId") UUID studentId,
            @Parameter(name = "courseId", description = "课程ID", required = true) @RequestParam("courseId") UUID courseId
    ) {
        return Result.success(studentCourseService.isEnrolled(studentId, courseId));
    }

    @HasPermission(
            summary = "获取学生某门课程的成绩",
            description = "获取学生在指定课程中的成绩。",
            permission = PermissionConstants.COURSE_QUERY
    )
    @GetMapping("/grade")
    public Result<BigDecimal> getStudentGrade(
            @Parameter(name = "studentId", description = "学生ID", required = true) @RequestParam("studentId") UUID studentId,
            @Parameter(name = "courseId", description = "课程ID", required = true) @RequestParam("courseId") UUID courseId
    ) {
        return Result.success(studentCourseService.getStudentGrade(studentId, courseId));
    }
}
