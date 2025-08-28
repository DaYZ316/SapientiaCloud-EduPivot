package com.dayz.sapientiacloud_edupivot.student.controller;

import com.dayz.sapientiacloud_edupivot.student.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.student.common.result.Result;
import com.dayz.sapientiacloud_edupivot.student.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentAddDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.vo.StudentVO;
import com.dayz.sapientiacloud_edupivot.student.service.IStudentService;
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

@Tag(name = "学生管理", description = "用于管理学生信息的API")
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController extends BaseController {

    private final IStudentService studentService;

    @Operation(summary = "分页查询学生列表", description = "根据传入的条件分页查询学生信息。支持根据学号、姓名、专业等字段进行模糊查询。")
    @GetMapping("/list")
    public TableDataResult listStudent(@ParameterObject StudentQueryDTO studentQueryDTO) {
        startPage();
        PageInfo<StudentVO> pageInfo = studentService.listStudentPage(studentQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @Operation(summary = "获取所有学生", description = "获取系统中所有的学生信息。")
    @GetMapping("/all")
    public Result<List<StudentVO>> listAllStudent() {
        List<StudentVO> studentVOList = studentService.listAllStudent();
        return Result.success(studentVOList);
    }

    @Operation(summary = "根据ID获取学生信息", description = "通过学生的唯一ID获取其详细信息。")
    @GetMapping("/{id}")
    public Result<StudentVO> getStudentById(
            @Parameter(name = "id", description = "学生ID", required = true) @PathVariable("id") UUID id
    ) {
        StudentVO studentVO = studentService.getStudentById(id);
        return Result.success(studentVO);
    }

    @Operation(summary = "根据用户ID获取学生信息", description = "通过学生的用户ID获取其详细信息。")
    @GetMapping("/user/{id}")
    public Result<StudentVO> getStudentBySysUserId(
            @Parameter(name = "id", description = "用户ID", required = true) @PathVariable("id") UUID id
    ) {
        StudentVO studentVO = studentService.getStudentBySysUserId(id);
        return Result.success(studentVO);
    }

    @Operation(summary = "添加新学生", description = "向系统中添加一个新的学生。")
    @PostMapping
    public Result<Boolean> addStudent(
            @RequestBody @Valid StudentAddDTO studentAddDTO
    ) {
        Boolean result = studentService.addStudent(studentAddDTO);
        return Result.success(result);
    }

    @Operation(summary = "更新学生信息", description = "更新现有学生的信息。")
    @PutMapping
    public Result<Boolean> updateStudent(
            @RequestBody @Valid StudentDTO studentDTO
    ) {
        Boolean result = studentService.updateStudent(studentDTO);
        return Result.success(result);
    }

    @Operation(summary = "根据ID删除学生", description = "通过学生的唯一ID删除学生信息。")
    @DeleteMapping("/{id}")
    public Result<Boolean> removeStudentById(
            @Parameter(name = "id", description = "学生ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = studentService.removeStudentById(id);
        return Result.success(result);
    }

    @Operation(summary = "批量删除学生", description = "根据学生ID列表批量删除学生信息。")
    @DeleteMapping
    public Result<Integer> removeStudentByIds(
            @RequestBody List<UUID> ids
    ) {
        Integer result = studentService.removeStudentByIds(ids);
        return Result.success(result);
    }
}