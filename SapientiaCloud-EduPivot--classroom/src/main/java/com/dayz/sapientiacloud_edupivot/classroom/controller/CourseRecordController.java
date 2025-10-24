package com.dayz.sapientiacloud_edupivot.classroom.controller;

import com.dayz.sapientiacloud_edupivot.classroom.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.classroom.common.result.Result;
import com.dayz.sapientiacloud_edupivot.classroom.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.classroom.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.classroom.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordQueryDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.CourseRecordVO;
import com.dayz.sapientiacloud_edupivot.classroom.service.ICourseRecordService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "课程记录管理", description = "课程教学记录（3D教室会话）管理相关API")
@RestController
@RequestMapping("/course-record")
@RequiredArgsConstructor
public class CourseRecordController extends BaseController {

    private final ICourseRecordService courseRecordService;

    /*@HasPermission(
            summary = "分页查询课程记录",
            description = "根据条件分页查询课程记录列表，支持按课程、教师、状态等条件筛选",
            permission = PermissionConstants.COURSE_RECORD_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listCourseRecord(@ParameterObject CourseRecordQueryDTO dto) {
        startPage();
        PageInfo<CourseRecordVO> pageInfo = courseRecordService.listCourseRecordPage(dto);
        return getDataTable(pageInfo.getList());
    }*/

    @HasPermission(
            summary = "查询所有课程记录",
            description = "获取所有课程记录列表",
            permission = PermissionConstants.COURSE_RECORD_QUERY
    )
    @GetMapping("/all")
    public Result<List<CourseRecordVO>> listAllCourseRecord() {
        List<CourseRecordVO> list = courseRecordService.listAllCourseRecord();
        return Result.success(list);
    }

    @HasPermission(
            summary = "查询课程记录详情",
            description = "根据ID查询课程记录详细信息",
            permission = PermissionConstants.COURSE_RECORD_QUERY
    )
    @GetMapping("/{id}")
    public Result<CourseRecordVO> getCourseRecordById(
            @Parameter(name = "id", description = "课程记录ID", required = true) @PathVariable("id") UUID id
    ) {
        CourseRecordVO courseRecordVO = courseRecordService.getCourseRecordById(id);
        return Result.success(courseRecordVO);
    }

    @HasPermission(
            summary = "创建课程记录",
            description = "教师开课，创建课程教学记录",
            permission = PermissionConstants.COURSE_RECORD_ADD
    )
    @PostMapping("/add")
    public Result<CourseRecordVO> addCourseRecord(@Valid @RequestBody CourseRecordDTO dto) {
        CourseRecordVO courseRecordVO = courseRecordService.addCourseRecord(dto);
        return Result.success(courseRecordVO);
    }

    @HasPermission(
            summary = "更新课程记录",
            description = "修改课程记录信息",
            permission = PermissionConstants.COURSE_RECORD_EDIT
    )
    @PutMapping("/update")
    public Result<Boolean> updateCourseRecord(@Valid @RequestBody CourseRecordDTO dto) {
        return Result.success(courseRecordService.updateCourseRecord(dto));
    }

    @HasPermission(
            summary = "删除课程记录",
            description = "根据ID删除课程记录",
            permission = PermissionConstants.COURSE_RECORD_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeCourseRecordById(
            @Parameter(name = "id", description = "课程记录ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(courseRecordService.removeCourseRecordById(id));
    }

    @HasPermission(
            summary = "批量删除课程记录",
            description = "根据ID列表批量删除课程记录",
            permission = PermissionConstants.COURSE_RECORD_DELETE
    )
    @DeleteMapping("/batch")
    public Result<Integer> removeCourseRecordByIds(
            @Parameter(name = "ids", description = "课程记录ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        return Result.success(courseRecordService.removeCourseRecordByIds(ids));
    }

    @HasPermission(
            summary = "结束课程",
            description = "教师下课，结束课程记录",
            permission = PermissionConstants.COURSE_RECORD_EDIT
    )
    @PostMapping("/end/{id}")
    public Result<Boolean> endCourseRecord(
            @Parameter(name = "id", description = "课程记录ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(courseRecordService.endCourseRecord(id));
    }

    @HasPermission(
            summary = "查询课程的所有记录",
            description = "根据课程ID查询该课程的所有教学记录",
            permission = PermissionConstants.COURSE_RECORD_QUERY
    )
    @GetMapping("/course/{courseId}")
    public Result<List<CourseRecordVO>> listCourseRecordByCourseId(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId
    ) {
        List<CourseRecordVO> list = courseRecordService.listCourseRecordByCourseId(courseId);
        return Result.success(list);
    }

    @HasPermission(
            summary = "查询教师的所有记录",
            description = "根据教师ID查询该教师的所有教学记录",
            permission = PermissionConstants.COURSE_RECORD_QUERY
    )
    @GetMapping("/teacher/{teacherId}")
    public Result<List<CourseRecordVO>> listCourseRecordByTeacherId(
            @Parameter(name = "teacherId", description = "教师ID", required = true) @PathVariable("teacherId") UUID teacherId
    ) {
        List<CourseRecordVO> list = courseRecordService.listCourseRecordByTeacherId(teacherId);
        return Result.success(list);
    }
}
