package com.dayz.sapientiacloud_edupivot.system.common.clients;

import com.dayz.sapientiacloud_edupivot.system.common.clients.vo.CourseStudentClientVO;
import com.dayz.sapientiacloud_edupivot.system.common.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.system.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(
        contextId = "CourseClient",
        value = "SapientiaCloud-EduPivot--course",
        path = "/course",
        configuration = FeignConfig.class
)
public interface CourseClient {

    /**
     * 根据课程ID查询该课程的所有学生
     *
     * @param courseId 课程ID
     * @return 学生列表（包含 sysUserId）
     */
    @GetMapping("/internal/{courseId}/students")
    Result<List<CourseStudentClientVO>> listStudentsByCourseId(@PathVariable("courseId") UUID courseId);

    /**
     * 获取课程总数
     *
     * @return 课程总数
     */
    @GetMapping("/internal/count")
    Result<Long> getCourseCount();
}


