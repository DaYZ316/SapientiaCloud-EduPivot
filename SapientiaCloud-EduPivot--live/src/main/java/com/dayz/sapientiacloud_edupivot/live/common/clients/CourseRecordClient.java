package com.dayz.sapientiacloud_edupivot.live.common.clients;

import com.dayz.sapientiacloud_edupivot.live.common.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.live.common.entity.vo.CourseRecordVO;
import com.dayz.sapientiacloud_edupivot.live.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        contextId = "CourseRecordClient",
        value = "SapientiaCloud-EduPivot--classroom",
        path = "/course-record",
        configuration = FeignConfig.class
)
public interface CourseRecordClient {

    @GetMapping("/internal/{id}")
    Result<CourseRecordVO> getCourseRecordById(@PathVariable("id") UUID id);
}
