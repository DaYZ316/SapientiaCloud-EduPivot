package com.dayz.sapientiacloud_edupivot.student.common.clients;

import com.dayz.sapientiacloud_edupivot.student.common.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.student.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;
import java.util.UUID;

@FeignClient(contextId = "QuestionClient", value = "SapientiaCloud-EduPivot--course", path = "/course", configuration = FeignConfig.class)
public interface QuestionClient {

    @GetMapping("/internal/question/{id}")
    Result<Map<String, Object>> getQuestionById(@PathVariable("id") UUID id);
}

