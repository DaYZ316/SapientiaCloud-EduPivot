package com.dayz.sapientiacloud_edupivot.minio.utils;

import com.dayz.sapientiacloud_edupivot.minio.config.MinioProperties;
import com.dayz.sapientiacloud_edupivot.minio.entity.FileInfo;
import com.dayz.sapientiacloud_edupivot.minio.enums.FileEnum;
import com.dayz.sapientiacloud_edupivot.minio.exception.BusinessException;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * MinIO工具类，提供文件上传、下载、删除等操作，并对多业务桶策略进行治理
 *
 * @author LANDH
 */
@Slf4j
@Component
public class MinIOUtil {

    private static final String LEGACY_BUCKET_CODE = "LEGACY_DEFAULT";

    @Resource
    private MinioProperties minioProperties;

    private MinioClient minioClient;

    @PostConstruct
    private void init() {
        createMinioClient();
        initializeBuckets();
    }

    /**
     * 创建MinioClient客户端
     */
    private void createMinioClient() {
        try {
            String protocol = minioProperties.isSecure() ? "https" : "http";
            String endpoint = protocol + "://" + minioProperties.getIp() + ":" + minioProperties.getPort();
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();
        } catch (Exception e) {
            throw new BusinessException(FileEnum.MINIO_CLIENT_INIT_FAILED.getMessage() + ": " + e.getMessage());
        }
    }

    /**
     * 初始化所需的业务桶
     */
    private void initializeBuckets() {
        if (CollectionUtils.isEmpty(minioProperties.getBuckets())) {
            String bucketName = minioProperties.getBucketName();
            if (StringUtils.hasText(bucketName)) {
                createBucketIfNotExists(bucketName, false);
            }
            return;
        }
        minioProperties.getBuckets().stream()
                .filter(bucket -> StringUtils.hasText(bucket.getBucketName()))
                .forEach(this::ensureBucketProvisioned);
    }

    private void ensureBucketProvisioned(MinioProperties.BucketPolicy bucketPolicy) {
        String bucketName = bucketPolicy.getBucketName();
        createBucketIfNotExists(bucketName, bucketPolicy.isObjectLockEnabled());
        configureBucketVersioning(bucketPolicy);
        configureBucketPolicy(bucketPolicy);
    }

    private void configureBucketVersioning(MinioProperties.BucketPolicy bucketPolicy) {
        if (bucketPolicy.getVersioning() == null || !bucketPolicy.getVersioning().isEnabled()) {
            return;
        }
        try {
            minioClient.setBucketVersioning(
                    SetBucketVersioningArgs.builder()
                            .bucket(bucketPolicy.getBucketName())
                            .config(new VersioningConfiguration(VersioningConfiguration.Status.ENABLED, false))
                            .build()
            );
        } catch (Exception e) {
            log.warn("启用桶 [{}] 版本控制失败: {}", bucketPolicy.getBucketName(), e.getMessage());
        }
    }

