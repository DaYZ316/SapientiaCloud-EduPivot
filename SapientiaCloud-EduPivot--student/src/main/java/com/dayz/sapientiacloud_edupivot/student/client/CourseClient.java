package com.dayz.sapientiacloud_edupivot.student.client;

import com.dayz.sapientiacloud_edupivot.student.common.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.student.common.result.Result;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.CourseStudentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "CourseClient", value = "SapientiaCloud-EduPivot--course", path = "/course", configuration = FeignConfig.class)
public interface CourseClient {

    @PutMapping("/internal/course-student")
    Result<Boolean> updateCourseStudent(@RequestBody CourseStudentDTO courseStudentDTO);
}
