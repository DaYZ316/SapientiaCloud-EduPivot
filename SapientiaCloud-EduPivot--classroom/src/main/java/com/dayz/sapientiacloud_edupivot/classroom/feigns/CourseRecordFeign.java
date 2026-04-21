package com.dayz.sapientiacloud_edupivot.classroom.feigns;

import com.dayz.sapientiacloud_edupivot.classroom.common.result.Result;
import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.CourseRecordVO;
import com.dayz.sapientiacloud_edupivot.classroom.service.ICourseRecordService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Course Record Internal API", description = "Internal course record APIs for service calls")
@RestController
@RequestMapping("/course-record")
@RequiredArgsConstructor
public class CourseRecordFeign {

    private final ICourseRecordService courseRecordService;

    @GetMapping("/internal/{id}")
    public Result<CourseRecordVO> getCourseRecordById(
            @Parameter(name = "id", description = "Course record ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(courseRecordService.getCourseRecordById(id));
    }
}
