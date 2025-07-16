package com.dayz.sapientiacloud_edupivot.system.common.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Schema(description = "通用基础实体")
public class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 6763171180183958436L;

    @Schema(name = "create_time", description = "创建时间 (系统自动生成)", accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("create_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(name = "update_time", description = "更新时间 (系统自动生成)", accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(name = "is_deleted", description = "逻辑删除标记 (内部使用)", hidden = true)
    @TableField("is_deleted")
    private Integer deleted;
}