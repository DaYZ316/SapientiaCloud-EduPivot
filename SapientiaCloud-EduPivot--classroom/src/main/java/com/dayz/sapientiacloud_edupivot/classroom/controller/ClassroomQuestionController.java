package com.dayz.sapientiacloud_edupivot.classroom.controller;

import com.dayz.sapientiacloud_edupivot.classroom.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.classroom.common.result.Result;
import com.dayz.sapientiacloud_edupivot.classroom.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.classroom.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.classroom.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.ClassroomQuestionDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.ClassroomQuestionQueryDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.ClassroomQuestionVO;
import com.dayz.sapientiacloud_edupivot.classroom.service.IClassroomQuestionService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "课堂题目管理", description = "课堂题目发布记录管理相关API")
@RestController
@RequestMapping("/classroom-question")
@RequiredArgsConstructor
public class ClassroomQuestionController extends BaseController {

    private final IClassroomQuestionService classroomQuestionService;

    @HasPermission(
            summary = "listClassroomQuestion",
            description = "根据条件分页查询课堂题目列表",
            permission = PermissionConstants.COURSE_RECORD_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listClassroomQuestion(@ParameterObject ClassroomQuestionQueryDTO dto) {
        PageInfo<ClassroomQuestionVO> pageInfo = classroomQuestionService.listPage(dto);
        return getDataTable(pageInfo);
    }

    @HasPermission(
            summary = "listByClassroomId",
            description = "根据课堂ID查询所有题目",
            permission = PermissionConstants.COURSE_RECORD_QUERY
    )
    @GetMapping("/classroom/{classroomId}")
    public Result<List<ClassroomQuestionVO>> listByClassroomId(
            @Parameter(name = "classroomId", description = "课堂记录ID", required = true) @PathVariable("classroomId") UUID classroomId
    ) {
        List<ClassroomQuestionVO> list = classroomQuestionService.listByClassroomId(classroomId);
        return Result.success(list);
    }

    @HasPermission(
            summary = "addClassroomQuestion",
            description = "添加课堂题目",
            permission = PermissionConstants.COURSE_RECORD_ADD
    )
    @PostMapping("/add")
    public Result<ClassroomQuestionVO> addClassroomQuestion(@Valid @RequestBody ClassroomQuestionDTO dto) {
        ClassroomQuestionVO vo = classroomQuestionService.add(dto);
        return Result.success(vo);
    }

    @HasPermission(
            summary = "updateClassroomQuestion",
            description = "更新课堂题目",
            permission = PermissionConstants.COURSE_RECORD_EDIT
    )
    @PutMapping("/update")
    public Result<Boolean> updateClassroomQuestion(@Valid @RequestBody ClassroomQuestionDTO dto) {
        return Result.success(classroomQuestionService.update(dto));
    }

    @HasPermission(
            summary = "removeClassroomQuestionById",
            description = "根据ID删除课堂题目",
            permission = PermissionConstants.COURSE_RECORD_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeClassroomQuestionById(
            @Parameter(name = "id", description = "记录ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(classroomQuestionService.removeById(id));
    }
}

