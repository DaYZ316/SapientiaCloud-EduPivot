package com.dayz.sapientiacloud_edupivot.classroom.entity.dto;

import com.dayz.sapientiacloud_edupivot.classroom.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程教学学生参与查询数据传输对象")
public class CourseRecordStudentQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -487207050366788845L;

    @Schema(description = "课程记录ID")
    private UUID recordId;

    @Schema(description = "学生ID")
    private UUID studentId;

    @Schema(description = "课程ID")
    private UUID courseId;

    @Schema(description = "座位状态 (0=正常, 2=已预留, 3=已占用)")
    private Integer seatStatus;
}
