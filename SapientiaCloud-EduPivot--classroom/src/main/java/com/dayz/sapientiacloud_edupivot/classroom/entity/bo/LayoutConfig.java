package com.dayz.sapientiacloud_edupivot.classroom.entity.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 教室布局详细参数实体类
 */
@Data
@Schema(description = "教室布局详细参数")
public class LayoutConfig implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "左侧长度")
    private Integer leftLength;
    
    @Schema(description = "右侧长度")
    private Integer rightLength;
    
    @Schema(description = "底部长度")
    private Integer bottomLength;
    
    @Schema(description = "U型宽度")
    private Float uWidth;
    
    // 可以根据需要添加其他布局参数
}