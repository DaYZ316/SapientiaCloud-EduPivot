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
@Schema(description = "题目查询数据传输对象")
public class QuestionQueryDTO extends PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = -7278858577824198465L;

    @Schema(name = "questionBankId", description = "题库ID")
    private UUID questionBankId;

    @Schema(name = "sysUserId", description = "创建用户ID")
    private UUID sysUserId;

    @Schema(name = "questionTitle", description = "题目标题")
    private String questionTitle;

    @Schema(name = "questionType", description = "题目类型")
    private Integer questionType;

    @Schema(name = "difficulty", description = "难度等级")
    private Integer difficulty;

    @Schema(name = "status", description = "题目状态")
    private Integer status;

    @Schema(name = "tags", description = "标签列表")
    private List<String> tags;

    @Schema(name = "createTimeStart", description = "创建时间开始")
    private String createTimeStart;

    @Schema(name = "createTimeEnd", description = "创建时间结束")
    private String createTimeEnd;
}
