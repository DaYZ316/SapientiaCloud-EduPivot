package com.dayz.sapientiacloud_edupivot.minio.config;

import com.dayz.sapientiacloud_edupivot.minio.enums.BusinessBucketEnum;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * minio 配置类
 *
 * <p>新增多桶与策略配置，支持根据业务域选择不同的Bucket以及
 * 生命周期、访问控制、对象锁等治理约束。</p>
 *
 * @author LANDH
 */
@Component
@ConfigurationProperties(prefix = "minio.config")
@Data
public class MinioProperties {

    /**
     * MinIO 服务 IP
     */
    private String ip;

    /**
     * MinIO 服务端口
     */
    private int port;

    /**
     * 是否使用 HTTPS
     */
    private boolean secure = false;

    /**
     * MinIO 接入凭证
     */
    private String accessKey;

    /**
     * MinIO 接入凭证
     */
    private String secretKey;

    /**
     * 默认桶名称（向后兼容 legacy 配置）
     */
    private String bucketName;

    /**
     * 默认业务桶编码
     */
    private BusinessBucketEnum defaultBucketCode;

    /**
     * 多租户桶配置
     */
    private List<BucketPolicy> buckets = Collections.emptyList();

    public Optional<BucketPolicy> findBucket(String bucketCode) {
        if (!StringUtils.hasText(bucketCode)) {
            return Optional.empty();
        }
        return findBucket(BusinessBucketEnum.fromBucketCode(bucketCode));
    }

    public Optional<BucketPolicy> findBucket(BusinessBucketEnum bucketEnum) {
        if (bucketEnum == null || CollectionUtils.isEmpty(buckets)) {
            return Optional.empty();
        }
        return buckets.stream()
                .filter(bucket -> bucketEnum.equals(bucket.getCode()))
                .findFirst();
    }

    public Optional<BucketPolicy> resolveDefaultBucket() {
        if (defaultBucketCode != null) {
            return findBucket(defaultBucketCode);
        }
        if (!CollectionUtils.isEmpty(buckets)) {
            return Optional.of(buckets.get(0));
        }
        return Optional.empty();
    }

    /**
     * 桶访问控制
     */
    public enum BucketAccess {
        PRIVATE,
        PUBLIC_READ,
        TOKEN_ONLY
    }

    /**
     * 对象锁治理模式
     */
    public enum GovernanceMode {
        GOVERNANCE,
        COMPLIANCE
    }

    /**
     * 生命周期层级
     */
    @Getter
    @Setter
    @ToString
    public static class LifecycleRule {
        /**
         * 最近保留的对象版本数
         */
        private Integer numVersions;

        /**
         * 多少天后转标准-低频
         */
        private Integer transitionToInfrequentDays;

        /**
         * 多少天后转归档
         */
        private Integer transitionToArchiveDays;

        /**
         * 多少天后过期删除
         */
        private Integer expireAfterDays;
    }

    /**
     * 版本控制配置
     */
    @Getter
    @Setter
    @ToString
    public static class Versioning {
        private boolean enabled = false;
    }

    /**
     * 审计配置
     */
    @Getter
    @Setter
    @ToString
    public static class Audit {
        private boolean bucketLogging = false;
        private boolean objectTagging = false;
        private boolean notifyOnWrite = false;
    }

    /**
     * 桶治理策略
     */
    @Getter
    @Setter
    @ToString
    public static class BucketPolicy {
        /**
         * 业务编码，例如 USER_AVATAR
         */
        private BusinessBucketEnum code;

        /**
         * 实际桶名称
         */
        private String bucketName;

        /**
         * 描述
         */
        private String description;

        /**
         * 是否开启对象锁治理
         */
        private boolean objectLockEnabled = false;

        /**
         * 对象锁模式（仅在 objectLockEnabled=true 生效）
         */
        private GovernanceMode objectLockMode;

        /**
         * 对象锁保留天数
         */
        private Integer objectLockRetentionDays;

        /**
         * CDN 写入后是否需要刷新
         */
        private boolean refreshCdnOnWrite = false;

        /**
         * 桶访问级别
         */
        private BucketAccess access = BucketAccess.PRIVATE;

        /**
         * 版本控制
         */
        private Versioning versioning = new Versioning();

        /**
         * 生命周期
         */
        private LifecycleRule lifecycle = new LifecycleRule();

        /**
         * 审计配置
         */
        private Audit audit = new Audit();

        public String normalizedCode() {
            return code == null ? null : code.getBucketCode();
        }
    }
}