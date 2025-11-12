package com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 题目答案视图对象
 *
 * @author SapientiaCloud
 * @since 2024-01-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "题目答案视图对象")
public class QuestionAnswerVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7787978125198637181L;

    @Schema(name = "id", description = "答案ID")
    private UUID id;

    @Schema(name = "questionId", description = "题目ID")
    private UUID questionId;

    @Schema(name = "questionTitle", description = "题目标题")
    private String questionTitle;

    @Schema(name = "sysUserId", description = "创建用户ID")
    private UUID sysUserId;

    @Schema(name = "sysUserName", description = "创建用户名称")
    private String sysUserName;

    @Schema(name = "answerContent", description = "答案内容")
    private String answerContent;

    @Schema(name = "answerText", description = "文本答案 (填空题、简答题)")
    private String answerText;

    @Schema(name = "isCorrect", description = "是否正确 (0=错误, 1=正确, 2=部分正确)")
    private Integer isCorrect;

    @Schema(name = "isCorrectName", description = "是否正确名称")
    private String isCorrectName;

    @Schema(name = "score", description = "得分")
    private BigDecimal score;

    @Schema(name = "createTime", description = "创建时间")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    private LocalDateTime updateTime;
}
