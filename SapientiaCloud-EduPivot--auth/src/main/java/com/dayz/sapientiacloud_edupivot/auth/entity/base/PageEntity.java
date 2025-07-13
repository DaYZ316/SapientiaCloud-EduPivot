package com.dayz.sapientiacloud_edupivot.auth.entity.base;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "通用分页请求参数")
public class PageEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 6934079705802231364L;

    @Schema(description = "当前页码", example = "1", defaultValue = "1")
    @Min(value = 1, message = "页码必须从1开始")
    private int pageNum = 1;

    @Schema(description = "每页记录数", example = "10", defaultValue = "10")
    @Min(value = 1, message = "每页记录数必须大于0")
    private int pageSize = 10;

    @Schema(description = "排序字段", example = "create_time")
    private String orderByColumn;

    @Schema(description = "排序方向", example = "desc", allowableValues = {"asc", "desc"})
    private String orderDirection = "desc";

    public String buildOrderBy() {
        if (orderByColumn != null && !orderByColumn.isEmpty()) {
            // 简单的SQL注入防御：只允许字母、数字、下划线和逗号
            String safeOrderByColumn = orderByColumn.replaceAll("[^a-zA-Z0-9_,]", "");
            String safeOrderDirection = "desc".equalsIgnoreCase(orderDirection) ? "desc" : "asc";
            return safeOrderByColumn + " " + safeOrderDirection;
        }
        return null;
    }
}