    private void configureBucketPolicy(MinioProperties.BucketPolicy bucketPolicy) {
        if (bucketPolicy.getAccess() != MinioProperties.BucketAccess.PUBLIC_READ) {
            return;
        }
        try {
            String policyJson = buildPublicReadPolicy(bucketPolicy.getBucketName());
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketPolicy.getBucketName())
                            .config(policyJson)
                            .build()
            );
        } catch (Exception e) {
            log.warn("设置桶 [{}] 公共读策略失败: {}", bucketPolicy.getBucketName(), e.getMessage());
        }
    }

    private String buildPublicReadPolicy(String bucketName) {
        return """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(bucketName);
    }

    /**
     * 如果存储桶不存在，则创建
     */
    private void createBucketIfNotExists(String bucketName, boolean objectLockEnabled) {
        if (!StringUtils.hasText(bucketName)) {
            return;
        }
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!bucketExists) {
                MakeBucketArgs.Builder builder = MakeBucketArgs.builder()
                        .bucket(bucketName);
                if (objectLockEnabled) {
                    builder.objectLock(true);
                }
                minioClient.makeBucket(builder.build());
            }
        } catch (Exception e) {
            throw new BusinessException(FileEnum.MINIO_BUCKET_CREATE_FAILED.getMessage() + ": " + e.getMessage());
        }
    }

    /**
     * 获取所有存储桶
     *
     * @return 存储桶列表
     */
    public List<Bucket> getAllBuckets() {
        try {
            return minioClient.listBuckets();
        } catch (Exception e) {
            throw new BusinessException(FileEnum.MINIO_GET_BUCKETS_FAILED.getMessage() + ": " + e.getMessage());
        }
    }

    /**
     * 上传文件
     *
     * @param file        文件
     * @param bucketCode  业务桶编码
     * @param objectName  对象名，为空时使用文件原名
     * @param contentType 内容类型，为空时自动检测
     * @return 对象名称
     */
    public String uploadFile(MultipartFile file, String bucketCode, String objectName, String contentType) {
        try {
            if (file == null || file.isEmpty()) {
                throw new BusinessException(FileEnum.FILE_CANNOT_BE_EMPTY.getMessage());
            }
            BucketContext bucketContext = resolveBucketContext(bucketCode);

            String fileName = objectName;
            if (!StringUtils.hasText(fileName)) {
                fileName = generateUniqueFileName(Objects.requireNonNull(file.getOriginalFilename()));
            }

            String fileContentType = contentType;
            if (!StringUtils.hasText(fileContentType)) {
                fileContentType = file.getContentType();
            }

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketContext.bucketName())
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(fileContentType)
                    .build());

            return fileName;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(FileEnum.FILE_UPLOAD_FAILED.getMessage() + ": " + e.getMessage());
        }
    }

    /**
     * 上传文件(简化版)
     *
     * @param file       文件
     * @param bucketCode 业务桶编码
     * @return 对象名称
     */
    public String uploadFile(MultipartFile file, String bucketCode) {
        return uploadFile(file, bucketCode, null, null);
    }

    public String uploadFile(MultipartFile file) {
        return uploadFile(file, null, null, null);
    }

    /**
     * 上传字节数组
     */
    public String uploadBytes(byte[] bytes, String bucketCode, String objectName, String contentType) {
        try {
            BucketContext bucketContext = resolveBucketContext(bucketCode);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketContext.bucketName())
                    .object(objectName)
                    .stream(bais, bytes.length, -1)
                    .contentType(contentType)
                    .build());
            return objectName;
        } catch (Exception e) {
            throw new BusinessException(FileEnum.BYTES_UPLOAD_FAILED.getMessage() + ": " + e.getMessage());
        }
    }

    /**
     * 下载文件
     */
    public InputStream downloadFile(String objectName, String bucketCode) {
        try {
            BucketContext bucketContext = resolveBucketContext(bucketCode);
            if (!doesObjectExist(objectName, bucketContext)) {
                throw new BusinessException(FileEnum.FILE_NOT_FOUND.getMessage());
            }

            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketContext.bucketName())
                    .object(objectName)
                    .build());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(FileEnum.FILE_DOWNLOAD_FAILED.getMessage() + ": " + e.getMessage());
        }
    }

    /**
     * 获取文件外链
     */
    public String getPresignedObjectUrl(String objectName, Integer expiry, String bucketCode) {
        try {
            BucketContext bucketContext = resolveBucketContext(bucketCode);
            if (!doesObjectExist(objectName, bucketContext)) {
                throw new BusinessException(FileEnum.FILE_NOT_FOUND.getMessage());
            }

            int expiryTime = expiry != null ? expiry : 7 * 24 * 3600;
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketContext.bucketName())
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(expiryTime, TimeUnit.SECONDS)
                    .build());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(FileEnum.FILE_URL_GENERATION_FAILED.getMessage() + ": " + e.getMessage());
        }
    }

    public String getPresignedObjectUrl(String objectName) {
        return getPresignedObjectUrl(objectName, null, null);
    }

    /**
     * 删除文件
     */
    public boolean removeObject(String objectName, String bucketCode) {
        try {
            BucketContext bucketContext = resolveBucketContext(bucketCode);
            if (!doesObjectExist(objectName, bucketContext)) {
                return true;
            }

            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketContext.bucketName())
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            throw new BusinessException(FileEnum.FILE_DELETE_FAILED.getMessage() + ": " + e.getMessage());
        }
    }

    /**
     * 批量删除文件
     */
    public Map<String, String> removeObjects(List<String> objectNames, String bucketCode) {
        Map<String, String> result = new HashMap<>(objectNames.size());
        List<DeleteObject> objects = new ArrayList<>(objectNames.size());
        BucketContext bucketContext = resolveBucketContext(bucketCode);

        for (String objectName : objectNames) {
            objects.add(new DeleteObject(objectName));
            result.put(objectName, "成功");
        }

        try {
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(bucketContext.bucketName())
                    .objects(objects)
                    .build());

            for (Result<DeleteError> r : results) {
                DeleteError error = r.get();
                result.put(error.objectName(), "失败: " + error.message());
            }
        } catch (Exception e) {
            throw new BusinessException(FileEnum.BATCH_FILE_DELETE_FAILED.getMessage() + ": " + e.getMessage());
        }

        return result;
    }

    /**
     * 检查文件是否存在
     */
    public boolean doesObjectExist(String objectName, String bucketCode) {
        return doesObjectExist(objectName, resolveBucketContext(bucketCode));
    }

    private boolean doesObjectExist(String objectName, BucketContext bucketContext) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketContext.bucketName())
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 列出指定前缀的对象
     */
    public List<Item> listObjects(String prefix, String bucketCode) {
        List<Item> items = new ArrayList<>();
        BucketContext bucketContext = resolveBucketContext(bucketCode);
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketContext.bucketName())
                    .prefix(prefix)
                    .recursive(true)
                    .build());
            for (Result<Item> result : results) {
                items.add(result.get());
            }
            return items;
        } catch (Exception e) {
            throw new BusinessException(FileEnum.FILE_LIST_FAILED.getMessage() + ": " + e.getMessage());
        }
    }

    /**
     * 生成唯一文件名
     */
    private String generateUniqueFileName(String originalFilename) {
        String suffix = "";
        if (originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uuid = UUID.randomUUID().toString().replace("-", "");
        String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

        return date + "/" + uuid + suffix;
    }

    /**
     * 获取文件详细信息
     */
    public FileInfo getFileInfo(String objectName, String bucketCode) {
        try {
            BucketContext bucketContext = resolveBucketContext(bucketCode);
            if (!doesObjectExist(objectName, bucketContext)) {
                throw new BusinessException(FileEnum.FILE_NOT_FOUND.getMessage());
            }

            StatObjectResponse statObject = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketContext.bucketName())
                    .object(objectName)
                    .build());

            String fileName = objectName;
            String path = "";
            if (objectName.contains("/")) {
                fileName = objectName.substring(objectName.lastIndexOf("/") + 1);
                path = objectName.substring(0, objectName.lastIndexOf("/") + 1);
            }

            String extension = "";
            if (fileName.contains(".")) {
                extension = fileName.substring(fileName.lastIndexOf("."));
            }

            String url = getPresignedObjectUrl(objectName, null, bucketContext.code());

            LocalDateTime lastModified = LocalDateTime.ofInstant(
                    statObject.lastModified().toInstant(),
                    ZoneId.systemDefault()
            );

            return FileInfo.builder()
                    .objectName(objectName)
                    .fileName(fileName)
                    .size(statObject.size())
                    .contentType(statObject.contentType())
                    .lastModified(lastModified)
                    .etag(statObject.etag())
                    .isDir(false)
                    .url(url)
                    .extension(extension)
                    .path(path)
                    .bucketName(bucketContext.bucketName())
                    .bucketCode(bucketContext.code())
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(FileEnum.FILE_INFO_FAILED.getMessage() + ": " + e.getMessage());
        }
    }

    /**
     * 获取文件详细信息（通过路径或URL）
     */
    public FileInfo getFileInfoByPath(String filePath, String bucketCode) {
        ObjectLocator locator = resolveLocatorByPath(filePath, bucketCode)
                .orElseThrow(() -> new BusinessException("无法解析文件路径: " + filePath));
        return getFileInfo(locator.objectName(), locator.bucketContext().code());
    }

    /**
     * 批量获取文件详细信息
     */
    public List<FileInfo> getBatchFileInfo(String[] objectNames, String bucketCode) {
        BucketContext bucketContext = resolveBucketContext(bucketCode);
        List<FileInfo> fileInfoList = new ArrayList<>();

        for (String input : objectNames) {
            try {
                String objectName = normalizeObjectName(
                        isMinIOUrl(input)
                                ? resolveLocatorByPath(input, bucketContext.code()).map(ObjectLocator::objectName).orElse(input)
                                : input);
                FileInfo fileInfo = getFileInfo(objectName, bucketContext.code());
                fileInfoList.add(fileInfo);
            } catch (Exception e) {
                fileInfoList.add(buildErrorFileInfo(input, e.getMessage()));
            }
        }

        return fileInfoList;
    }

    /**
     * 通过路径数组批量获取文件详细信息
     */
    public List<FileInfo> getBatchFileInfoByPath(String[] filePaths, String bucketCode) {
        List<FileInfo> fileInfoList = new ArrayList<>();
        for (String input : filePaths) {
            try {
                FileInfo fileInfo = getFileInfoByPath(input, bucketCode);
                fileInfoList.add(fileInfo);
            } catch (Exception e) {
                fileInfoList.add(buildErrorFileInfo(input, e.getMessage()));
            }
        }
        return fileInfoList;
    }

    private FileInfo buildErrorFileInfo(String input, String errorMessage) {
        FileInfo errorFileInfo = new FileInfo();
        errorFileInfo.setObjectName(input);
        errorFileInfo.setFileName(extractFileName(input));
        errorFileInfo.setSize(0L);
        errorFileInfo.setContentType("unknown");
        errorFileInfo.setLastModified(null);
        errorFileInfo.setEtag("error");
        errorFileInfo.setError(true);
        errorFileInfo.setErrorMessage(errorMessage);
        return errorFileInfo;
    }

    /**
     * 从路径中提取文件名
     */
    private String extractFileName(String path) {
        if (!StringUtils.hasText(path)) {
            return "unknown";
        }
        String normalizedPath = path.replace("\\", "/");
        int lastSlashIndex = normalizedPath.lastIndexOf("/");
        if (lastSlashIndex >= 0 && lastSlashIndex < normalizedPath.length() - 1) {
            return normalizedPath.substring(lastSlashIndex + 1);
        }
        return normalizedPath;
    }

    /**
     * 判断输入是否为MinIO URL
     */
    private boolean isMinIOUrl(String input) {
        return input != null && (input.startsWith("http://") || input.startsWith("https://"));
    }

    /**
     * 根据文件路径删除文件
     */
    public boolean removeObjectByPath(String filePath, String bucketCode) {
        try {
            ObjectLocator locator = resolveLocatorByPath(filePath, bucketCode)
                    .orElseThrow(() -> new BusinessException("无法从路径中解析对象: " + filePath));
            return removeObject(locator.objectName(), locator.bucketContext().code());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("根据文件路径删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件路径批量删除文件
     */
    public Map<String, String> removeObjectsByPath(List<String> filePaths, String bucketCode) {
        Map<String, String> result = new HashMap<>(filePaths.size());
        Map<BucketContext, List<String>> bucketObjects = new HashMap<>();

        for (String filePath : filePaths) {
            try {
                ObjectLocator locator = resolveLocatorByPath(filePath, bucketCode)
                        .orElseThrow(() -> new BusinessException("无法从路径中解析对象: " + filePath));
                bucketObjects.computeIfAbsent(locator.bucketContext(), key -> new ArrayList<>()).add(locator.objectName());
                result.put(filePath, "成功");
            } catch (Exception e) {
                result.put(filePath, "失败: " + e.getMessage());
            }
        }

        for (Map.Entry<BucketContext, List<String>> entry : bucketObjects.entrySet()) {
            BucketContext context = entry.getKey();
            List<String> objects = entry.getValue();
            try {
                Map<String, String> deleteResult = removeObjects(objects, context.code());
                result.replaceAll((path, status) -> {
                    if (!"成功".equals(status)) {
                        return status;
                    }
                    ObjectLocator locator = resolveLocatorByPath(path, context.code()).orElse(null);
                    if (locator == null) {
                        return status;
                    }
                    return deleteResult.getOrDefault(locator.objectName(), status);
                });
            } catch (Exception e) {
                log.error("批量删除桶 [{}] 对象失败: {}", context.bucketName(), e.getMessage());
                objects.forEach(object -> result.put(object, "失败: " + e.getMessage()));
            }
        }
        return result;
    }

    /**
     * 解析文件路径
     */
    private Optional<ObjectLocator> resolveLocatorByPath(String input, String bucketCode) {
        if (!StringUtils.hasText(input)) {
            return Optional.empty();
        }

        if (isMinIOUrl(input)) {
            return extractObjectLocatorFromUrl(input);
        }

        BucketContext bucketContext = resolveBucketContext(bucketCode);
        return Optional.of(new ObjectLocator(bucketContext, normalizeObjectName(input)));
    }

    private Optional<ObjectLocator> extractObjectLocatorFromUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return Optional.empty();
        }
        String urlWithoutQuery = url.split("\\?")[0];

        if (!CollectionUtils.isEmpty(minioProperties.getBuckets())) {
            for (MinioProperties.BucketPolicy policy : minioProperties.getBuckets()) {
                ObjectLocator locator = matchBucketInUrl(urlWithoutQuery, policy.normalizedCode(), policy.getBucketName());
                if (locator != null) {
                    return Optional.of(locator);
                }
            }
        }

        if (StringUtils.hasText(minioProperties.getBucketName())) {
            ObjectLocator locator = matchBucketInUrl(urlWithoutQuery, LEGACY_BUCKET_CODE, minioProperties.getBucketName());
            if (locator != null) {
                return Optional.of(locator);
            }
        }

        return Optional.empty();
    }

    private ObjectLocator matchBucketInUrl(String url, String bucketCode, String bucketName) {
        String marker = "/" + bucketName + "/";
        int index = url.indexOf(marker);
        if (index < 0) {
            return null;
        }
        String objectName = url.substring(index + marker.length());
        objectName = URLDecoder.decode(objectName, StandardCharsets.UTF_8);
        return new ObjectLocator(new BucketContext(bucketCode, bucketName), objectName);
    }

    private BucketContext resolveBucketContext(String bucketCode) {
        if (StringUtils.hasText(bucketCode)) {
            return minioProperties.findBucket(bucketCode)
                    .map(policy -> new BucketContext(policy.normalizedCode(), policy.getBucketName()))
                    .orElseThrow(() -> new BusinessException("未配置编码为 " + bucketCode + " 的存储桶"));
        }

        return minioProperties.resolveDefaultBucket()
                .map(policy -> new BucketContext(policy.normalizedCode(), policy.getBucketName()))
                .or(() -> StringUtils.hasText(minioProperties.getBucketName())
                        ? Optional.of(new BucketContext(LEGACY_BUCKET_CODE, minioProperties.getBucketName()))
                        : Optional.empty())
                .orElseThrow(() -> new BusinessException("MinIO默认存储桶未配置"));
    }

    private String normalizeObjectName(String objectName) {
        if (!StringUtils.hasText(objectName)) {
            return "";
        }
        String normalized = objectName.trim();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    /**
     * 桶上下文，包含桶编码和桶名称
     * 作为 Map 的 key 使用，已重写 equals 和 hashCode
     */
    private static class BucketContext {
        private final String code;
        private final String bucketName;

        public BucketContext(String code, String bucketName) {
            this.code = code;
            this.bucketName = bucketName;
        }

        public String code() {
            return code;
        }

        public String bucketName() {
            return bucketName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            BucketContext that = (BucketContext) o;
            return Objects.equals(code, that.code) && Objects.equals(bucketName, that.bucketName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(code, bucketName);
        }

        @Override
        public String toString() {
            return "BucketContext{" +
                    "code='" + code + '\'' +
                    ", bucketName='" + bucketName + '\'' +
                    '}';
        }
    }

    private record ObjectLocator(BucketContext bucketContext, String objectName) {
    }
}
