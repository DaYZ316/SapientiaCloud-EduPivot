package com.dayz.sapientiacloud_edupivot.classroom.entity.dto;

import com.dayz.sapientiacloud_edupivot.classroom.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程教学记录查询数据传输对象")
public class CourseRecordQueryDTO extends PageEntity {

    @Serial
    private static final long serialVersionUID = -5139529736459014006L;

    @Schema(description = "关联课程ID")
    private UUID courseId;

    @Schema(description = "授课教师ID")
    private UUID teacherId;

    @Schema(description = "课程名称（模糊查询）")
    private String courseName;

    @Schema(description = "教室类型 (0=小型教室, 1=中型教室, 2=大型教室, 3=超大型教室)")
    private Integer classroomType;

    @Schema(description = "课程状态 (0=未开始, 1=进行中, 2=已结束, 3=已取消)")
    private Integer status;

    @Schema(description = "开始时间范围 - 起始")
    private LocalDateTime startTimeBegin;

    @Schema(description = "开始时间范围 - 结束")
    private LocalDateTime startTimeEnd;
}
