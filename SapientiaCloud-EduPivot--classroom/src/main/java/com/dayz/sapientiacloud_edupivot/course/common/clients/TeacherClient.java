package com.dayz.sapientiacloud_edupivot.course.common.clients;

import com.dayz.sapientiacloud_edupivot.course.common.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.course.common.entity.dto.TeacherAddDTO;
import com.dayz.sapientiacloud_edupivot.course.common.entity.dto.TeacherDTO;
import com.dayz.sapientiacloud_edupivot.course.common.entity.dto.TeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.TeacherVO;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(contextId = "TeacherClient", value = "SapientiaCloud-EduPivot--teacher", path = "/teacher", configuration = FeignConfig.class)
public interface TeacherClient {

    @PostMapping("/internal/list")
    Result<List<TeacherVO>> listTeacher(@RequestBody TeacherQueryDTO teacherQueryDTO);

    @GetMapping("/internal/all")
    Result<List<TeacherVO>> listAllTeacher();

    @GetMapping("/internal/{id}")
    Result<TeacherVO> getTeacherById(@PathVariable("id") UUID id);

    @GetMapping("/internal/user/{id}")
    Result<TeacherVO> getTeacherByUserId(@PathVariable("id") UUID id);

    @PostMapping("/internal")
    Result<Boolean> addTeacher(@RequestBody TeacherAddDTO teacherAddDTO);

    @PutMapping("/internal")
    Result<Boolean> updateTeacher(@RequestBody TeacherDTO teacherDTO);

    @DeleteMapping("/internal/{id}")
    Result<Boolean> removeTeacherById(@PathVariable("id") UUID id);

    @DeleteMapping("/internal/batch")
    Result<Integer> removeTeacherByIds(@RequestBody List<UUID> ids);
}
