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
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "mg_question_option")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "题目选项持久化对象 (PO)")
public class QuestionOption extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -5035821604282617050L;

    @Schema(name = "id", description = "选项ID")
    @Id
    private UUID id;

    @Schema(name = "questionId", description = "所属题目ID")
    @Field("question_id")
    private UUID questionId;

    @Schema(name = "optionContent", description = "选项内容")
    @Field("option_content")
    private String optionContent;

    @Schema(name = "optionLabel", description = "选项标签 (A, B, C, D等)")
    @Field("option_label")
    private String optionLabel;

    @Schema(name = "isCorrect", description = "是否为正确答案 (0=错误, 1=正确)")
    @Field("is_correct")
    private Integer isCorrect;

    @Schema(name = "score", description = "选项分数 (多选题部分得分使用)")
    @Field("score")
    private BigDecimal score;

    @Schema(name = "imageUrls", description = "图片URL列表")
    @Field("image_urls")
    private List<String> imageUrls;

    @Schema(name = "explanation", description = "选项解析")
    @Field("explanation")
    private String explanation;
}
