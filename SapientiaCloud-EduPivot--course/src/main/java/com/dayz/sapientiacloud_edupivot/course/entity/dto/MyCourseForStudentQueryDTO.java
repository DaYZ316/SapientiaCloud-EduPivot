package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import com.dayz.sapientiacloud_edupivot.course.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "我的课程查询数据传输对象")
public class MyCourseForStudentQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 6948071869021503294L;

    @Schema(name = "courseName", description = "课程名称（模糊查询）")
    private String courseName;

    @Schema(name = "courseType", description = "课程类型")
    private Integer courseType;

    @Schema(name = "status", description = "课程状态 (0=正常, 1=停课)")
    private Integer status;

    @Schema(name = "studentId", description = "学生ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NonNull
    private UUID studentId;

    @Schema(name = "isPublic", description = "是否公开 (0=仅课程成员, 1=公开)")
    private Integer isPublic;
}
