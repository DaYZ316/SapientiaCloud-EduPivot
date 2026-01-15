package com.dayz.sapientiacloud_edupivot.celestial_hub.enums;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileDocumentEnum implements BaseEnum {

    FILE_NOT_EXISTS(60001, "文件不存在"),
    FILE_ID_REQUIRED(60002, "文件ID不能为空"),
    FILE_UPLOAD_FAILED(60003, "文件上传失败"),
    FILE_DOWNLOAD_FAILED(60004, "文件下载失败"),
    FILE_DELETE_FAILED(60005, "文件删除失败"),
    FILE_TYPE_NOT_SUPPORTED(60006, "不支持的文件类型"),
    FILE_SIZE_EXCEEDED(60007, "文件大小超出限制"),
    FILE_PARSE_FAILED(60008, "文件解析失败"),
    FILE_VECTORIZE_FAILED(60009, "文件向量化失败"),
    FILE_NOT_VECTORIZED(60010, "文件尚未向量化");

    private final int code;
    private final String message;
}

