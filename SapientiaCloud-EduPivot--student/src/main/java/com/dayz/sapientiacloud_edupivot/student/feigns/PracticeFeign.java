package com.dayz.sapientiacloud_edupivot.student.feigns;

import com.dayz.sapientiacloud_edupivot.student.common.result.Result;
import com.dayz.sapientiacloud_edupivot.student.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.student.entity.po.QuestionStudent;
import com.dayz.sapientiacloud_edupivot.student.service.IQuestionStudentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "学生练习内部接口", description = "学生练习数据对内服务接口")
@RestController
@RequestMapping("/practice")
@RequiredArgsConstructor
public class PracticeFeign {

    private final IQuestionStudentService questionStudentService;

    @HasPermission(summary = "listByClassroomInternal", description = "根据课堂ID查询所有学生作答记录（内部接口）")
    @GetMapping("/internal/classroom/{classroomId}")
    public Result<List<QuestionStudent>> listByClassroomInternal(
            @Parameter(name = "classroomId", description = "课堂记录ID", required = true) @PathVariable("classroomId") UUID classroomId
    ) {
        List<QuestionStudent> list = questionStudentService.listByClassroom(classroomId);
        return Result.success(list);
    }
}
