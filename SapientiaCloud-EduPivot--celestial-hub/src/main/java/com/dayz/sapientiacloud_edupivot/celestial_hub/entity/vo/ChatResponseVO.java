package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileReference;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Schema(description = "AI对话响应")
public class ChatResponseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -3367253394232753305L;

    @Schema(description = "会话ID")
    private UUID sessionId;

    @Schema(description = "消息ID")
    private UUID messageId;

    @Schema(description = "AI回复内容")
    private String content;

    @Schema(description = "使用的模型")
    private String model;

    @Schema(description = "token使用量")
    private Integer tokenCount;

    @Schema(description = "引用的参考内容")
    private List<ReferenceVO> references;

    @Schema(description = "引用文件数组（用于索引文件向量数据）")
    private List<FileReference> fileReferences;

    @Schema(description = "响应时间")
    private LocalDateTime responseTime;

    @Schema(description = "是否完成")
    private Boolean finished;

    @Schema(description = "元数据")
    private Map<String, Object> metadata;

    @Schema(description = "音频状态: 0-无音频, 1-待生成, 2-生成中, 3-可播放, 4-生成失败, 5-已取消")
    private Integer audioStatus;

    @Schema(description = "音频访问地址")
    private String audioUrl;

    @Schema(description = "音频格式")
    private String audioFormat;

    @Schema(description = "音频任务ID")
    private String audioTaskId;

    @Data
    @Schema(description = "引用内容")
    public static class ReferenceVO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "内容ID")
        private UUID contentId;

        @Schema(description = "内容类型: 0-章节, 1-问题, 2-答案, 3-论坛")
        private Integer contentType;

        @Schema(description = "标题")
        private String title;

        @Schema(description = "内容片段")
        private String snippet;

        @Schema(description = "相似度分数")
        private Double similarityScore;

        @Schema(description = "来源URL")
        private String sourceUrl;
    }
}

