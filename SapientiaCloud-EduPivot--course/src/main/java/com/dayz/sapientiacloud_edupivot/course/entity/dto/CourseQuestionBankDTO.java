package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "课程题库数据传输对象")
public class CourseQuestionBankDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -6790570181053963376L;

    @Schema(name = "id", description = "题库ID")
    private UUID id;

    @Schema(name = "courseId", description = "所属课程ID")
    @NotNull(message = "课程ID不能为空")
    private UUID courseId;

    @Schema(name = "bankName", description = "题库名称")
    @NotBlank(message = "题库名称不能为空")
    private String bankName;

    @Schema(name = "description", description = "题库描述")
    private String description;

    @Schema(name = "bankType", description = "题库类型 (0=练习题库, 1=考试题库, 2=作业题库)")
    @NotNull(message = "题库类型不能为空")
    private Integer bankType;

    @Schema(name = "tags", description = "标签列表")
    private List<String> tags;

    @Schema(name = "difficulty", description = "整体难度等级 (1=简单, 2=中等, 3=困难)")
    @NotNull(message = "难度等级不能为空")
    private Integer difficulty;

    @Schema(name = "isPublic", description = "是否公开 (0=私有, 1=公开)")
    @NotNull(message = "是否公开不能为空")
    private Integer isPublic;
}
