package com.dayz.sapientiacloud_edupivot.minio.utils;

import com.dayz.sapientiacloud_edupivot.minio.config.MinioProperties;
import com.dayz.sapientiacloud_edupivot.minio.entity.dto.FileInfoDTO;
import com.dayz.sapientiacloud_edupivot.minio.enums.FileEnum;
import com.dayz.sapientiacloud_edupivot.minio.exception.BusinessException;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * MinIO工具类，提供文件上传、下载、删除等操作
 *
 * @author LANDH
 */
@Component
@Slf4j
public class MinIOUtil {

    @Resource
    private MinioProperties minioProperties;

    private MinioClient minioClient;

    @PostConstruct
    private void init() {
        createMinioClient();
        createBucketIfNotExists();
    }

    /**
     * 创建MinioClient客户端
     */
    private void createMinioClient() {
        try {
            String endpoint = "http://" + minioProperties.getIp() + ":" + minioProperties.getPort();
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();
            log.info("MinIO客户端初始化成功");
        } catch (Exception e) {
            log.error("MinIO客户端初始化失败: {}", e.getMessage());
            throw new BusinessException(FileEnum.MINIO_CLIENT_INIT_FAILED.getMessage());
        }
    }

    /**
     * 如果存储桶不存在，则创建
     */
    private void createBucketIfNotExists() {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioProperties.getBucketName())
                        .build());
                log.info("创建存储桶: {}", minioProperties.getBucketName());
            }
        } catch (Exception e) {
            log.error("创建存储桶失败: {}", e.getMessage());
            throw new BusinessException(FileEnum.MINIO_BUCKET_CREATE_FAILED.getMessage());
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
            log.error("获取存储桶列表失败: {}", e.getMessage());
            throw new BusinessException(FileEnum.MINIO_GET_BUCKETS_FAILED.getMessage());
        }
    }

    /**
     * 上传文件
     *
     * @param file        文件
     * @param objectName  对象名，为空时使用文件原名
     * @param contentType 内容类型，为空时自动检测
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String objectName, String contentType) {
        try {
            if (file == null || file.isEmpty()) {
                throw new BusinessException(FileEnum.FILE_CANNOT_BE_EMPTY.getMessage());
            }

            // 生成文件名
            String fileName = objectName;
            if (fileName == null || fileName.isEmpty()) {
                fileName = generateUniqueFileName(Objects.requireNonNull(file.getOriginalFilename()));
            }

            // 检测内容类型
            String fileContentType = contentType;
            if (fileContentType == null || fileContentType.isEmpty()) {
                fileContentType = file.getContentType();
            }

            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(fileContentType)
                    .build());

            log.info("文件上传成功: {}", fileName);
            return fileName;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new BusinessException(FileEnum.FILE_UPLOAD_FAILED.getMessage());
        }
    }

    /**
     * 上传文件(简化版)
     *
     * @param file 文件
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, null, null);
    }

    /**
     * 上传字节数组
     *
     * @param bytes       字节数组
     * @param objectName  对象名
     * @param contentType 内容类型
     * @return 文件访问URL
     */
    public String uploadBytes(byte[] bytes, String objectName, String contentType) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .stream(bais, bytes.length, -1)
                    .contentType(contentType)
                    .build());
            log.info("字节数组上传成功: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("字节数组上传失败: {}", e.getMessage());
            throw new BusinessException(FileEnum.BYTES_UPLOAD_FAILED.getMessage());
        }
    }

    /**
     * 下载文件
     *
     * @param objectName 对象名称
     * @return 文件流
     */
    public InputStream downloadFile(String objectName) {
        try {
            if (!doesObjectExist(objectName)) {
                throw new BusinessException(FileEnum.FILE_NOT_FOUND.getMessage());
            }

            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .build());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage());
            throw new BusinessException(FileEnum.FILE_DOWNLOAD_FAILED.getMessage());
        }
    }

    /**
     * 获取文件外链
     *
     * @param objectName 对象名称
     * @param expiry     过期时间（以秒为单位），默认7天
     * @return 文件URL
     */
    public String getPresignedObjectUrl(String objectName, Integer expiry) {
        try {
            if (!doesObjectExist(objectName)) {
                throw new BusinessException(FileEnum.FILE_NOT_FOUND.getMessage());
            }

            int expiryTime = expiry != null ? expiry : 7 * 24 * 3600;
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(expiryTime, TimeUnit.SECONDS)
                    .build());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", e.getMessage());
            throw new BusinessException(FileEnum.FILE_URL_GENERATION_FAILED.getMessage());
        }
    }

    /**
     * 获取文件外链（默认过期时间）
     *
     * @param objectName 对象名称
     * @return 文件URL
     */
    public String getPresignedObjectUrl(String objectName) {
        return getPresignedObjectUrl(objectName, null);
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     * @return 是否删除成功
     */
    public boolean removeObject(String objectName) {
        try {
            if (!doesObjectExist(objectName)) {
                return true;
            }

            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 批量删除文件
     *
     * @param objectNames 对象名称列表
     * @return 删除结果
     */
    public Map<String, String> removeObjects(List<String> objectNames) {
        Map<String, String> result = new HashMap<>(objectNames.size());
        List<DeleteObject> objects = new ArrayList<>(objectNames.size());

        // 构建删除对象列表
        for (String objectName : objectNames) {
            objects.add(new DeleteObject(objectName));
            result.put(objectName, "成功");
        }

        try {
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(RemoveObjectsArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .objects(objects)
                    .build());

            // 收集删除失败的对象
            for (Result<DeleteError> r : results) {
                DeleteError error = r.get();
                result.put(error.objectName(), "失败: " + error.message());
            }
        } catch (Exception e) {
            log.error("批量删除文件失败: {}", e.getMessage());
            throw new BusinessException(FileEnum.BATCH_FILE_DELETE_FAILED.getMessage());
        }

        return result;
    }

    /**
     * 检查文件是否存在
     *
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean doesObjectExist(String objectName) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 列出指定前缀的对象
     *
     * @param prefix 前缀
     * @return 对象列表
     */
    public List<Item> listObjects(String prefix) {
        List<Item> items = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .prefix(prefix)
                    .recursive(true)
                    .build());
            for (Result<Item> result : results) {
                items.add(result.get());
            }
            return items;
        } catch (Exception e) {
            log.error("列出对象失败: {}", e.getMessage());
            throw new BusinessException(FileEnum.FILE_LIST_FAILED.getMessage());
        }
    }

    /**
     * 生成唯一文件名
     *
     * @param originalFilename 原始文件名
     * @return 唯一文件名
     */
    private String generateUniqueFileName(String originalFilename) {
        // 获取文件后缀
        String suffix = "";
        if (originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 生成UUID作为文件名，并按日期分目录
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String date = new java.text.SimpleDateFormat("yyyy/MM/dd").format(new Date());

        return date + "/" + uuid + suffix;
    }

    /**
     * 获取文件详细信息
     *
     * @param objectName 对象名称
     * @return 文件详细信息
     */
    public FileInfoDTO getFileInfo(String objectName) {
        try {
            if (!doesObjectExist(objectName)) {
                throw new BusinessException(FileEnum.FILE_NOT_FOUND.getMessage());
            }

            // 获取对象统计信息
            StatObjectResponse statObject = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .build());

            // 从对象名中提取文件名和路径
            String fileName = objectName;
            String path = "";
            if (objectName.contains("/")) {
                fileName = objectName.substring(objectName.lastIndexOf("/") + 1);
                path = objectName.substring(0, objectName.lastIndexOf("/") + 1);
            }

            // 获取文件扩展名
            String extension = "";
            if (fileName.contains(".")) {
                extension = fileName.substring(fileName.lastIndexOf("."));
            }

            // 获取文件访问URL
            String url = getPresignedObjectUrl(objectName);

            // 转换时间
            LocalDateTime lastModified = LocalDateTime.ofInstant(
                    statObject.lastModified().toInstant(),
                    ZoneId.systemDefault()
            );

            return FileInfoDTO.builder()
                    .objectName(objectName)
                    .fileName(fileName)
                    .size(statObject.size())
                    .contentType(statObject.contentType())
                    .lastModified(lastModified)
                    .etag(statObject.etag())
                    .isDir(false) // 文件对象不是目录
                    .url(url)
                    .extension(extension)
                    .path(path)
                    .bucketName(minioProperties.getBucketName())
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取文件信息失败: {}", e.getMessage());
            throw new BusinessException(FileEnum.FILE_INFO_FAILED.getMessage());
        }
    }

    /**
     * 获取文件详细信息（通过路径）
     *
     * @param filePath 文件路径
     * @return 文件详细信息
     */
    public FileInfoDTO getFileInfoByPath(String filePath) {
        // 确保路径格式正确
        String objectName = filePath;
        if (!objectName.startsWith("/")) {
            objectName = "/" + objectName;
        }
        if (objectName.startsWith("/")) {
            objectName = objectName.substring(1);
        }
        
        return getFileInfo(objectName);
    }

    /**
     * 批量获取文件详细信息
     *
     * @param objectNames 对象名称数组
     * @return 文件详细信息列表
     * @throws Exception 异常
     */
    public List<FileInfoDTO> getBatchFileInfo(String[] objectNames) throws Exception {
        List<FileInfoDTO> fileInfoList = new ArrayList<>();
        
        for (String input : objectNames) {
            try {
                // 判断输入是否为URL，如果是则提取对象名称
                String objectName = isMinIOUrl(input) ? extractObjectNameFromUrl(input) : input;
                
                FileInfoDTO fileInfo = getFileInfo(objectName);
                fileInfoList.add(fileInfo);
            } catch (Exception e) {
                log.warn("获取文件信息失败: {}, 错误: {}", input, e.getMessage());
                // 创建一个包含错误信息的FileInfoDTO
                FileInfoDTO errorFileInfo = new FileInfoDTO();
                errorFileInfo.setObjectName(input);
                errorFileInfo.setFileName(extractFileName(input));
                errorFileInfo.setSize(0L);
                errorFileInfo.setContentType("unknown");
                errorFileInfo.setLastModified(null);
                errorFileInfo.setEtag("error");
                errorFileInfo.setError(true);
                errorFileInfo.setErrorMessage(e.getMessage());
                fileInfoList.add(errorFileInfo);
            }
        }
        
        return fileInfoList;
    }

    /**
     * 通过路径数组批量获取文件详细信息
     *
     * @param filePaths 文件路径数组
     * @return 文件详细信息列表
     * @throws Exception 异常
     */
    public List<FileInfoDTO> getBatchFileInfoByPath(String[] filePaths) throws Exception {
        List<FileInfoDTO> fileInfoList = new ArrayList<>();
        
        for (String input : filePaths) {
            try {
                // 判断输入是否为URL，如果是则提取对象名称
                String objectName = isMinIOUrl(input) ? extractObjectNameFromUrl(input) : input;
                
                FileInfoDTO fileInfo = getFileInfoByPath(objectName);
                fileInfoList.add(fileInfo);
            } catch (Exception e) {
                log.warn("通过路径获取文件信息失败: {}, 错误: {}", input, e.getMessage());
                // 创建一个包含错误信息的FileInfoDTO
                FileInfoDTO errorFileInfo = new FileInfoDTO();
                errorFileInfo.setObjectName(input);
                errorFileInfo.setFileName(extractFileName(input));
                errorFileInfo.setSize(0L);
                errorFileInfo.setContentType("unknown");
                errorFileInfo.setLastModified(null);
                errorFileInfo.setEtag("error");
                errorFileInfo.setError(true);
                errorFileInfo.setErrorMessage(e.getMessage());
                fileInfoList.add(errorFileInfo);
            }
        }
        
        return fileInfoList;
    }

    /**
     * 从路径中提取文件名
     *
     * @param path 文件路径
     * @return 文件名
     */
    private String extractFileName(String path) {
        if (path == null || path.isEmpty()) {
            return "unknown";
        }
        
        // 处理路径分隔符
        String normalizedPath = path.replace("\\", "/");
        int lastSlashIndex = normalizedPath.lastIndexOf("/");
        
        if (lastSlashIndex >= 0 && lastSlashIndex < normalizedPath.length() - 1) {
            return normalizedPath.substring(lastSlashIndex + 1);
        }
        
        return normalizedPath;
    }

    /**
     * 从MinIO URL中提取对象名称
     *
     * @param url MinIO URL
     * @return 对象名称
     */
    private String extractObjectNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        
        try {
            // 移除查询参数
            String urlWithoutQuery = url.split("\\?")[0];
            
            // 查找bucket名称后的路径部分
            // URL格式: http://host:port/bucket-name/object-path
            String[] parts = urlWithoutQuery.split("/");
            
            // 找到bucket名称后的部分
            boolean foundBucket = false;
            StringBuilder objectName = new StringBuilder();
            
            for (String part : parts) {
                if (foundBucket) {
                    if (objectName.length() > 0) {
                        objectName.append("/");
                    }
                    objectName.append(part);
                } else if (part.contains("sapientiacloud-edupivot")) {
                    // 找到bucket名称
                    foundBucket = true;
                }
            }
            
            String result = objectName.toString();
            
            // URL解码
            if (!result.isEmpty()) {
                result = URLDecoder.decode(result, StandardCharsets.UTF_8);
            }
            
            return result;
        } catch (Exception e) {
            log.warn("解析URL失败: {}, 错误: {}", url, e.getMessage());
            return url; // 如果解析失败，返回原始URL
        }
    }

    /**
     * 判断输入是否为MinIO URL
     *
     * @param input 输入字符串
     * @return 是否为URL
     */
    private boolean isMinIOUrl(String input) {
        return input != null && (input.startsWith("http://") || input.startsWith("https://"));
    }

    /**
     * 根据文件路径删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public boolean removeObjectByPath(String filePath) {
        try {
            if (!isMinIOUrl(filePath)) {
                throw new BusinessException("无效的URL格式");
            }
            
            String objectName = extractObjectNameFromUrl(filePath);
            if (objectName.isEmpty()) {
                throw new BusinessException("无法从URL中提取对象名称");
            }
            
            return removeObject(objectName);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("根据文件路径删除文件失败: {}", e.getMessage());
            throw new BusinessException("根据文件路径删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 根据文件路径批量删除文件
     *
     * @param filePaths 文件路径列表
     * @return 删除结果
     */
    public Map<String, String> removeObjectsByPath(List<String> filePaths) {
        Map<String, String> result = new HashMap<>(filePaths.size());
        List<String> objectNames = new ArrayList<>();
        
        // 提取所有文件路径中的对象名称
        for (String filePath : filePaths) {
            try {
                if (!isMinIOUrl(filePath)) {
                    result.put(filePath, "失败: 无效的URL格式");
                    continue;
                }
                
                String objectName = extractObjectNameFromUrl(filePath);
                if (objectName.isEmpty()) {
                    result.put(filePath, "失败: 无法从URL中提取对象名称");
                    continue;
                }
                
                objectNames.add(objectName);
                result.put(filePath, "成功");
            } catch (Exception e) {
                result.put(filePath, "失败: " + e.getMessage());
            }
        }
        
        // 如果没有有效的对象名称，直接返回结果
        if (objectNames.isEmpty()) {
            return result;
        }
        
        try {
            // 执行批量删除
            Map<String, String> deleteResult = removeObjects(objectNames);
            
            // 更新结果，将对象名称映射回文件路径
            Map<String, String> finalResult = new HashMap<>();
            for (Map.Entry<String, String> entry : result.entrySet()) {
                String filePath = entry.getKey();
                String status = entry.getValue();
                
                if ("成功".equals(status)) {
                    String objectName = extractObjectNameFromUrl(filePath);
                    String deleteStatus = deleteResult.get(objectName);
                    if (deleteStatus != null) {
                        finalResult.put(filePath, deleteStatus);
                    } else {
                        finalResult.put(filePath, "成功");
                    }
                } else {
                    finalResult.put(filePath, status);
                }
            }
            
            return finalResult;
        } catch (Exception e) {
            log.error("根据文件路径批量删除文件失败: {}", e.getMessage());
            throw new BusinessException("根据文件路径批量删除文件失败: " + e.getMessage());
        }
    }
} 