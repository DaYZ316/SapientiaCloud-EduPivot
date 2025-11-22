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
@Document(collection = "mg_question")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "题目持久化对象 (PO)")
public class Question extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 7737370774718766534L;

    @Schema(name = "id", description = "题目ID")
    @Id
    private UUID id;

    @Schema(name = "questionBankId", description = "所属题库ID")
    @Field("question_bank_id")
    private UUID questionBankId;

    @Schema(name = "courseId", description = "所属课程ID")
    @Field("course_id")
    private UUID courseId;

    @Schema(name = "sysUserId", description = "创建用户ID")
    @Field("sys_user_id")
    private UUID sysUserId;

    @Schema(name = "questionTitle", description = "题目标题")
    @Field("question_title")
    private String questionTitle;

    @Schema(name = "questionContent", description = "题目内容")
    @Field("question_content")
    private String questionContent;

    @Schema(name = "questionType", description = "题目类型 (0=单选题, 1=多选题, 2=判断题, 3=填空题, 4=简答题)")
    @Field("question_type")
    private Integer questionType;

    @Schema(name = "difficulty", description = "难度等级 (1=简单, 2=中等, 3=困难)")
    @Field("difficulty")
    private Integer difficulty;

    @Schema(name = "score", description = "题目分数")
    @Field("score")
    private BigDecimal score;

    @Schema(name = "estimatedTime", description = "预计答题时间 (分钟)")
    @Field("estimated_time")
    private Integer estimatedTime;

    @Schema(name = "tags", description = "标签列表")
    @Field("tags")
    private List<String> tags;

    @Schema(name = "imageUrls", description = "图片URL列表")
    @Field("image_urls")
    private List<String> imageUrls;

    @Schema(name = "allowPartialCredit", description = "是否允许部分得分 (0=不允许, 1=允许)")
    @Field("allow_partial_credit")
    private Integer allowPartialCredit;

    @Schema(name = "viewCount", description = "浏览次数")
    @Field("view_count")
    private Long viewCount;

    @Schema(name = "status", description = "题目状态 (0=草稿, 1=发布, 2=停用)")
    @Field("status")
    private Integer status;

}
