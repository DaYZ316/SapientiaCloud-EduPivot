package com.dayz.sapientiacloud_edupivot.teacher.feigns;

import com.dayz.sapientiacloud_edupivot.teacher.common.result.Result;
import com.dayz.sapientiacloud_edupivot.teacher.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.teacher.entity.dto.TeacherAddDTO;
import com.dayz.sapientiacloud_edupivot.teacher.entity.dto.TeacherDTO;
import com.dayz.sapientiacloud_edupivot.teacher.entity.dto.TeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.teacher.entity.vo.TeacherVO;
import com.dayz.sapientiacloud_edupivot.teacher.service.ITeacherService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "教师内部接口", description = "教师内部管理接口")
@RestController
@RequestMapping("/teacher")
@RequiredArgsConstructor
public class TeacherFeign {

    private final ITeacherService teacherService;

    @HasPermission(
            summary = "listTeacher",
            description = "根据传入的条件分页查询教师信息。支持根据工号、姓名、部门等字段进行模糊查询。"
    )
    @GetMapping("/internal/list")
    public Result<List<TeacherVO>> listTeacher(@RequestBody TeacherQueryDTO teacherQueryDTO) {
        List<TeacherVO> teacherVOList = teacherService.listTeacher(teacherQueryDTO).getList();
        return Result.success(teacherVOList);
    }

    @HasPermission(
            summary = "listAllTeacher",
            description = "获取系统中所有的教师信息。"
    )
    @GetMapping("/internal/all")
    public Result<List<TeacherVO>> listAllTeacher() {
        List<TeacherVO> teacherVOList = teacherService.listAllTeacher();
        return Result.success(teacherVOList);
    }

    @HasPermission(
            summary = "getTeacherById",
            description = "通过教师的唯一ID获取其详细信息。"
    )
    @GetMapping("/internal/{id}")
    public Result<TeacherVO> getTeacherById(
            @Parameter(name = "id", description = "教师ID", required = true) @PathVariable("id") UUID id
    ) {
        TeacherVO teacherVO = teacherService.getTeacherById(id);
        return Result.success(teacherVO);
    }

    @HasPermission(
            summary = "getTeacherByUserId",
            description = "通过用户的唯一ID获取其关联的教师信息。"
    )
    @GetMapping("/internal/user/{id}")
    public Result<TeacherVO> getTeacherByUserId(
            @Parameter(name = "id", description = "用户ID", required = true) @PathVariable("id") UUID id
    ) {
        TeacherVO teacherVO = teacherService.getTeacherByUserId(id);
        return Result.success(teacherVO);
    }

    @HasPermission(
            summary = "addTeacher",
            description = "向系统中添加一个新的教师。"
    )
    @PostMapping("/internal")
    public Result<Boolean> addTeacher(
            @RequestBody TeacherAddDTO teacherAddDTO
    ) {
        Boolean result = teacherService.addTeacher(teacherAddDTO);
        return Result.success(result);
    }

    @HasPermission(
            summary = "updateTeacher",
            description = "更新现有教师的信息。"
    )
    @PutMapping("/internal")
    public Result<Boolean> updateTeacher(
            @RequestBody TeacherDTO teacherDTO
    ) {
        Boolean result = teacherService.updateTeacher(teacherDTO);
        return Result.success(result);
    }

    @HasPermission(
            summary = "removeTeacherById",
            description = "通过教师的唯一ID删除教师信息。"
    )
    @DeleteMapping("/internal/{id}")
    public Result<Boolean> removeTeacherById(
            @Parameter(name = "id", description = "教师ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = teacherService.removeTeacherById(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "removeTeacherByIds",
            description = "根据教师ID列表批量删除教师信息。"
    )
    @DeleteMapping("/internal/batch")
    public Result<Integer> removeTeacherByIds(
            @RequestBody List<UUID> ids
    ) {
        Integer result = teacherService.removeTeacherByIds(ids);
        return Result.success(result);
    }
}
