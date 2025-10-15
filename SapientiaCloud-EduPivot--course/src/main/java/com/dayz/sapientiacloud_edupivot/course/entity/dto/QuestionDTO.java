package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * 题目数据传输对象
 *
 * @author SapientiaCloud
 * @since 2024-01-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "题目数据传输对象")
public class QuestionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 6552463419472685936L;

    @Schema(name = "id", description = "题目ID")
    private UUID id;

    @Schema(name = "questionBankId", description = "所属题库ID")
    @NotNull(message = "题库ID不能为空")
    private UUID questionBankId;

    @Schema(name = "questionTitle", description = "题目标题")
    @NotBlank(message = "题目标题不能为空")
    private String questionTitle;

    @Schema(name = "questionContent", description = "题目内容")
    @NotBlank(message = "题目内容不能为空")
    private String questionContent;

    @Schema(name = "questionType", description = "题目类型 (0=单选题, 1=多选题, 2=判断题, 3=填空题, 4=简答题)")
    @NotNull(message = "题目类型不能为空")
    private Integer questionType;

    @Schema(name = "difficulty", description = "难度等级 (1=简单, 2=中等, 3=困难)")
    @NotNull(message = "难度等级不能为空")
    private Integer difficulty;

    @Schema(name = "score", description = "题目分数")
    @NotNull(message = "题目分数不能为空")
    private BigDecimal score;

    @Schema(name = "estimatedTime", description = "预计答题时间 (分钟)")
    private Integer estimatedTime;

    @Schema(name = "tags", description = "标签列表")
    private List<String> tags;

    @Schema(name = "imageUrls", description = "图片URL列表")
    private List<String> imageUrls;

    @Schema(name = "allowPartialCredit", description = "是否允许部分得分 (0=不允许, 1=允许)")
    private Integer allowPartialCredit;

    @Schema(name = "status", description = "题目状态 (0=草稿, 1=发布, 2=停用)")
    private Integer status;
}
