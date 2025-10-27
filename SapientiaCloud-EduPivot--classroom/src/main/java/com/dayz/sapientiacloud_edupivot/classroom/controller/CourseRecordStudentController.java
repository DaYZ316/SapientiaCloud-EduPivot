package com.dayz.sapientiacloud_edupivot.classroom.controller;

import com.dayz.sapientiacloud_edupivot.classroom.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.classroom.common.result.Result;
import com.dayz.sapientiacloud_edupivot.classroom.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.classroom.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.classroom.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordStudentDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.CourseRecordStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.StudentSeatDeleteDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.CourseRecordStudentVO;
import com.dayz.sapientiacloud_edupivot.classroom.service.ICourseRecordStudentService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "学生座位管理", description = "课程教学学生座位管理相关API")
@RestController
@RequestMapping("/course-record-student")
@RequiredArgsConstructor
public class CourseRecordStudentController extends BaseController {

    private final ICourseRecordStudentService courseRecordStudentService;

    @HasPermission(
            summary = "分页查询学生座位",
            description = "根据条件分页查询学生座位记录列表",
            permission = PermissionConstants.COURSE_RECORD_STUDENT_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listCourseRecordStudent(@ParameterObject CourseRecordStudentQueryDTO dto) {
        startPage();
        PageInfo<CourseRecordStudentVO> pageInfo = courseRecordStudentService.listCourseRecordStudentPage(dto);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "查询所有学生座位",
            description = "获取所有学生座位记录列表",
            permission = PermissionConstants.COURSE_RECORD_STUDENT_QUERY
    )
    @GetMapping("/all")
    public Result<List<CourseRecordStudentVO>> listAllCourseRecordStudent() {
        List<CourseRecordStudentVO> list = courseRecordStudentService.listAllCourseRecordStudent();
        return Result.success(list);
    }

    @HasPermission(
            summary = "查询课程记录的所有学生",
            description = "根据课程记录ID查询该课程的所有学生座位信息",
            permission = PermissionConstants.COURSE_RECORD_STUDENT_QUERY
    )
    @GetMapping("/record/{recordId}")
    public Result<List<CourseRecordStudentVO>> listStudentsByRecordId(
            @Parameter(name = "recordId", description = "课程记录ID", required = true) @PathVariable("recordId") UUID recordId
    ) {
        List<CourseRecordStudentVO> list = courseRecordStudentService.listStudentsByRecordId(recordId);
        return Result.success(list);
    }

    @HasPermission(
            summary = "查询学生座位详情",
            description = "查询具体学生在某课程记录中的座位信息",
            permission = PermissionConstants.COURSE_RECORD_STUDENT_QUERY
    )
    @GetMapping("/student-seat/{recordId}")
    public Result<CourseRecordStudentVO> getStudentSeat(
            @Parameter(name = "recordId", description = "课程记录ID", required = true) @PathVariable("recordId") UUID recordId,
            @Parameter(name = "studentId", description = "学生ID", required = true) @RequestParam("studentId") UUID studentId
    ) {
        CourseRecordStudentVO studentSeat = courseRecordStudentService.getStudentSeat(recordId, studentId);
        return Result.success(studentSeat);
    }

    @HasPermission(
            summary = "学生选座",
            description = "学生选择座位并入座",
            permission = PermissionConstants.COURSE_RECORD_STUDENT_ADD
    )
    @PostMapping("/add")
    public Result<CourseRecordStudentVO> addStudentSeat(@Valid @RequestBody CourseRecordStudentDTO dto) {
        CourseRecordStudentVO studentSeat = courseRecordStudentService.addStudentSeat(dto);
        return Result.success(studentSeat);
    }

    @HasPermission(
            summary = "更新学生座位",
            description = "更新学生座位信息（换座、更新状态等）",
            permission = PermissionConstants.COURSE_RECORD_STUDENT_EDIT
    )
    @PutMapping("/update")
    public Result<Boolean> updateStudentSeat(@Valid @RequestBody CourseRecordStudentDTO dto) {
        return Result.success(courseRecordStudentService.updateStudentSeat(dto));
    }

    @HasPermission(
            summary = "删除学生座位",
            description = "移除学生座位（学生离开教室）",
            permission = PermissionConstants.COURSE_RECORD_STUDENT_DELETE
    )
    @DeleteMapping("/{recordId}/{studentId}")
    public Result<Boolean> removeStudentSeat(
            @Parameter(name = "recordId", description = "课程记录ID", required = true) @PathVariable("recordId") UUID recordId,
            @Parameter(name = "studentId", description = "学生ID", required = true) @PathVariable("studentId") UUID studentId
    ) {
        return Result.success(courseRecordStudentService.removeStudentSeat(recordId, studentId));
    }

    @HasPermission(
            summary = "批量删除学生座位",
            description = "批量移除学生座位",
            permission = PermissionConstants.COURSE_RECORD_STUDENT_DELETE
    )
    @Operation(summary = "批量删除学生座位")
    @DeleteMapping("/batch")
    public Result<Integer> removeStudentSeatBatch(@RequestBody List<StudentSeatDeleteDTO> dtoList) {
        return Result.success(courseRecordStudentService.removeStudentSeatBatch(dtoList));
    }

    @HasPermission(
            summary = "检查座位是否被占用",
            description = "检查指定座位是否已被其他学生占用",
            permission = PermissionConstants.COURSE_RECORD_STUDENT_QUERY
    )
    @GetMapping("/check-seat/{recordId}/{seatIndex}")
    public Result<Boolean> checkSeatOccupied(
            @Parameter(name = "recordId", description = "课程记录ID", required = true) @PathVariable("recordId") UUID recordId,
            @Parameter(name = "seatIndex", description = "座位编号", required = true) @PathVariable("seatIndex") Integer seatIndex
    ) {
        Boolean occupied = courseRecordStudentService.checkSeatOccupied(recordId, seatIndex);
        return Result.success(occupied);
    }

    @HasPermission(
            summary = "统计实到人数",
            description = "统计某课程记录的实际到课人数",
            permission = PermissionConstants.COURSE_RECORD_STUDENT_QUERY
    )
    @GetMapping("/count/{recordId}")
    public Result<Integer> countStudentsByRecordId(
            @Parameter(name = "recordId", description = "课程记录ID", required = true) @PathVariable("recordId") UUID recordId
    ) {
        Integer count = courseRecordStudentService.countStudentsByRecordId(recordId);
        return Result.success(count);
    }
}
