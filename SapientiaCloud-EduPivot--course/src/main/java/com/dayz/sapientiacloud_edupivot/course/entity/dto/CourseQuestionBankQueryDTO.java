package com.dayz.sapientiacloud_edupivot.course.entity.dto;

import com.dayz.sapientiacloud_edupivot.course.common.entity.base.PageEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "课程题库查询数据传输对象")
public class CourseQuestionBankQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 5717359642269403400L;

    @Schema(name = "courseId", description = "课程ID")
    private UUID courseId;

    @Schema(name = "bankName", description = "题库名称")
    private String bankName;

    @Schema(name = "bankType", description = "题库类型")
    private Integer bankType;

    @Schema(name = "difficulty", description = "难度等级")
    private Integer difficulty;

    @Schema(name = "tags", description = "标签列表")
    private List<String> tags;

    @Schema(name = "createTimeStart", description = "创建时间开始")
    private String createTimeStart;

    @Schema(name = "createTimeEnd", description = "创建时间结束")
    private String createTimeEnd;
}
