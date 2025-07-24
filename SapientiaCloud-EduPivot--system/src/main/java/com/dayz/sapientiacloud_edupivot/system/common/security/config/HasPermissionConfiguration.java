package com.dayz.sapientiacloud_edupivot.system.common.security.config;

import com.dayz.sapientiacloud_edupivot.system.common.security.annotation.HasPermission;
import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.filters.OpenApiMethodFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

/**
 * HasPermission功能配置类
 * 集中配置所有HasPermission相关组件
 */
@Configuration
public class HasPermissionConfiguration {
    
    /**
     * 创建HasPermission操作自定义处理器
     * 用于处理HasPermission注解的Operation元数据
     */
    @Bean
    public OperationCustomizer hasPermissionOperationCustomizer() {
        return new HasPermissionOperationCustomizer();
    }
    
    /**
     * 创建HasPermission隐藏操作过滤器
     * 用于处理@HasPermission注解中的hidden属性
     */
    @Bean
    public OpenApiMethodFilter hasPermissionHiddenOperationFilter() {
        return new HasPermissionHiddenOperationFilter();
    }
    
    /**
     * HasPermission注解的Operation自定义处理器
     * 用于处理HasPermission注解的Operation元数据
     */
    public static class HasPermissionOperationCustomizer implements OperationCustomizer {

        @Override
        public Operation customize(Operation operation, HandlerMethod handlerMethod) {
            HasPermission hasPermission = handlerMethod.getMethodAnnotation(HasPermission.class);
            if (hasPermission == null) {
                return operation;
            }

            // 设置接口摘要
            if (!hasPermission.summary().isEmpty()) {
                operation.setSummary(hasPermission.summary());
            }
            
            // 设置接口描述
            if (!hasPermission.description().isEmpty()) {
                operation.setDescription(hasPermission.description());
            }
            
            return operation;
        }
    }
    
    /**
     * HasPermission注解的隐藏操作过滤器
     * 用于处理@HasPermission注解中的hidden属性
     */
    public static class HasPermissionHiddenOperationFilter implements OpenApiMethodFilter {

        @Override
        public boolean isMethodToInclude(Method method) {
            HasPermission hasPermission = method.getAnnotation(HasPermission.class);
            // 返回true表示包含该方法，false表示排除该方法
            // 如果方法有@HasPermission注解，且hidden=true，则排除该方法（不在API文档中显示）
            return hasPermission == null || !hasPermission.hidden();
        }
    }
} 