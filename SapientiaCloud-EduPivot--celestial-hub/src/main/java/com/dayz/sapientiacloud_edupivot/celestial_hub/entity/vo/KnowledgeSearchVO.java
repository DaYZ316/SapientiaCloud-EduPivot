package com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "知识检索结果")
public class KnowledgeSearchVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "查询内容")
    private String query;

    @Schema(description = "检索结果列表")
    private List<KnowledgeItemVO> items;

    @Schema(description = "总数")
    private Integer total;

    @Schema(description = "查询耗时(ms)")
    private Long queryTime;
}

