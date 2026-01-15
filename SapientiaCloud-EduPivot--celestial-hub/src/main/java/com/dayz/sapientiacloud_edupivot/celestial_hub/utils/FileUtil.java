package com.dayz.sapientiacloud_edupivot.celestial_hub.utils;

/**
 * 文件工具类
 *
 * @author system
 */
public class FileUtil {

    /**
     * 获取文件扩展名
     *
     * @param fileName 文件名
     * @return 文件扩展名（不包含点号），如果文件名为空或不包含点号则返回空字符串
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        return fileName.substring(lastDotIndex + 1);
    }
}

