package com.dayz.sapientiacloud_edupivot.student.controller;

import com.dayz.sapientiacloud_edupivot.student.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.student.common.result.Result;
import com.dayz.sapientiacloud_edupivot.student.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.student.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.student.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentAddDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.po.QuestionStudent;
import com.dayz.sapientiacloud_edupivot.student.service.IQuestionStudentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public Result<List<QuestionStudent>> summaryMyPractice() {
        List<QuestionStudent> list = questionStudentService.summaryMy();
        return Result.success(list);
    }

    @HasPermission(
            summary = "listPractice",
            description = "分页查询课堂练习作答记录",
            permission = PermissionConstants.STUDENT_PRACTICE_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listPractice(
            @Parameter(name = "classroomId", description = "课堂记录ID") @RequestParam(value = "classroomId", required = false) UUID classroomId,
            @Parameter(name = "studentId", description = "学生ID") @RequestParam(value = "studentId", required = false) UUID studentId,
            @Parameter(name = "pageNum", description = "当前页码", example = "1")
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(name = "pageSize", description = "每页记录数", example = "10")
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        startPage(pageNum, pageSize);
        List<QuestionStudent> list = questionStudentService.list(classroomId, studentId);
        return getDataTable(list);
    }

    @HasPermission(
            summary = "listAllPractice",
            description = "获取全部课堂练习作答记录",
            permission = PermissionConstants.STUDENT_PRACTICE_QUERY
    )
    @GetMapping("/all")
    public Result<List<QuestionStudent>> listAllPractice() {
        List<QuestionStudent> list = questionStudentService.listAll();
        return Result.success(list);
    }

    @HasPermission(
            summary = "getPracticeById",
            description = "根据ID获取课堂练习作答记录",
            permission = PermissionConstants.STUDENT_PRACTICE_QUERY
    )
    @GetMapping("/{id}")
    public Result<QuestionStudent> getPracticeById(
            @Parameter(name = "id", description = "作答记录ID", required = true) @PathVariable("id") UUID id
    ) {
        QuestionStudent data = questionStudentService.getById(id);
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