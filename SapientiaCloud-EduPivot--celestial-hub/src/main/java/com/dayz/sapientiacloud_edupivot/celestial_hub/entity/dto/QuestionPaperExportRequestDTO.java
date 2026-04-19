package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "AI整卷导出请求")
public class QuestionPaperExportRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4914813257099403274L;

    @Schema(description = "试卷名称")
    private String paperName;

    @Valid
    @NotEmpty(message = "试卷题目不能为空")
    @Schema(description = "试卷题目列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<QuestionResponseDTO> questions;

    @Schema(description = "是否导出答案与解析")
    private Boolean includeAnswers;
}
