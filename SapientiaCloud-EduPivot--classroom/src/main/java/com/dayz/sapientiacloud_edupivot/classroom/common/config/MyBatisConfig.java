package com.dayz.sapientiacloud_edupivot.classroom.common.config;

import com.dayz.sapientiacloud_edupivot.classroom.entity.bo.LayoutConfig;
import jakarta.annotation.PostConstruct;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;


/**
 * MyBatis配置类，用于注册自定义TypeHandler
 */
@Configuration
public class MyBatisConfig {
    
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    
    @PostConstruct
    public void registerTypeHandlers() {
        // 只为特定类型注册JsonTypeHandler，避免全局应用
        sqlSessionFactory.getConfiguration().getTypeHandlerRegistry().register(LayoutConfig.class, JsonTypeHandler.class);
    }
}