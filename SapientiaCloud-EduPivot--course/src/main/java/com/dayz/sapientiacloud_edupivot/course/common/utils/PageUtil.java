package com.dayz.sapientiacloud_edupivot.course.common.utils;

import com.dayz.sapientiacloud_edupivot.course.common.entity.base.PageEntity;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 分页工具类
 */
public class PageUtil {
    /**
     * 设置请求分页数据
     */
    public static void startPage() {
        PageEntity pageEntity = TableUtil.buildPageRequest();
        Integer pageNum = pageEntity.getPageNum();
        Integer pageSize = pageEntity.getPageSize();
        String orderBy = StringUtils.isNotEmpty(pageEntity.getOrderBy()) ? pageEntity.getOrderBy() : "";
        Boolean reasonable = pageEntity.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
    }

    /**
     * 设置请求分页数据
     *
     * @param pageNum  页码
     * @param pageSize 每页记录数
     */
    public static void startPage(Integer pageNum, Integer pageSize) {
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
    }

    /**
     * 设置请求分页数据
     *
     * @param pageNum  页码
     * @param pageSize 每页记录数
     * @param orderBy  排序
     */
    public static void startPage(Integer pageNum, Integer pageSize, String orderBy) {
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize, orderBy);
        }
    }

    /**
     * 响应请求分页数据
     *
     * @param list 列表数据
     * @return 分页数据
     */
    public static TableDataResult getDataTable(List<?> list) {
        TableDataResult rspData = new TableDataResult();
        rspData.setCode(200);
        rspData.setMessage("查询成功");
        rspData.setData(list);
        rspData.setTotal(new PageInfo<>(list).getTotal());
        return rspData;
    }
} 