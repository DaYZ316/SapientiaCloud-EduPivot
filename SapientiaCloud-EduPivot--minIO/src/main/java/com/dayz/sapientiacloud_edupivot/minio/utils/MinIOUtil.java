package com.dayz.sapientiacloud_edupivot.minio.utils;

import com.dayz.sapientiacloud_edupivot.minio.config.MinioProperties;
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
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * MinIO工具类，提供文件上传、下载、删除等操作
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
     * @param file 文件
     * @param objectName 对象名，为空时使用文件原名
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
     * @param file 文件
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file) {
        return uploadFile(file, null, null);
    }

    /**
     * 上传字节数组
     * @param bytes 字节数组
     * @param objectName 对象名
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
     * @param objectName 对象名称
     * @param expiry 过期时间（以秒为单位），默认7天
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
     * @param objectName 对象名称
     * @return 文件URL
     */
    public String getPresignedObjectUrl(String objectName) {
        return getPresignedObjectUrl(objectName, null);
    }

    /**
     * 删除文件
     * @param objectName 对象名称
     * @return 是否删除成功
     */
    public boolean removeObject(String objectName) {
        try {
            if (!doesObjectExist(objectName)) {
                return true; // 文件不存在也视为删除成功
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
} 