package com.dayz.sapientiacloud_edupivot.teacher.common.controller;

import com.dayz.sapientiacloud_edupivot.teacher.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.teacher.common.utils.PageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * web层通用数据处理
 */
public class BaseController {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDateTime.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(text, formatter);
                setValue(dateTime);
            }
        });
    }

    protected void startPage() {
        PageUtil.startPage();
    }

    protected void startPage(Integer pageNum, Integer pageSize) {
        PageUtil.startPage(pageNum, pageSize);
    }

    protected void startPage(Integer pageNum, Integer pageSize, String orderBy) {
        PageUtil.startPage(pageNum, pageSize, orderBy);
    }

    protected TableDataResult getDataTable(List<?> list) {
        return PageUtil.getDataTable(list);
    }
} 