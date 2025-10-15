package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import com.dayz.sapientiacloud_edupivot.course.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程任务查询数据传输对象")
public class CourseTaskQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -5622883214353612807L;

    @Schema(name = "courseId", description = "所属课程ID")
    private UUID courseId;

    @Schema(name = "sysUserId", description = "任务发起人ID")
    private UUID sysUserId;

    @Schema(name = "taskName", description = "任务名称（模糊查询）")
    private String taskName;

    @Schema(name = "taskType", description = "任务类型 (0=作业, 1=测验, 2=项目, 3=实验)")
    private Integer taskType;

    @Schema(name = "difficulty", description = "难度等级 (1=简单, 2=中等, 3=困难)")
    private Integer difficulty;

    @Schema(name = "status", description = "任务状态 (0=草稿, 1=发布, 2=进行中, 3=已结束)")
    private Integer status;

    @Schema(name = "autoGrade", description = "是否自动评分 (0=手动评分, 1=自动评分)")
    private Integer autoGrade;

    @Schema(name = "allowLateSubmit", description = "是否允许迟交 (0=不允许, 1=允许)")
    private Integer allowLateSubmit;

    @Schema(name = "tag", description = "标签（模糊查询）")
    private String tag;

    @Schema(name = "minScore", description = "最小分数")
    private BigDecimal minScore;

    @Schema(name = "maxScore", description = "最大分数")
    private BigDecimal maxScore;

    @Schema(name = "minEstimatedTime", description = "最小预计完成时间（分钟）")
    private Integer minEstimatedTime;

    @Schema(name = "maxEstimatedTime", description = "最大预计完成时间（分钟）")
    private Integer maxEstimatedTime;
}
