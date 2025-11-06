package com.dayz.sapientiacloud_edupivot.auth.clients;

import com.dayz.sapientiacloud_edupivot.auth.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.auth.entity.dto.StudentAddDTO;
import com.dayz.sapientiacloud_edupivot.auth.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "StudentClient", value = "SapientiaCloud-EduPivot--student", path = "/student", configuration = FeignConfig.class)
public interface StudentClient {

    @PostMapping("/internal")
    Result<Boolean> addStudent(@RequestBody StudentAddDTO studentAddDTO);
}

