package com.dayz.sapientiacloud_edupivot.course.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "填空简答视图对象")
public class QuestionAnswerVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7787978125198637181L;

    @Schema(name = "id", description = "答案ID")
    private UUID id;

    @Schema(name = "questionId", description = "题目ID")
    private UUID questionId;

    @Schema(name = "answerContent", description = "本空答案")
    private String answerContent;

    @Schema(name = "explanation", description = "本空解析")
    private String explanation;

    @Schema(name = "score", description = "分数")
    private BigDecimal score;

    @Schema(name = "sortOrder", description = "本空序号")
    private Integer sortOrder;

    @Schema(name = "createTime", description = "创建时间")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    private LocalDateTime updateTime;
}
