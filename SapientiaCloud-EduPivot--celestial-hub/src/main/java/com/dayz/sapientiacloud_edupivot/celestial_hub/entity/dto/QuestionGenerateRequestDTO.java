package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * AI question generation request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "QuestionGenerateRequestDTO",
        description = "AI出题请求参数，支持课程/章节/题库/文件/RAG/知识点与自动入题库等上下文能力"
)
public class QuestionGenerateRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2153417259123456789L;

    @Schema(description = "会话ID。新建出题会话时可为空", example = "11111111-1111-1111-1111-111111111111")
    private UUID sessionId;

    @Schema(description = "请求ID。前端可传入以便刷新后恢复同一任务", example = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
    private String requestId;

    @Schema(
            description = "鐢熸垚妯″紡锛歵uestion=鍑洪锛宲aper=鍑洪+鏁村嵎",
            allowableValues = {"question", "paper"},
            example = "question"
    )
    private String generationMode;

    @Schema(description = "前端当前语种，例如 zh-CN 或 en-US", example = "zh-CN")
    private String locale;

    @Schema(description = "课程ID。需要结合课程章节、题库或生成会话时建议传入", example = "22222222-2222-2222-2222-222222222222")
    private UUID courseId;

    @Schema(description = "题库ID。用于参考已有题库风格，或将生成结果写回指定题库", example = "33333333-3333-3333-3333-333333333333")
    private UUID questionBankId;

    @Schema(description = "章节ID列表。用于限定出题范围", example = "[\"44444444-4444-4444-4444-444444444444\",\"55555555-5555-5555-5555-555555555555\"]")
    private List<UUID> chapterIds;

    @Schema(description = "题目数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "8")
    @NotNull(message = "Question count is required")
    @Min(value = 1, message = "Question count must be at least 1")
    @Max(value = 50, message = "Question count must be at most 50")
    private Integer questionCount;

    @Schema(
            description = "题型：0单选，1多选，2判断，3填空，4简答，5混合",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"0", "1", "2", "3", "4", "5"},
            example = "5"
    )
    @NotNull(message = "Question type is required")
    private Integer questionType;

    @Schema(
            description = "难度：0随机，1简单，2中等，3困难",
            requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"0", "1", "2", "3"},
            example = "2"
    )
    @NotNull(message = "Difficulty is required")
    private Integer difficulty;

    @Schema(description = "每题分值。若传入则作为默认分值写入各题", example = "5")
    private BigDecimal scorePerQuestion;

    @Schema(description = "试卷总分。用于校验整套试卷分值是否匹配", example = "40")
    private BigDecimal totalScore;

    @Schema(description = "整套试卷预计作答总时长，单位分钟", example = "60")
    private Integer totalEstimatedTime;

    @Schema(description = "试卷名称", example = "Java基础阶段测验")
    private String paperName;

    @Schema(description = "试卷类型，如阶段测试、章节练习、期末模拟", example = "阶段测试")
    private String paperType;

    @Schema(description = "出题补充要求", example = "覆盖集合、异常、泛型与IO基础，题目要避免与题库样题重复，兼顾基础和应用。")
    @Size(max = 1000, message = "Requirement length must be at most 1000")
    private String requirement;

    @Schema(description = "是否启用RAG检索增强", example = "true")
    private Boolean useRag;

    @Valid
    @Schema(description = "引用文件列表。用于把上传文档内容纳入出题上下文")
    private List<FileReference> fileReferences;

    @Schema(description = "显式参考题目ID列表。可用于风格对齐或避免重复", example = "[\"77777777-7777-7777-7777-777777777777\"]")
    private List<UUID> referenceQuestionIds;

    @Schema(description = "知识点列表。会作为出题目标与标签输入", example = "[\"Collection\",\"Generic\",\"Exception\",\"IO\"]")
    private List<String> knowledgePoints;

    @Schema(description = "能力目标列表。用于约束题目覆盖的能力维度", example = "[\"知识理解\",\"代码分析\",\"综合应用\"]")
    private List<String> abilityGoals;

    @Schema(description = "是否将生成结果自动保存到题库。启用时需同时提供 courseId 和 questionBankId", example = "true")
    private Boolean saveToQuestionBank;

    @Schema(
            description = "保存到题库后的状态：0草稿，1已发布，2已停用",
            allowableValues = {"0", "1", "2"},
            example = "0"
    )
    private Integer saveStatus;
}
