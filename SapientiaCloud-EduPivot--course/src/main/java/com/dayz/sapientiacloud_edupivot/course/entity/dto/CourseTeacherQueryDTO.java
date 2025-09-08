package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import com.dayz.sapientiacloud_edupivot.course.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "教师课程分页查询数据传输对象")
public class CourseTeacherQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 7756280467062573716L;

    @Schema(name = "teacherId", description = "教师ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID teacherId;
}
