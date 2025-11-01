package com.dayz.sapientiacloud_edupivot.auth.clients;

import com.dayz.sapientiacloud_edupivot.auth.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.TeacherAddDTO;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "TeacherClient", value = "SapientiaCloud-EduPivot--teacher", path = "/teacher", configuration = FeignConfig.class)
public interface TeacherClient {

    @PostMapping("/internal")
    Result<Boolean> addTeacher(@RequestBody TeacherAddDTO teacherAddDTO);
}

