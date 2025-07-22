package com.dayz.sapientiacloud_edupivot.auth.entity.base;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分页实体基类
 */
@Data
@Schema(description = "分页查询参数")
public class PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 6934079705802231364L;

    @Schema(name = "startTime", description = "起始时间", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(name = "endTime", description = "结束时间", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(name = "pageNum", description = "当前记录起始索引", example = "1")
    private Integer pageNum;

    @Schema(name = "pageSize", description = "每页显示记录数", example = "10")
    private Integer pageSize;

    @Schema(name = "orderByColumn", description = "排序列")
    private String orderByColumn;

    @Schema(name = "isAsc", description = "排序的方向", example = "asc", allowableValues = {"asc", "desc"})
    private String isAsc = "asc";

    @Schema(description = "分页参数合理化")
    private Boolean reasonable = true;

    public String getOrderBy() {
        if (StringUtils.isEmpty(orderByColumn)) {
            return "";
        }
        return StringUtils.toRootLowerCase(orderByColumn) + " " + isAsc;
    }
}