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

    @Schema(name = "sysUserId", description = "创建用户ID")
    @Field("sys_user_id")
    private UUID sysUserId;

    @Schema(name = "answerContent", description = "答案内容")
    @Field("answer_content")
    private String answerContent;

    @Schema(name = "answerText", description = "文本答案 (填空题、简答题)")
    @Field("answer_text")
    private String answerText;

    @Schema(name = "isCorrect", description = "是否正确 (0=错误, 1=正确, 2=部分正确)")
    @Field("is_correct")
    private Integer isCorrect;

    @Schema(name = "score", description = "得分")
    @Field("score")
    private BigDecimal score;

}
