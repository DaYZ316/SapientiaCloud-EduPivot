package com.dayz.sapientiacloud_edupivot.system.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 表格分页数据对象
 */
@Data
@Schema(description = "表格分页数据对象")
public class TableDataResult implements Serializable {

    @Serial
    private static final long serialVersionUID = -8518029618324271744L;

    /** 总记录数 */
    @Schema(description = "总记录数")
    private long total;

    /** 列表数据 */
    @Schema(description = "列表数据")
    private List<?> data;

    /** 消息状态码 */
    @Schema(description = "消息状态码")
    private int code;
    
    /** 消息内容 */
    @Schema(description = "消息内容")
    private String msg;

    /**
     * 表格数据对象
     */
    public TableDataResult() {
    }

    /**
     * 分页
     * 
     * @param list 列表数据
     * @param total 总记录数
     */
    public TableDataResult(List<?> list, int total) {
        this.data = list;
        this.total = total;
    }
    
    /**
     * 响应请求分页数据
     */
    public static TableDataResult build(List<?> list) {
        TableDataResult rspData = new TableDataResult();
        rspData.setCode(ResultEnum.SUCCESS.getCode());
        rspData.setMsg("查询成功");
        rspData.setData(list);
        rspData.setTotal(list.size());
        return rspData;
    }

    /**
     * 响应请求分页数据
     */
    public static TableDataResult build(List<?> list, long total) {
        TableDataResult rspData = new TableDataResult();
        rspData.setCode(ResultEnum.SUCCESS.getCode());
        rspData.setMsg("查询成功");
        rspData.setData(list);
        rspData.setTotal(total);
        return rspData;
    }
} 