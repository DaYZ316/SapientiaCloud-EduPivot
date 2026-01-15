package com.dayz.sapientiacloud_edupivot.student.controller;

import com.dayz.sapientiacloud_edupivot.student.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.student.common.result.Result;
import com.dayz.sapientiacloud_edupivot.student.common.result.TableDataResult;
import com.github.pagehelper.PageInfo;
import com.dayz.sapientiacloud_edupivot.student.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.student.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentAddDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.vo.PracticeStatisticsVO;
import com.dayz.sapientiacloud_edupivot.student.entity.vo.QuestionStudentVO;
import com.dayz.sapientiacloud_edupivot.student.service.IQuestionStudentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "学生课堂练习", description = "学生课堂练习提交与查询API")
@RestController
@RequestMapping("/practice")
@RequiredArgsConstructor
public class PracticeController extends BaseController {

    private final IQuestionStudentService questionStudentService;

    @HasPermission(
            summary = "summaryMyPractice",
            description = "获取当前学生的练习汇总（跨课堂）",
            permission = PermissionConstants.STUDENT_PRACTICE_QUERY
    )
    @GetMapping("/summary/me")
    public Result<List<QuestionStudentVO>> summaryMyPractice() {
        List<QuestionStudentVO> list = questionStudentService.summaryMy();
        return Result.success(list);
    }

    @HasPermission(
            summary = "listPractice",
            description = "分页查询课堂练习作答记录",
            permission = PermissionConstants.STUDENT_PRACTICE_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listPractice(@ParameterObject QuestionStudentQueryDTO queryDTO) {
        PageInfo<QuestionStudentVO> pageInfo = questionStudentService.listPaged(queryDTO);
        return getDataTable(pageInfo);
    }

    @HasPermission(
            summary = "listAllPractice",
            description = "获取全部课堂练习作答记录",
            permission = PermissionConstants.STUDENT_PRACTICE_QUERY
    )
    @GetMapping("/all")
    public Result<List<QuestionStudentVO>> listAllPractice() {
        List<QuestionStudentVO> list = questionStudentService.listAll();
        return Result.success(list);
    }

    @HasPermission(
            summary = "listPracticeByClassroomAndStudent",
            description = "根据课堂ID和学生ID查询课堂练习作答记录",
            permission = PermissionConstants.STUDENT_PRACTICE_QUERY
    )
    @GetMapping("/classroom/{classroomId}/student/{studentId}")
    public Result<List<QuestionStudentVO>> listPracticeByClassroomAndStudent(
            @Parameter(name = "classroomId", description = "课堂ID", required = true) @PathVariable("classroomId") UUID classroomId,
            @Parameter(name = "studentId", description = "学生ID", required = true) @PathVariable("studentId") UUID studentId
    ) {
        QuestionStudentQueryDTO queryDTO = new QuestionStudentQueryDTO();
        queryDTO.setClassroomId(classroomId);
        queryDTO.setStudentId(studentId);
        List<QuestionStudentVO> list = questionStudentService.list(queryDTO);
        return Result.success(list);
    }

    @HasPermission(
            summary = "listPracticeByPractice",
            description = "根据练习ID查询课堂练习作答记录",
            permission = PermissionConstants.STUDENT_PRACTICE_QUERY
    )
    @GetMapping("/practice/{practiceId}")
    public Result<List<QuestionStudentVO>> listPracticeByPractice(
            @Parameter(name = "practiceId", description = "练习ID", required = true) @PathVariable("practiceId") UUID practiceId
    ) {
        List<QuestionStudentVO> list = questionStudentService.listByPractice(practiceId);
        return Result.success(list);
    }

    @HasPermission(
            summary = "listPracticeByPracticeAndStudent",
            description = "根据练习ID和学生ID查询课堂练习作答记录",
            permission = PermissionConstants.STUDENT_PRACTICE_QUERY
    )
    @GetMapping("/practice/{practiceId}/student/{studentId}")
    public Result<List<QuestionStudentVO>> listPracticeByPracticeAndStudent(
            @Parameter(name = "practiceId", description = "练习ID", required = true) @PathVariable("practiceId") UUID practiceId,
            @Parameter(name = "studentId", description = "学生ID", required = true) @PathVariable("studentId") UUID studentId
    ) {
        List<QuestionStudentVO> list = questionStudentService.listByPracticeAndStudent(practiceId, studentId);
        return Result.success(list);
    }

    @HasPermission(
            summary = "getPracticeStatistics",
            description = "获取练习统计信息（题目总数、正确/错误/半对数量）",
            permission = PermissionConstants.STUDENT_PRACTICE_QUERY
    )
    @GetMapping("/statistics/{practiceId}")
    public Result<PracticeStatisticsVO> getPracticeStatistics(
            @Parameter(name = "practiceId", description = "练习ID", required = true) @PathVariable("practiceId") UUID practiceId
    ) {
        PracticeStatisticsVO statistics = questionStudentService.getPracticeStatistics(practiceId);
        return Result.success(statistics);
    }

    @HasPermission(
            summary = "listPracticeByCourse",
            description = "根据课程ID查询课堂练习作答记录",
            permission = PermissionConstants.STUDENT_PRACTICE_QUERY
    )
    @GetMapping("/course/{courseId}")
    public Result<List<QuestionStudentVO>> listPracticeByCourse(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId
    ) {
        List<QuestionStudentVO> list = questionStudentService.listByCourse(courseId);
        return Result.success(list);
    }

        @HasPermission(
            summary = "getPracticeById",
            description = "根据ID获取课堂练习作答记录",
            permission = PermissionConstants.STUDENT_PRACTICE_QUERY
    )
    @GetMapping("/{id}")
    public Result<QuestionStudentVO> getPracticeById(
            @Parameter(name = "id", description = "作答记录ID", required = true) @PathVariable("id") UUID id
    ) {
        QuestionStudentVO data = questionStudentService.getById(id);
        return Result.success(data);
    }

        @HasPermission(
            summary = "addPractice",
            description = "新增课堂练习作答记录",
            permission = PermissionConstants.STUDENT_PRACTICE_ADD
    )
    @PostMapping
    public Result<Boolean> addPractice(
            @Valid @RequestBody QuestionStudentAddDTO dto
    ) {
        Boolean result = questionStudentService.add(dto);
        return Result.success(result);
    }

        @HasPermission(
            summary = "updatePractice",
            description = "更新课堂练习作答记录",
            permission = PermissionConstants.STUDENT_PRACTICE_EDIT
    )
    @PutMapping
    public Result<Boolean> updatePractice(
            @Valid @RequestBody QuestionStudentDTO dto
    ) {
        Boolean result = questionStudentService.update(dto);
        return Result.success(result);
    }

        @HasPermission(
            summary = "removePracticeById",
            description = "根据ID删除课堂练习作答记录",
            permission = PermissionConstants.STUDENT_PRACTICE_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removePracticeById(
            @Parameter(name = "id", description = "作答记录ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = questionStudentService.removeById(id);
        return Result.success(result);
    }

        @HasPermission(
            summary = "removePracticeByIds",
            description = "批量删除课堂练习作答记录",
            permission = PermissionConstants.STUDENT_PRACTICE_DELETE
    )
    @DeleteMapping
    public Result<Integer> removePracticeByIds(
            @RequestBody List<UUID> ids
    ) {
        Integer result = questionStudentService.removeByIds(ids);
        return Result.success(result);
    }
}