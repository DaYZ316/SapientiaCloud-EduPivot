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
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "mg_question_answer")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "题目答案持久化对象 (PO)")
public class QuestionAnswer extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 6707320798020409407L;

    @Schema(name = "id", description = "答案ID")
    @Id
    private UUID id;

    @Schema(name = "questionId", description = "题目ID")
    @Field("question_id")
    private UUID questionId;

    @Schema(name = "answerContent", description = "本空答案")
    @Field("answer_content")
    private String answerContent;

    @Schema(name = "explanation", description = "本空解析")
    @Field("explanation")
    private String explanation;

    @Schema(name = "score", description = "分数")
    @Field("score")
    private BigDecimal score;

    @Schema(name = "sortOrder", description = "本空序号")
    @Field("sort_order")
    private Integer sortOrder;

}
