package com.dayz.sapientiacloud_edupivot.live.common.clients;

import com.dayz.sapientiacloud_edupivot.live.common.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.live.common.entity.dto.StudentAddDTO;
import com.dayz.sapientiacloud_edupivot.live.common.entity.dto.StudentDTO;
import com.dayz.sapientiacloud_edupivot.live.common.entity.dto.StudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.live.common.entity.vo.StudentVO;
import com.dayz.sapientiacloud_edupivot.live.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(contextId = "StudentClient", value = "SapientiaCloud-EduPivot--student", path = "/student", configuration = FeignConfig.class)
public interface StudentClient {

    @PostMapping("/internal/list")
    Result<List<StudentVO>> listStudent(@RequestBody StudentQueryDTO studentQueryDTO);

    @GetMapping("/internal/all")
    Result<List<StudentVO>> listAllStudent();

    @GetMapping("/internal/{id}")
    Result<StudentVO> getStudentById(@PathVariable("id") UUID id);

    @GetMapping("/internal/user/{id}")
    Result<StudentVO> getStudentByUserId(@PathVariable("id") UUID id);

    @PostMapping("/internal")
    Result<Boolean> addStudent(@RequestBody StudentAddDTO studentAddDTO);

    @PutMapping("/internal")
    Result<Boolean> updateStudent(@RequestBody StudentDTO studentDTO);

    @DeleteMapping("/internal/{id}")
    Result<Boolean> removeStudentById(@PathVariable("id") UUID id);

    @DeleteMapping("/internal/batch")
    Result<Integer> removeStudentByIds(@RequestBody List<UUID> ids);
}
