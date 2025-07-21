package com.dayz.sapientiacloud_edupivot.system.common.utils;

import com.dayz.sapientiacloud_edupivot.system.common.entity.PageEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 表格数据处理
 */
public class TableUtil {

    public static final String PAGE_NUM = "pageNum";
    public static final String PAGE_SIZE = "pageSize";
    public static final String ORDER_BY_COLUMN = "orderByColumn";
    public static final String IS_ASC = "isAsc";
    public static final String REASONABLE = "reasonable";

    public static PageEntity getPageEntity() {
        PageEntity pageDomain = new PageEntity();
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        pageDomain.setPageNum(getParameterToInt(request, PAGE_NUM));
        pageDomain.setPageSize(getParameterToInt(request, PAGE_SIZE));
        pageDomain.setOrderByColumn(request.getParameter(ORDER_BY_COLUMN));
        pageDomain.setIsAsc(request.getParameter(IS_ASC));
        pageDomain.setReasonable(getParameterToBool(request));
        return pageDomain;
    }

    public static PageEntity buildPageRequest() {
        return getPageEntity();
    }

    private static Integer getParameterToInt(HttpServletRequest request, String paramName) {
        String paramValue = request.getParameter(paramName);
        if (StringUtils.isNumeric(paramValue)) {
            return Integer.parseInt(paramValue);
        }
        return null;
    }

    private static Boolean getParameterToBool(HttpServletRequest request) {
        String paramValue = request.getParameter(TableUtil.REASONABLE);
        if (StringUtils.isNotBlank(paramValue)) {
            return Boolean.valueOf(paramValue);
        }
        return null;
    }
} 