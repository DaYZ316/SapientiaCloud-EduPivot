package com.dayz.sapientiacloud_edupivot.classroom.controller;

import com.dayz.sapientiacloud_edupivot.classroom.common.clients.StudentPracticeClient;
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

@Tag(name = "课堂练习管理", description = "课堂练习发布与管理相关API")
@RestController
@RequestMapping("/classroom-practice")
@RequiredArgsConstructor
public class ClassroomPracticeController extends BaseController {

    private final IClassroomQuestionService classroomQuestionService;
    private final StudentPracticeClient studentPracticeClient;

    @HasPermission(
            summary = "listClassroomPractice",
            description = "根据条件分页查询课堂内练习发布记录",
            permission = PermissionConstants.CLASSROOM_PRACTICE_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listClassroomPractice(@ParameterObject ClassroomQuestionQueryDTO dto) {
        PageInfo<ClassroomQuestionVO> pageInfo = classroomQuestionService.listPage(dto);
        return getDataTable(pageInfo);
    }

    @HasPermission(
            summary = "listByClassroomId",
            description = "根据课堂ID查询所有已发布练习",
            permission = PermissionConstants.CLASSROOM_PRACTICE_QUERY
    )
    @GetMapping("/classroom/{classroomId}")
    public Result<List<ClassroomQuestionVO>> listByClassroomId(
            @Parameter(name = "classroomId", description = "课堂记录ID", required = true) @PathVariable("classroomId") UUID classroomId
    ) {
        List<ClassroomQuestionVO> list = classroomQuestionService.listByClassroomId(classroomId);
        return Result.success(list);
    }

    @HasPermission(
            summary = "listStudentSubmissions",
            description = "获取课堂内所有学生练习作答记录（教师统计）",
            permission = PermissionConstants.CLASSROOM_PRACTICE_QUERY
    )
    @GetMapping("/submissions/{classroomId}")
    public Result<List<?>> listStudentSubmissions(
            @Parameter(name = "classroomId", description = "课堂记录ID", required = true) @PathVariable("classroomId") UUID classroomId
    ) {
        return Result.success(studentPracticeClient.listByClassroomInternal(classroomId).getData());
    }

    @HasPermission(
            summary = "addClassroomPractice",
            description = "发布课堂练习题目",
            permission = PermissionConstants.CLASSROOM_PRACTICE_ADD
    )
    @PostMapping("/add")
    public Result<ClassroomQuestionVO> addClassroomPractice(@Valid @RequestBody ClassroomQuestionDTO dto) {
        ClassroomQuestionVO vo = classroomQuestionService.add(dto);
        return Result.success(vo);
    }

    @HasPermission(
            summary = "syncClassroomPractice",
            description = "批量同步课堂练习题目（新增缺失、删除多余）",
            permission = PermissionConstants.CLASSROOM_PRACTICE_ADD
    )
    @PostMapping("/sync")
    public Result<List<ClassroomQuestionVO>> syncClassroomPractice(@Valid @RequestBody List<ClassroomQuestionDTO> dtoList) {
        List<ClassroomQuestionVO> voList = classroomQuestionService.addBatch(dtoList);
        return Result.success(voList);
    }

    @HasPermission(
            summary = "updateClassroomPractice",
            description = "更新课堂练习题目配置",
            permission = PermissionConstants.CLASSROOM_PRACTICE_EDIT
    )
    @PutMapping
    public Result<Boolean> updateClassroomPractice(@Valid @RequestBody ClassroomQuestionDTO dto) {
        return Result.success(classroomQuestionService.update(dto));
    }

    @HasPermission(
            summary = "removeClassroomPractice",
            description = "删除课堂已发布的练习题目",
            permission = PermissionConstants.CLASSROOM_PRACTICE_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeClassroomPractice(
            @Parameter(name = "id", description = "发布记录ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(classroomQuestionService.removeById(id));
    }
}