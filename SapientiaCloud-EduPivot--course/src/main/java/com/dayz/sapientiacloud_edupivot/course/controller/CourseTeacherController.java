package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.TeacherVO;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseTeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.MyCourseForTeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseVO;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseTeacherService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "课程教师管理", description = "用于管理课程教师信息的API")
@RestController
@RequestMapping("/course-teacher")
@RequiredArgsConstructor
public class CourseTeacherController extends BaseController {

    private final ICourseTeacherService courseTeacherService;


    @HasPermission(
            summary = "listCourseByTeacherId",
            description = "分页获取教师作为负责人或教学团队成员的所有课程。",
            permission = PermissionConstants.TEACHER_QUERY
    )
    @GetMapping("/teacher")
    public TableDataResult listCourseByTeacherId(@ParameterObject CourseTeacherQueryDTO courseTeacherQueryDTO) {
        startPage();
        PageInfo<CourseVO> pageInfo = courseTeacherService.listCourseByTeacherId(courseTeacherQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "listAllTeacherByCourseId",
            description = "获取课程下的所有教师。",
            permission = PermissionConstants.TEACHER_QUERY
    )
    @GetMapping("/course/{courseId}/all")
    public Result<List<TeacherVO>> listAllTeacherByCourseId(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId
    ) {
        List<TeacherVO> teacherVOList = courseTeacherService.listAllTeacherByCourseId(courseId);
        return Result.success(teacherVOList);
    }

    @HasPermission(
            summary = "listAllCourseByTeacherId",
            description = "获取教师作为负责人或教学团队成员的所有课程。",
            permission = PermissionConstants.TEACHER_QUERY
    )
    @GetMapping("/teacher/{teacherId}/all")
    public Result<List<CourseVO>> listAllCourseByTeacherId(
            @Parameter(name = "teacherId", description = "教师ID", required = true) @PathVariable("teacherId") UUID teacherId
    ) {
        List<CourseVO> courseVOList = courseTeacherService.listAllCourseByTeacherId(teacherId);
        return Result.success(courseVOList);
    }

    @HasPermission(
            summary = "assignCourseTeachers",
            description = "为指定课程批量分配教师团队，支持添加和移除教师。",
            permission = PermissionConstants.TEACHER_EDIT
    )
    @PostMapping("/{courseId}/teachers/assign")
    public Result<Boolean> assignCourseTeachers(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId,
            @Parameter(name = "teacherIds", description = "教师ID列表", required = true) @RequestBody List<UUID> teacherIds
    ) {
        return Result.success(courseTeacherService.assignCourseTeachers(courseId, teacherIds));
    }

    @HasPermission(
            summary = "listMyCourseForTeacher",
            description = "分页获取当前教师作为助教参与的课程。",
            permission = PermissionConstants.TEACHER_QUERY
    )
    @GetMapping("my-course")
    public TableDataResult listMyCourseForTeacher(@ParameterObject MyCourseForTeacherQueryDTO myCourseForTeacherQueryDTO) {
        startPage();
        PageInfo<CourseVO> pageInfo = courseTeacherService.listMyCourseForTeacher(myCourseForTeacherQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "batchAddAssistantTeachers",
            description = "为指定课程批量添加助教教师关系。",
            permission = PermissionConstants.TEACHER_EDIT
    )
    @PostMapping("/{courseId}/assistants/batch")
    public Result<Boolean> batchAddAssistantTeachers(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId,
            @Parameter(name = "teacherIds", description = "教师ID列表", required = true) @RequestBody List<UUID> teacherIds
    ) {
        return Result.success(courseTeacherService.batchAddAssistantTeachers(courseId, teacherIds));
    }

    @HasPermission(
            summary = "batchDeleteAssistantTeachers",
            description = "为指定课程批量删除助教教师关系。",
            permission = PermissionConstants.TEACHER_EDIT
    )
    @DeleteMapping("/{courseId}/assistants/batch")
    public Result<Integer> batchDeleteAssistantTeachers(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId,
            @Parameter(name = "teacherIds", description = "教师ID列表", required = true) @RequestBody List<UUID> teacherIds
    ) {
        Integer result = courseTeacherService.batchDeleteAssistantTeachers(courseId, teacherIds);
        return Result.success(result);
    }
}
