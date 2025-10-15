package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "课程任务数据传输对象")
public class CourseTaskDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5017676351834399371L;

    @Schema(name = "id", description = "任务ID，更新时必须提供")
    private UUID id;

    @Schema(name = "courseId", description = "所属课程ID")
    @NotNull(message = "课程ID不能为空")
    private UUID courseId;

    @Schema(name = "sysUserId", description = "任务发起人ID")
    @NotNull(message = "任务发起人ID不能为空")
    private UUID sysUserId;

    @Schema(name = "taskName", description = "任务名称")
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 200, message = "任务名称不能超过200个字符")
    private String taskName;

    @Schema(name = "description", description = "任务描述")
    @Size(max = 1000, message = "任务描述不能超过1000个字符")
    private String description;

    @Schema(name = "taskType", description = "任务类型 (0=作业, 1=测验, 2=项目, 3=实验)")
    @NotNull(message = "任务类型不能为空")
    @Min(value = 0, message = "任务类型输入不正确")
    @Max(value = 3, message = "任务类型输入不正确")
    private Integer taskType;

    @Schema(name = "taskContent", description = "任务内容 (富文本)")
    @NotBlank(message = "任务内容不能为空")
    private String taskContent;

    @Schema(name = "attachmentUrls", description = "任务附件URL列表")
    private List<String> attachmentUrls;

    @Schema(name = "resourceUrls", description = "参考资料URL列表")
    private List<String> resourceUrls;

    @Schema(name = "maxScore", description = "满分")
    @NotNull(message = "满分不能为空")
    @DecimalMin(value = "0.01", message = "满分必须大于0")
    @Digits(integer = 10, fraction = 2, message = "满分格式不正确")
    private BigDecimal maxScore;

    @Schema(name = "startTime", description = "任务开始时间")
    private LocalDateTime startTime;

    @Schema(name = "endTime", description = "任务结束时间")
    private LocalDateTime endTime;

    @Schema(name = "allowLateSubmit", description = "是否允许迟交 (0=不允许, 1=允许)")
    @Min(value = 0, message = "迟交设置值无效")
    @Max(value = 1, message = "迟交设置值无效")
    private Integer allowLateSubmit;

    @Schema(name = "maxSubmitCount", description = "最大提交次数 (0=无限制)")
    @Min(value = 0, message = "最大提交次数不能小于0")
    private Integer maxSubmitCount;

    @Schema(name = "autoGrade", description = "是否自动评分 (0=手动评分, 1=自动评分)")
    @Min(value = 0, message = "自动评分设置值无效")
    @Max(value = 1, message = "自动评分设置值无效")
    private Integer autoGrade;

    @Schema(name = "tags", description = "标签列表")
    private List<String> tags;

    @Schema(name = "difficulty", description = "难度等级 (1=简单, 2=中等, 3=困难)")
    @Min(value = 1, message = "难度等级值无效")
    @Max(value = 3, message = "难度等级值无效")
    private Integer difficulty;

    @Schema(name = "estimatedTime", description = "预计完成时间 (分钟)")
    @Min(value = 1, message = "预计完成时间必须大于0")
    private Integer estimatedTime;

    @Schema(name = "status", description = "任务状态 (0=草稿, 1=发布, 2=进行中, 3=已结束)")
    @Min(value = 0, message = "任务状态值无效")
    @Max(value = 3, message = "任务状态值无效")
    private Integer status;
}
