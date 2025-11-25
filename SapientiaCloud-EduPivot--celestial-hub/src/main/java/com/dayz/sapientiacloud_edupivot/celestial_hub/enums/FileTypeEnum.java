package com.dayz.sapientiacloud_edupivot.celestial_hub.enums;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum FileTypeEnum implements BaseEnum {

    PDF(0, "PDF文档", "pdf", "application/pdf"),
    DOC(1, "Word文档", "doc", "application/msword"),
    DOCX(2, "Word文档", "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    XLS(3, "Excel表格", "xls", "application/vnd.ms-excel"),
    XLSX(4, "Excel表格", "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    TXT(5, "文本文件", "txt", "text/plain"),
    MD(6, "Markdown文档", "md", "text/markdown"),
    RTF(7, "RTF文档", "rtf", "application/rtf");

    private final int code;
    private final String message;
    private final String extension;
    private final String mimeType;

    public static FileTypeEnum fromExtension(String extension) {
        if (extension == null) {
            return null;
        }
        String ext = extension.toLowerCase().startsWith(".") ? extension.substring(1) : extension.toLowerCase();
        for (FileTypeEnum type : values()) {
            if (type.extension.equals(ext)) {
                return type;
            }
        }
        return null;
    }

    public static FileTypeEnum fromMimeType(String mimeType) {
        if (mimeType == null) {
            return null;
        }
        for (FileTypeEnum type : values()) {
            if (type.mimeType.equals(mimeType)) {
                return type;
            }
        }
        return null;
    }

    public static boolean isValidExtension(String extension) {
        return fromExtension(extension) != null;
    }

    public static FileTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (FileTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }

    public static List<String> getAllowedExtensions() {
        return Arrays.stream(values())
                .map(FileTypeEnum::getExtension)
                .toList();
    }
}

