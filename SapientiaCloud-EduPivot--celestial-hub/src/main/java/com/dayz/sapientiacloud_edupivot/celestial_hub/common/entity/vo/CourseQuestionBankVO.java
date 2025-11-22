package com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 课程题库视图对象
 *
 * @author SapientiaCloud
 * @since 2024-01-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "课程题库视图对象")
public class CourseQuestionBankVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7754971407076633131L;

    @Schema(name = "id", description = "题库ID")
    private UUID id;

    @Schema(name = "courseId", description = "所属课程ID")
    private UUID courseId;

    @Schema(name = "sysUserId", description = "创建用户ID")
    private UUID sysUserId;

    @Schema(name = "sysUserName", description = "创建用户名称")
    private String sysUserName;

    @Schema(name = "sysUserAvatar", description = "创建用户头像")
    private String sysUserAvatar;

    @Schema(name = "bankName", description = "题库名称")
    private String bankName;

    @Schema(name = "description", description = "题库描述")
    private String description;

    @Schema(name = "bankType", description = "题库类型 (0=练习题库, 1=考试题库, 2=作业题库)")
    private Integer bankType;

    @Schema(name = "tags", description = "标签列表")
    private List<String> tags;

    @Schema(name = "difficulty", description = "整体难度等级 (1=简单, 2=中等, 3=困难)")
    private Integer difficulty;

    @Schema(name = "questionCount", description = "题目数量")
    private Long questionCount;

    @Schema(name = "createTime", description = "创建时间")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    private LocalDateTime updateTime;
}
