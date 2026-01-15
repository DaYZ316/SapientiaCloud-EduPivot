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
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "mg_course_question_bank")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程题库持久化对象 (PO)")
public class CourseQuestionBank extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -912109173783420527L;

    @Schema(name = "id", description = "题库ID")
    @Id
    private UUID id;

    @Schema(name = "courseId", description = "所属课程ID")
    @Field("course_id")
    private UUID courseId;

    @Schema(name = "sysUserId", description = "创建用户ID")
    @Field("sys_user_id")
    private UUID sysUserId;

    @Schema(name = "bankName", description = "题库名称")
    @Field("bank_name")
    private String bankName;

    @Schema(name = "description", description = "题库描述")
    @Field("description")
    private String description;

    @Schema(name = "bankType", description = "题库类型 (0=练习题库, 1=考试题库, 2=作业题库)")
    @Field("bank_type")
    private Integer bankType;

    @Schema(name = "tags", description = "标签列表")
    @Field("tags")
    private List<String> tags;

    @Schema(name = "difficulty", description = "整体难度等级 (1=简单, 2=中等, 3=困难)")
    @Field("difficulty")
    private Integer difficulty;
}
