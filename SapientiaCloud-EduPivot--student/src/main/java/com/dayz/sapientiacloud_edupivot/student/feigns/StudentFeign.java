package com.dayz.sapientiacloud_edupivot.student.feigns;

import com.dayz.sapientiacloud_edupivot.student.common.result.Result;
import com.dayz.sapientiacloud_edupivot.student.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentAddDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.StudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.vo.StudentVO;
import com.dayz.sapientiacloud_edupivot.student.service.IStudentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "学生内部接口", description = "学生内部管理接口")
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
@Slf4j
public class StudentFeign {

    private final IStudentService studentService;

    @HasPermission(
            summary = "listStudent",
            description = "根据传入的条件分页查询学生信息。支持根据学号、姓名、专业等字段进行模糊查询。"
    )
    @GetMapping("/internal/list")
    public Result<List<StudentVO>> listStudent(@RequestBody StudentQueryDTO studentQueryDTO) {
        List<StudentVO> studentVOList = studentService.listStudent(studentQueryDTO).getList();
        return Result.success(studentVOList);
    }

    @HasPermission(
            summary = "listAllStudent",
            description = "获取系统中所有的学生信息。"
    )
    @GetMapping("/internal/all")
    public Result<List<StudentVO>> listAllStudent() {
        List<StudentVO> studentVOList = studentService.listAllStudent();
        return Result.success(studentVOList);
    }

    @HasPermission(
            summary = "getStudentById",
            description = "通过学生的唯一ID获取其详细信息。"
    )
    @GetMapping("/internal/{id}")
    public Result<StudentVO> getStudentById(
            @Parameter(name = "id", description = "学生ID", required = true) @PathVariable("id") UUID id
    ) {
        StudentVO studentVO = studentService.getStudentById(id);
        return Result.success(studentVO);
    }

    @HasPermission(
            summary = "getStudentByUserId",
            description = "通过学生的用户ID获取其详细信息。"
    )
    @GetMapping("/internal/user/{id}")
    public Result<StudentVO> getStudentByUserId(
            @Parameter(name = "id", description = "用户ID", required = true) @PathVariable("id") UUID id
    ) {
        StudentVO studentVO = studentService.getStudentByUserId(id);
        return Result.success(studentVO);
    }

    @HasPermission(
            summary = "addStudent",
            description = "向系统中添加一个新的学生。"
    )
    @PostMapping("/internal")
    public Result<Boolean> addStudent(
            @RequestBody StudentAddDTO studentAddDTO
    ) {
        Boolean result = studentService.addStudent(studentAddDTO);
        return Result.success(result);
    }

    @HasPermission(
            summary = "updateStudent",
            description = "更新现有学生的信息。"
    )
    @PutMapping("/internal")
    public Result<Boolean> updateStudent(
            @RequestBody StudentDTO studentDTO
    ) {
        Boolean result = studentService.updateStudent(studentDTO);
        return Result.success(result);
    }

    @HasPermission(
            summary = "removeStudentById",
            description = "通过学生的唯一ID删除学生信息。"
    )
    @DeleteMapping("/internal/{id}")
    public Result<Boolean> removeStudentById(
            @Parameter(name = "id", description = "学生ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = studentService.removeStudentById(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "removeStudentByIds",
            description = "根据学生ID列表批量删除学生信息。"
    )
    @DeleteMapping("/internal/batch")
    public Result<Integer> removeStudentByIds(
            @RequestBody List<UUID> ids
    ) {
        Integer result = studentService.removeStudentByIds(ids);
        return Result.success(result);
    }
}
