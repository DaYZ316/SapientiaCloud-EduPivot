package com.dayz.sapientiacloud_edupivot.minio.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件操作相关的枚举
 *
 * @author LANDH
 */
@Getter
@AllArgsConstructor
public enum FileEnum {

    // MinIO客户端相关
    MINIO_CLIENT_INIT_FAILED(2001, "MinIO客户端初始化失败"),
    MINIO_BUCKET_CREATE_FAILED(2002, "创建存储桶失败"),

    // 存储桶操作
    MINIO_GET_BUCKETS_FAILED(2010, "获取存储桶列表失败"),

    // 文件上传相关
    FILE_UPLOAD_FAILED(2101, "文件上传失败"),
    FILE_CANNOT_BE_EMPTY(2102, "上传文件不能为空"),
    FILE_SIZE_LIMIT_EXCEEDED(2103, "文件大小超出系统限制，请压缩或分片上传"),

    // 字节数组上传相关
    BYTES_UPLOAD_FAILED(2111, "字节数组上传失败"),

    // 文件下载相关
    FILE_DOWNLOAD_FAILED(2201, "文件下载失败"),
    FILE_NOT_FOUND(2202, "文件不存在"),

    // 文件URL相关
    FILE_URL_GENERATION_FAILED(2301, "获取文件URL失败"),

    // 文件删除相关
    FILE_DELETE_FAILED(2401, "删除文件失败"),
    BATCH_FILE_DELETE_FAILED(2402, "批量删除文件失败"),

    // 文件列表相关
    FILE_LIST_FAILED(2501, "列出文件失败"),

    // 文件信息相关
    FILE_INFO_FAILED(2601, "获取文件信息失败"),

    // 通用错误
    FILE_OPERATION_FAILED(2901, "文件操作失败"),
    INVALID_FILE_FORMAT(2902, "无效的文件格式"),
    INVALID_FILE_PATH(2903, "无效的文件路径"),
    FILE_BIND_FAILED(2904, "文件绑定失败");

    private final int code;

    private final String message;
} 