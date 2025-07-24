package com.dayz.sapientiacloud_edupivot.minio.utils;

import java.util.function.Function;

/**
 * 枚举工具类，用于根据枚举属性查找枚举值
 */
public class EnumLookupUtil {

    /**
     * 通过枚举的属性值获取枚举
     *
     * @param enumClass 枚举类
     * @param value     值
     * @param getter    获取方法
     * @param <E>       枚举类型
     * @param <V>       值类型
     * @return 枚举值
     */
    public static <E extends Enum<E>, V> E getByAttribute(Class<E> enumClass, V value, Function<E, V> getter) {
        if (value == null) {
            return null;
        }
        E[] enums = enumClass.getEnumConstants();
        for (E e : enums) {
            V v = getter.apply(e);
            if (value.equals(v)) {
                return e;
            }
        }
        return null;
    }
} 