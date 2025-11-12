package com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "课程任务视图对象 (VO)")
public class CourseTaskVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2673654547862110870L;

    @Schema(name = "id", description = "任务ID")
    private UUID id;

    @Schema(name = "courseId", description = "所属课程ID")
    private UUID courseId;

    @Schema(name = "courseName", description = "课程名称")
    private String courseName;

    @Schema(name = "sysUserId", description = "任务发起人ID")
    private UUID sysUserId;

    @Schema(name = "teacherName", description = "任务发起人姓名")
    private String teacherName;

    @Schema(name = "teacherAvatar", description = "任务发起人头像")
    private String teacherAvatar;

    @Schema(name = "taskName", description = "任务名称")
    private String taskName;

    @Schema(name = "description", description = "任务描述")
    private String description;

    @Schema(name = "taskType", description = "任务类型 (0=作业, 1=测验, 2=项目, 3=实验)")
    private Integer taskType;

    @Schema(name = "taskContent", description = "任务内容 (富文本)")
    private String taskContent;

    @Schema(name = "attachmentUrls", description = "任务附件URL列表")
    private List<String> attachmentUrls;

    @Schema(name = "resourceUrls", description = "参考资料URL列表")
    private List<String> resourceUrls;

    @Schema(name = "maxScore", description = "满分")
    private BigDecimal maxScore;

    @Schema(name = "startTime", description = "任务开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(name = "endTime", description = "任务结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(name = "allowLateSubmit", description = "是否允许迟交 (0=不允许, 1=允许)")
    private Integer allowLateSubmit;

    @Schema(name = "maxSubmitCount", description = "最大提交次数 (0=无限制)")
    private Integer maxSubmitCount;

    @Schema(name = "autoGrade", description = "是否自动评分 (0=手动评分, 1=自动评分)")
    private Integer autoGrade;

    @Schema(name = "tags", description = "标签列表")
    private List<String> tags;

    @Schema(name = "difficulty", description = "难度等级 (1=简单, 2=中等, 3=困难)")
    private Integer difficulty;

    @Schema(name = "estimatedTime", description = "预计完成时间 (分钟)")
    private Integer estimatedTime;

    @Schema(name = "viewCount", description = "浏览次数")
    private Long viewCount;

    @Schema(name = "status", description = "任务状态 (0=草稿, 1=发布, 2=进行中, 3=已结束)")
    private Integer status;

    @Schema(name = "submitCount", description = "已提交次数")
    private Integer submitCount;

    @Schema(name = "isSubmitted", description = "是否已提交")
    private Boolean isSubmitted;

    @Schema(name = "submitTime", description = "提交时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submitTime;

    @Schema(name = "score", description = "得分")
    private BigDecimal score;

    @Schema(name = "isGraded", description = "是否已评分")
    private Boolean isGraded;

    @Schema(name = "gradeTime", description = "评分时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gradeTime;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
