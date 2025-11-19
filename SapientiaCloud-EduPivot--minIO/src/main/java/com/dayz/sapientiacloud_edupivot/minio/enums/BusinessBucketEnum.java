package com.dayz.sapientiacloud_edupivot.minio.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 业务桶枚举，统一管理各类 MinIO 业务域及其默认配置
 *
 * <p>设计与 {@code ContentTypeEnum} 保持一致：提供 code/message 以及业务唯一标识，便于枚举表述和 JSON 序列化。</p>
 *
 * @author LANDH
 */
@Getter
@AllArgsConstructor
public enum BusinessBucketEnum implements BaseEnum {

    USER_AVATAR(0, "用户头像/证件照", "USER_AVATAR", "sapientiacloud-user-avatar"),
    COURSE_PUBLIC(1, "课程公开课件/论坛封面", "COURSE_PUBLIC", "sapientiacloud-course-public"),
    COURSE_PRIVATE(2, "课程私有资料/作业", "COURSE_PRIVATE", "sapientiacloud-course-private"),
    LIVE_PLAYBACK(3, "直播录制回放", "LIVE_PLAYBACK", "sapientiacloud-live-playback"),
    AI_QA_ASSET(4, "AI 问答临时资产", "AI_QA_ASSET", "sapientiacloud-ai-qa");

    /**
     * 数值编码
     */
    private final int code;

    /**
     * 展示文案
     */
    private final String message;

    /**
     * 与配置文件对应的桶编码
     */
    private final String bucketCode;

    /**
     * 推荐/默认桶名称（可被配置覆盖）
     */
    private final String defaultBucketName;

    public static BusinessBucketEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.code == code)
                .findFirst()
                .orElse(null);
    }

    public static BusinessBucketEnum fromBucketCode(String bucketCode) {
        if (bucketCode == null || bucketCode.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(item -> item.bucketCode.equalsIgnoreCase(bucketCode))
                .findFirst()
                .orElse(null);
    }

    public static boolean isValid(String bucketCode) {
        return fromBucketCode(bucketCode) != null;
    }

    public static boolean isValidCode(Integer code) {
        return fromCode(code) != null;
    }

    public String normalizedCode() {
        return bucketCode;
    }
}

