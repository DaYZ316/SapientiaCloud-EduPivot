package com.dayz.sapientiacloud_edupivot.student.controller;

import com.dayz.sapientiacloud_edupivot.student.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.student.common.result.Result;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentSubmitDTO;
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

    /*@HasPermission(
            summary = "submitPractice",
            description = "学生提交课堂练习作答",
            permission = PermissionConstants.STUDENT_PRACTICE_ADD
    )*/
    @PostMapping("/submit/{classroomId}")
    public Result<QuestionStudent> submitPractice(
            @Parameter(name = "classroomId", description = "课堂记录ID", required = true) @PathVariable("classroomId") UUID classroomId,
            @Valid @RequestBody QuestionStudentSubmitDTO dto
    ) {
        QuestionStudent saved = questionStudentService.submit(classroomId, dto);
        return Result.success(saved);
    }

    /*@HasPermission(
            summary = "listMyPracticeByClassroom",
            description = "获取当前学生在指定课堂的练习作答记录",
            permission = PermissionConstants.STUDENT_PRACTICE_QUERY
    )*/
    @GetMapping("/me/{classroomId}")
    public Result<List<QuestionStudent>> listMyPracticeByClassroom(
            @Parameter(name = "classroomId", description = "课堂记录ID", required = true) @PathVariable("classroomId") UUID classroomId
    ) {
        List<QuestionStudent> list = questionStudentService.listMyByClassroom(classroomId);
        return Result.success(list);
    }

    /*@HasPermission(
            summary = "summaryMyPractice",
            description = "获取当前学生的练习汇总（跨课堂）",
            permission = PermissionConstants.STUDENT_PRACTICE_QUERY
    )*/
    @GetMapping("/summary/me")
    public Result<List<QuestionStudent>> summaryMyPractice() {
        List<QuestionStudent> list = questionStudentService.summaryMy();
        return Result.success(list);
    }
}