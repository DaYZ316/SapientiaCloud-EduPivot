package com.dayz.sapientiacloud_edupivot.course.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "题目视图对象")
public class QuestionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4465723018589767123L;

    @Schema(name = "id", description = "题目ID")
    private UUID id;

    @Schema(name = "questionBankId", description = "所属题库ID")
    private UUID questionBankId;

    @Schema(name = "questionBankName", description = "所属题库名称")
    private String questionBankName;

    @Schema(name = "sysUserId", description = "创建用户ID")
    private UUID sysUserId;

    @Schema(name = "sysUserName", description = "创建用户名称")
    private String sysUserName;

    @Schema(name = "questionTitle", description = "题目标题")
    private String questionTitle;

    @Schema(name = "questionContent", description = "题目内容")
    private String questionContent;

    @Schema(name = "questionType", description = "题目类型 (0=单选题, 1=多选题, 2=判断题, 3=填空题, 4=简答题)")
    private Integer questionType;

    @Schema(name = "difficulty", description = "难度等级 (1=简单, 2=中等, 3=困难)")
    private Integer difficulty;

    @Schema(name = "score", description = "题目分数")
    private BigDecimal score;

    @Schema(name = "estimatedTime", description = "预计答题时间 (分钟)")
    private Integer estimatedTime;

    @Schema(name = "tags", description = "标签列表")
    private List<String> tags;

    @Schema(name = "imageUrls", description = "图片URL列表")
    private List<String> imageUrls;

    @Schema(name = "allowPartialCredit", description = "是否允许部分得分 (0=不允许, 1=允许)")
    private Integer allowPartialCredit;

    @Schema(name = "viewCount", description = "浏览次数")
    private Long viewCount;

    @Schema(name = "status", description = "题目状态 (0=草稿, 1=发布, 2=停用)")
    private Integer status;

    @Schema(name = "options", description = "选项列表")
    private List<QuestionOptionVO> options;

    @Schema(name = "answers", description = "答案列表（填空题, 简答题使用）")
    private List<QuestionAnswerVO> answers;

    @Schema(name = "createTime", description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "updateTime", description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
