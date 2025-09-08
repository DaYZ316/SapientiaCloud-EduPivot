package com.dayz.sapientiacloud_edupivot.teacher.controller;

import com.dayz.sapientiacloud_edupivot.teacher.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.teacher.common.result.Result;
import com.dayz.sapientiacloud_edupivot.teacher.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.teacher.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.teacher.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.teacher.entity.dto.TeacherAddDTO;
import com.dayz.sapientiacloud_edupivot.teacher.entity.dto.TeacherDTO;
import com.dayz.sapientiacloud_edupivot.teacher.entity.dto.TeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.teacher.entity.vo.TeacherVO;
import com.dayz.sapientiacloud_edupivot.teacher.service.ITeacherService;
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

@Tag(name = "教师管理", description = "用于管理教师信息的API")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class TeacherController extends BaseController {

    private final ITeacherService teacherService;

    @HasPermission(
            summary = "listTeacher",
            description = "根据传入的条件分页查询教师信息。支持根据工号、姓名、部门等字段进行模糊查询。",
            permission = PermissionConstants.TEACHER_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listTeacher(@ParameterObject TeacherQueryDTO teacherQueryDTO) {
        startPage();
        PageInfo<TeacherVO> pageInfo = teacherService.listTeacher(teacherQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "listAllTeacher",
            description = "获取系统中所有的教师信息。",
            permission = PermissionConstants.TEACHER_QUERY
    )
    @GetMapping("/all")
    public Result<List<TeacherVO>> listAllTeacher() {
        List<TeacherVO> teacherVOList = teacherService.listAllTeacher();
        return Result.success(teacherVOList);
    }

    @HasPermission(
            summary = "getTeacherById",
            description = "通过教师的唯一ID获取其详细信息。",
            permission = PermissionConstants.TEACHER_QUERY
    )
    @GetMapping("/{id}")
    public Result<TeacherVO> getTeacherById(
            @Parameter(name = "id", description = "教师ID", required = true) @PathVariable("id") UUID id
    ) {
        TeacherVO teacherVO = teacherService.getTeacherById(id);
        return Result.success(teacherVO);
    }

    @HasPermission(
            summary = "getTeacherByUserId",
            description = "通过用户的唯一ID获取其关联的教师信息。",
            permission = PermissionConstants.TEACHER_QUERY
    )
    @GetMapping("/user/{id}")
    public Result<TeacherVO> getTeacherByUserId(
            @Parameter(name = "id", description = "用户ID", required = true) @PathVariable("id") UUID id
    ) {
        TeacherVO teacherVO = teacherService.getTeacherByUserId(id);
        return Result.success(teacherVO);
    }

    @HasPermission(
            summary = "addTeacher",
            description = "向系统中添加一个新的教师。",
            permission = PermissionConstants.TEACHER_ADD
    )
    @PostMapping
    public Result<Boolean> addTeacher(
            @RequestBody @Valid TeacherAddDTO teacherAddDTO
    ) {
        Boolean result = teacherService.addTeacher(teacherAddDTO);
        return Result.success(result);
    }

    @HasPermission(
            summary = "updateTeacher",
            description = "更新现有教师的信息。",
            permission = PermissionConstants.TEACHER_EDIT
    )
    @PutMapping
    public Result<Boolean> updateTeacher(
            @RequestBody @Valid TeacherDTO teacherDTO
    ) {
        Boolean result = teacherService.updateTeacher(teacherDTO);
        return Result.success(result);
    }

    @HasPermission(
            summary = "removeTeacherById",
            description = "通过教师的唯一ID删除教师信息。",
            permission = PermissionConstants.TEACHER_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeTeacherById(
            @Parameter(name = "id", description = "教师ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = teacherService.removeTeacherById(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "removeTeacherByIds",
            description = "根据教师ID列表批量删除教师信息。",
            permission = PermissionConstants.TEACHER_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeTeacherByIds(
            @RequestBody List<UUID> ids
    ) {
        Integer result = teacherService.removeTeacherByIds(ids);
        return Result.success(result);
    }
}
