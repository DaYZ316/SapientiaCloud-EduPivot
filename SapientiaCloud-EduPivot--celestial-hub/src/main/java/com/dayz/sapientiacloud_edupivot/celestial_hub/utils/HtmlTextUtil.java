package com.dayz.sapientiacloud_edupivot.celestial_hub.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * HTML 与纯文本处理工具。
 * - 去除标签并保留必要的换行
 * - 构造带标题的章节文本
 * - 简单字符级分片（带重叠）
 */
public final class HtmlTextUtil {

    private HtmlTextUtil() {
    }

    /**
     * 将HTML内容转换为较干净的纯文本，保留换行，去除标签。
     */
    public static String stripHtmlPreserveLines(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        String withNewlines = html
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</h[1-6]>", "\n");
        String noTags = withNewlines.replaceAll("<[^>]+>", "");
        String entities = noTags
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
        String normalized = entities
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
        return normalized;
    }

    /**
     * 构造章节用于向量化的文本：标题在前，正文在后。
     */
    public static String buildChapterText(String title, String plainContent) {
        StringBuilder sb = new StringBuilder();
        if (title != null && !title.isEmpty()) {
            sb.append(title).append("\n\n");
        }
        if (plainContent != null && !plainContent.isEmpty()) {
            sb.append(plainContent);
        }
        return sb.toString().trim();
    }

    /**
     * 简单的字符级分片，适合中文文本。支持重叠，避免语义被切断。
     */
    public static List<String> chunkText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null) {
            return chunks;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return chunks;
        }
        if (chunkSize <= 0) {
            chunks.add(trimmed);
            return chunks;
        }
        if (trimmed.length() <= chunkSize) {
            chunks.add(trimmed);
            return chunks;
        }
        int start = 0;
        while (start < trimmed.length()) {
            int end = Math.min(start + chunkSize, trimmed.length());
            chunks.add(trimmed.substring(start, end));
            if (end >= trimmed.length()) {
                break;
            }
            int nextStart = end - Math.max(0, overlap);
            if (nextStart <= start) {
                nextStart = end;
            }
            start = nextStart;
        }
        return chunks;
    }
}


