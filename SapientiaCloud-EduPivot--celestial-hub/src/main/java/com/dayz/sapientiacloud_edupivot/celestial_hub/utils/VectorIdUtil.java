package com.dayz.sapientiacloud_edupivot.celestial_hub.utils;

import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.KnowledgeConstants;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 向量ID工具，确保 Redis Key / 元数据 / Mongo 之间的 ID 一致
 */
public final class VectorIdUtil {

    private VectorIdUtil() {
    }

    /**
     * 确保 metadata 中包含可用的 vectorId，并返回该值
     */
    public static String ensureVectorId(Map<String, Object> metadata) {
        return ensureVectorId(metadata, null);
    }

    /**
     * 确保 metadata 中包含可用的 vectorId，并与 Redis 前缀保持一致
     */
    public static String ensureVectorId(Map<String, Object> metadata, String redisPrefix) {
        if (metadata == null) {
            throw new IllegalArgumentException("metadata must not be null when generating vectorId");
        }
        Object value = metadata.get(KnowledgeConstants.METADATA_VECTOR_ID);
        String vectorId = value != null ? value.toString() : null;
        vectorId = normalizeVectorId(vectorId, redisPrefix);
        if (!StringUtils.hasText(vectorId)) {
            vectorId = UuidCreator.getTimeOrderedEpoch().toString();
        }
        metadata.put(KnowledgeConstants.METADATA_VECTOR_ID, vectorId);
        return vectorId;
    }

    private static String normalizeVectorId(String vectorId, String redisPrefix) {
        if (!StringUtils.hasText(vectorId)) {
            return null;
        }
        if (StringUtils.hasText(redisPrefix)) {
            String prefixWithColon = redisPrefix + ":";
            if (vectorId.startsWith(prefixWithColon)) {
                return normalizeVectorId(vectorId.substring(prefixWithColon.length()), redisPrefix);
            }
            if (vectorId.startsWith(redisPrefix)) {
                return normalizeVectorId(vectorId.substring(redisPrefix.length()), redisPrefix);
            }
        }
        return vectorId;
    }
}

