package com.dayz.sapientiacloud_edupivot.system.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "表格分页数据对象")
public class TableDataResult implements Serializable {

    @Serial
    private static final long serialVersionUID = -8518029618324271744L;

    @Schema(name = "total", description = "总记录数")
    private long total;

    @Schema(name = "data", description = "列表数据")
    private List<?> data;

    @Schema(name = "code", description = "消息状态码")
    private int code;

    @Schema(name = "message", description = "消息内容")
    private String message;

    public TableDataResult() {
    }

    public TableDataResult(List<?> list, int total) {
        this.data = list;
        this.total = total;
    }

    public static TableDataResult build(List<?> list) {
        TableDataResult rspData = new TableDataResult();
        rspData.setCode(ResultEnum.SUCCESS.getCode());
        rspData.setMessage("查询成功");
        rspData.setData(list);
        rspData.setTotal(list.size());
        return rspData;
    }

    public static TableDataResult build(List<?> list, long total) {
        TableDataResult rspData = new TableDataResult();
        rspData.setCode(ResultEnum.SUCCESS.getCode());
        rspData.setMessage("查询成功");
        rspData.setData(list);
        rspData.setTotal(total);
        return rspData;
    }
} 