package com.dayz.sapientiacloud_edupivot.auth.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 通用枚举查找工具类
 */
public class EnumUtil {

    // 使用ConcurrentHashMap来存储不同枚举类型的映射，确保线程安全
    private static final Map<Class<? extends Enum<?>>, Map<String, ? extends Enum<?>>> ENUM_CACHE = new ConcurrentHashMap<>();

    /**
     * 根据指定属性（例如message）查找枚举
     *
     * @param enumClass       枚举的Class对象
     * @param attributeValue  要查找的属性值
     * @param attributeGetter 获取枚举属性值的函数式接口
     * @param <E>             枚举类型
     * @return 匹配的枚举，如果未找到则返回null
     */
    @SuppressWarnings("unchecked")
    public static <E extends Enum<E>> E getByAttribute(
            Class<E> enumClass,
            String attributeValue,
            Function<E, String> attributeGetter) {

        // 尝试从缓存中获取当前枚举类型的映射
        Map<String, E> attributeMap = (Map<String, E>) ENUM_CACHE.get(enumClass);

        // 如果缓存中没有，则构建并放入缓存
        if (attributeMap == null) {
            attributeMap = new HashMap<>();
            for (E enumValue : enumClass.getEnumConstants()) {
                attributeMap.put(attributeGetter.apply(enumValue), enumValue);
            }
            // 使用putIfAbsent确保线程安全地更新缓存
            ENUM_CACHE.putIfAbsent(enumClass, attributeMap);
            // 重新获取，因为可能有其他线程已经放入了
            attributeMap = (Map<String, E>) ENUM_CACHE.get(enumClass);
        }

        return attributeMap.get(attributeValue);
    }
}