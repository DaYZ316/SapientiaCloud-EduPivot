package com.dayz.sapientiacloud_edupivot.celestial_hub.enums;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum FileTypeEnum implements BaseEnum {

    PDF(0, "PDF文档", "pdf", "application/pdf", true),
    DOC(1, "Word文档", "doc", "application/msword", false),
    DOCX(2, "Word文档", "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", true),
    XLS(3, "Excel表格", "xls", "application/vnd.ms-excel", true),
    XLSX(4, "Excel表格", "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", true),
    TXT(5, "文本文件", "txt", "text/plain", true),
    MD(6, "Markdown文档", "md", "text/markdown", true),
    RTF(7, "RTF文档", "rtf", "application/rtf", true);

    private final int code;
    private final String message;
    private final String extension;
    private final String mimeType;
    private final boolean parseSupported;

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
        FileTypeEnum type = fromExtension(extension);
        return type != null && type.isParseSupported();
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
                .filter(FileTypeEnum::isParseSupported)
                .map(FileTypeEnum::getExtension)
                .toList();
    }

    public boolean isParseSupported() {
        return parseSupported;
    }
}

