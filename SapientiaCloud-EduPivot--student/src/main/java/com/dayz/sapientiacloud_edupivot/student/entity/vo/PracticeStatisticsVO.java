package com.dayz.sapientiacloud_edupivot.student.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 练习统计VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "练习统计结果")
public class PracticeStatisticsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4748733630452840598L;

    @Schema(description = "练习ID")
    private String practiceId;

    @Schema(description = "题目总数")
    private Long totalQuestions;

    @Schema(description = "正确数量")
    private Long correctCount;

    @Schema(description = "错误数量")
    private Long incorrectCount;

    @Schema(description = "半对数量")
    private Long partialCount;

    @Schema(description = "待批阅数量")
    private Long pendingReviewCount;

    @Schema(description = "题目平均分")
    private Double averageScore;
}
