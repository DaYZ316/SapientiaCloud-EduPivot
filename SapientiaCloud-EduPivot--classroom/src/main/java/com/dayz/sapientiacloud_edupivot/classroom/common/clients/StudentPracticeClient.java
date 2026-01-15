package com.dayz.sapientiacloud_edupivot.classroom.common.clients;

import com.dayz.sapientiacloud_edupivot.classroom.common.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.classroom.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(contextId = "StudentPracticeClient", value = "SapientiaCloud-EduPivot--student", path = "/practice", configuration = FeignConfig.class)
public interface StudentPracticeClient {

    @GetMapping("/internal/classroom/{classroomId}")
    Result<List<Map<String, Object>>> listByClassroomInternal(@PathVariable("classroomId") UUID classroomId);
}