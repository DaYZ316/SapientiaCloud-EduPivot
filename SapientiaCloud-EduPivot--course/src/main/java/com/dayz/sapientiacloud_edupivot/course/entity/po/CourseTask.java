package com.dayz.sapientiacloud_edupivot.course.entity.po;

import com.dayz.sapientiacloud_edupivot.course.common.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "mg_course_task")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程任务持久化对象 (PO)")
public class CourseTask extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 759792035539915981L;

    @Schema(name = "id", description = "任务ID")
    @Id
    private UUID id;

    @Schema(name = "courseId", description = "所属课程ID")
    @Field("course_id")
    private UUID courseId;

    @Schema(name = "sysUserId", description = "任务发起人ID")
    @Field("sys_user_id")
    private UUID sysUserId;

    @Schema(name = "taskName", description = "任务名称")
    @Field("task_name")
    private String taskName;

    @Schema(name = "description", description = "任务描述")
    @Field("description")
    private String description;

    @Schema(name = "taskType", description = "任务类型 (0=作业, 1=测验, 2=项目, 3=实验)")
    @Field("task_type")
    private Integer taskType;

    @Schema(name = "taskContent", description = "任务内容 (富文本)")
    @Field("task_content")
    private String taskContent;

    @Schema(name = "attachmentUrls", description = "任务附件URL列表")
    @Field("attachment_urls")
    private List<String> attachmentUrls;

    @Schema(name = "resourceUrls", description = "参考资料URL列表")
    @Field("resource_urls")
    private List<String> resourceUrls;

    @Schema(name = "maxScore", description = "满分")
    @Field("max_score")
    private BigDecimal maxScore;

    @Schema(name = "startTime", description = "任务开始时间")
    @Field("start_time")
    private LocalDateTime startTime;

    @Schema(name = "endTime", description = "任务结束时间")
    @Field("end_time")
    private LocalDateTime endTime;

    @Schema(name = "allowLateSubmit", description = "是否允许迟交 (0=不允许, 1=允许)")
    @Field("allow_late_submit")
    private Integer allowLateSubmit;

    @Schema(name = "maxSubmitCount", description = "最大提交次数 (0=无限制)")
    @Field("max_submit_count")
    private Integer maxSubmitCount;

    @Schema(name = "autoGrade", description = "是否自动评分 (0=手动评分, 1=自动评分)")
    @Field("auto_grade")
    private Integer autoGrade;

    @Schema(name = "tags", description = "标签列表")
    @Field("tags")
    private List<String> tags;

    @Schema(name = "difficulty", description = "难度等级 (1=简单, 2=中等, 3=困难)")
    @Field("difficulty")
    private Integer difficulty;

    @Schema(name = "estimatedTime", description = "预计完成时间 (分钟)")
    @Field("estimated_time")
    private Integer estimatedTime;

    @Schema(name = "viewCount", description = "浏览次数")
    @Field("view_count")
    private Long viewCount;

    @Schema(name = "status", description = "任务状态 (0=草稿, 1=发布, 2=进行中, 3=已结束)")
    @Field("status")
    private Integer status;
}
