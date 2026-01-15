package com.dayz.sapientiacloud_edupivot.minio.controller;

import com.dayz.sapientiacloud_edupivot.minio.constant.MinIOConstants;
import com.dayz.sapientiacloud_edupivot.minio.entity.FileInfo;
import com.dayz.sapientiacloud_edupivot.minio.enums.BusinessBucketEnum;
import com.dayz.sapientiacloud_edupivot.minio.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.minio.result.Result;
import com.dayz.sapientiacloud_edupivot.minio.utils.MinIOUtil;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 *
 * @author LANDH
 */
@Tag(name = "文件操作", description = "文件上传下载接口")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class MinIOController {

    private final MinIOUtil minIOUtil;

    /**
     * 获取所有存储桶
     *
     * @return 存储桶列表
     */
    @Operation(summary = "listBuckets", description = "获取所有存储桶接口")
    @GetMapping("/buckets")
    public Result<List<String>> listBuckets() {
        List<Bucket> buckets = minIOUtil.getAllBuckets();
        List<String> bucketNames = buckets.stream()
                .map(Bucket::name)
                .toList();
        return Result.success(bucketNames);
    }

    /**
     * 上传文件
     *
     * @param file      文件
     * @param directory 目录（可选）
     * @return 文件信息
     */
    @Operation(summary = "uploadFile", description = "上传文件接口")
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(
            @Parameter(description = "上传的文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "存储目录（可选）") @RequestParam(value = "directory", required = false) String directory,
            @Parameter(description = "业务桶编码", required = false) @RequestParam(value = "bucketCode", required = false) BusinessBucketEnum bucketCode
    ) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.fail(MinIOConstants.FILE_CANNOT_BE_EMPTY_MESSAGE);
            }

            String objectName = null;
            if (directory != null && !directory.isEmpty()) {
                // 确保目录以/结尾
                directory = directory.endsWith("/") ? directory : directory + "/";
                objectName = directory + file.getOriginalFilename();
            }

            // 上传文件并获取对象名
            String resolvedBucketCode = resolveBucketCode(bucketCode);

            String uploadedObjectName = minIOUtil.uploadFile(file, resolvedBucketCode, objectName, null);

            // 获取访问URL
            String url = minIOUtil.getPresignedObjectUrl(uploadedObjectName, null, resolvedBucketCode);

            // 构建返回结果
            Map<String, String> fileInfo = new HashMap<>();
            fileInfo.put("fileName", file.getOriginalFilename());
            fileInfo.put("objectName", uploadedObjectName);
            fileInfo.put("fileSize", String.valueOf(file.getSize()));
            fileInfo.put("contentType", file.getContentType());
            fileInfo.put("url", url);
            fileInfo.put("bucketCode", resolvedBucketCode);

            return Result.success(fileInfo);
        } catch (Exception e) {
            throw new BusinessException(MinIOConstants.FILE_UPLOAD_FAILED_MESSAGE + ": " + e.getMessage());
        }
    }

    /**
     * 获取文件URL
     *
     * @param objectName 文件名称
     * @param expiry     过期时间（秒），可选
     * @return 文件URL
     */
    @Operation(summary = "getFileUrl", description = "获取文件URL接口")
    @GetMapping("/url")
    public Result<String> getFileUrl(
            @Parameter(description = "文件对象名称", required = true) @RequestParam("objectName") String objectName,
            @Parameter(description = "过期时间（秒）", required = false) @RequestParam(value = "expiry", required = false) Integer expiry,
            @Parameter(description = "业务桶编码", required = false) @RequestParam(value = "bucketCode", required = false) BusinessBucketEnum bucketCode
    ) {
        try {
            String url = minIOUtil.getPresignedObjectUrl(objectName, expiry, resolveBucketCode(bucketCode));
            return Result.success(url);
        } catch (Exception e) {
            throw new BusinessException(MinIOConstants.FILE_URL_GENERATION_FAILED_MESSAGE + ": " + e.getMessage());
        }
    }

    /**
     * 下载文件
     *
     * @param objectName 对象名称
     * @param response   HTTP响应
     */
    @Operation(summary = "downloadFile", description = "下载文件接口")
    @GetMapping("/download")
    public void downloadFile(
            @Parameter(description = "文件对象名称", required = true) @RequestParam("objectName") String objectName,
            @Parameter(description = "业务桶编码", required = false) @RequestParam(value = "bucketCode", required = false) BusinessBucketEnum bucketCode,
            HttpServletResponse response
    ) {
        try {
            // 从对象名中提取文件名
            String filename = objectName;
            if (objectName.contains("/")) {
                filename = objectName.substring(objectName.lastIndexOf("/") + 1);
            }

            // 设置响应头
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));

            // 获取并写入文件内容
            try (InputStream inputStream = minIOUtil.downloadFile(objectName, resolveBucketCode(bucketCode))) {
                IOUtils.copy(inputStream, response.getOutputStream());
                response.flushBuffer();
            }
        } catch (Exception e) {
            throw new BusinessException(MinIOConstants.FILE_DOWNLOAD_FAILED_MESSAGE + ": " + e.getMessage());
        }
    }

    /**
     * 下载文件（返回字节数组，用于Feign调用）
     *
     * @param objectName 对象名称
     * @param bucketCode 业务桶编码
     * @return 文件字节数组
     */
    @Operation(summary = "downloadFileBytes", description = "下载文件接口（返回字节数组）")
    @GetMapping("/download/bytes")
    public Result<byte[]> downloadFileBytes(
            @Parameter(description = "文件对象名称", required = true) @RequestParam("objectName") String objectName,
            @Parameter(description = "业务桶编码", required = false) @RequestParam(value = "bucketCode", required = false) BusinessBucketEnum bucketCode
    ) {
        try {
            try (InputStream inputStream = minIOUtil.downloadFile(objectName, resolveBucketCode(bucketCode))) {
                byte[] bytes = IOUtils.toByteArray(inputStream);
                return Result.success(bytes);
            }
        } catch (Exception e) {
            throw new BusinessException(MinIOConstants.FILE_DOWNLOAD_FAILED_MESSAGE + ": " + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 对象名称
     * @return 删除结果
     */
    @Operation(summary = "deleteFile", description = "删除文件接口")
    @DeleteMapping("/delete")
    public Result<Boolean> deleteFile(
            @Parameter(description = "文件对象名称", required = true) @RequestParam("objectName") String objectName,
            @Parameter(description = "业务桶编码", required = false) @RequestParam(value = "bucketCode", required = false) BusinessBucketEnum bucketCode
    ) {
        boolean result = minIOUtil.removeObject(objectName, resolveBucketCode(bucketCode));
        if (result) {
            return Result.success(true);
        } else {
            return Result.fail(MinIOConstants.FILE_DELETE_FAILED_MESSAGE);
        }
    }

    /**
     * 批量删除文件
     *
     * @param objectNames 对象名称列表
     * @return 删除结果
     */
    @Operation(summary = "batchDeleteFiles", description = "批量删除文件接口")
    @DeleteMapping("/batch-delete")
    public Result<Map<String, String>> batchDeleteFiles(
            @Parameter(name = "objectNames", description = "文件对象名称列表", required = true) @RequestBody List<String> objectNames,
            @Parameter(description = "业务桶编码", required = false) @RequestParam(value = "bucketCode", required = false) BusinessBucketEnum bucketCode
    ) {
        Map<String, String> result = minIOUtil.removeObjects(objectNames, resolveBucketCode(bucketCode));
        return Result.success(result);
    }

    /**
     * 列出指定前缀的文件
     *
     * @param prefix 前缀
     * @return 文件列表
     */
    @Operation(summary = "listFiles", description = "列出指定前缀的文件接口")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> listFiles(
            @Parameter(description = "文件前缀", required = false)
            @RequestParam(value = "prefix", required = false, defaultValue = "") String prefix,
            @Parameter(description = "业务桶编码", required = false) @RequestParam(value = "bucketCode", required = false) BusinessBucketEnum bucketCode
    ) {
        try {
            List<Item> items = minIOUtil.listObjects(prefix, resolveBucketCode(bucketCode));
            List<Map<String, Object>> fileList = items.stream()
                    .map(item -> {
                        Map<String, Object> fileInfo = new HashMap<>();
                        try {
                            fileInfo.put("objectName", item.objectName());
                            fileInfo.put("size", item.size());
                            fileInfo.put("lastModified", item.lastModified());
                            fileInfo.put("isDir", item.isDir());
                            fileInfo.put("etag", item.etag());
                        } catch (Exception e) {
                            throw new BusinessException(MinIOConstants.FILE_INFO_ERROR_LOG + ": " + e.getMessage());
                        }
                        return fileInfo;
                    })
                    .toList();
            return Result.success(fileList);
        } catch (Exception e) {
            throw new BusinessException(MinIOConstants.FILE_LIST_FAILED_MESSAGE + ": " + e.getMessage());
        }
    }

    /**
     * 获取文件详细信息
     *
     * @param objectName 对象名称
     * @return 文件详细信息
     */
    @Operation(summary = "getFileInfo", description = "获取文件详细信息接口")
    @GetMapping("/info")
    public Result<FileInfo> getFileInfo(
            @Parameter(description = "文件对象名称", required = true) @RequestParam("objectName") String objectName,
            @Parameter(description = "业务桶编码", required = false) @RequestParam(value = "bucketCode", required = false) BusinessBucketEnum bucketCode
    ) {
        try {
            FileInfo fileInfo = minIOUtil.getFileInfo(objectName, resolveBucketCode(bucketCode));
            return Result.success(fileInfo);
        } catch (Exception e) {
            throw new BusinessException(MinIOConstants.FILE_INFO_FAILED_MESSAGE + ": " + e.getMessage());
        }
    }

    /**
     * 通过路径获取文件详细信息
     *
     * @param filePath 文件路径
     * @return 文件详细信息
     */
    @Operation(summary = "getFileInfoByPath", description = "通过路径获取文件详细信息接口")
    @GetMapping("/info/path")
    public Result<FileInfo> getFileInfoByPath(
            @Parameter(description = "文件路径", required = true) @RequestParam("filePath") String filePath,
            @Parameter(description = "业务桶编码", required = false) @RequestParam(value = "bucketCode", required = false) BusinessBucketEnum bucketCode
    ) {
        try {
            FileInfo fileInfo = minIOUtil.getFileInfoByPath(filePath, resolveBucketCode(bucketCode));
            return Result.success(fileInfo);
        } catch (Exception e) {
            throw new BusinessException(MinIOConstants.FILE_INFO_FAILED_MESSAGE + ": " + e.getMessage());
        }
    }

    /**
     * 批量获取文件详细信息
     *
     * @param objectNames 对象名称数组
     * @return 文件详细信息列表
     */
    @Operation(summary = "getBatchFileInfo", description = "批量获取文件详细信息接口")
    @PostMapping("/info/batch")
    public Result<List<FileInfo>> getBatchFileInfo(
            @Parameter(description = "文件对象名称数组", required = true) @RequestBody String[] objectNames,
            @Parameter(description = "业务桶编码", required = false) @RequestParam(value = "bucketCode", required = false) BusinessBucketEnum bucketCode
    ) {
        try {
            List<FileInfo> fileInfoList = minIOUtil.getBatchFileInfo(objectNames, resolveBucketCode(bucketCode));
            return Result.success(fileInfoList);
        } catch (Exception e) {
            throw new BusinessException(MinIOConstants.FILE_INFO_FAILED_MESSAGE + ": " + e.getMessage());
        }
    }

    /**
     * 通过路径数组批量获取文件详细信息
     *
     * @param filePaths 文件路径数组
     * @return 文件详细信息列表
     */
    @Operation(summary = "getBatchFileInfoByPath", description = "通过路径数组批量获取文件详细信息接口")
    @PostMapping("/info/batch/path")
    public Result<List<FileInfo>> getBatchFileInfoByPath(
            @Parameter(description = "文件路径数组", required = true) @RequestBody String[] filePaths,
            @Parameter(description = "业务桶编码", required = false) @RequestParam(value = "bucketCode", required = false) BusinessBucketEnum bucketCode
    ) {
        try {
            List<FileInfo> fileInfoList = minIOUtil.getBatchFileInfoByPath(filePaths, resolveBucketCode(bucketCode));
            return Result.success(fileInfoList);
        } catch (Exception e) {
            throw new BusinessException(MinIOConstants.FILE_INFO_FAILED_MESSAGE + ": " + e.getMessage());
        }
    }

    /**
     * 根据文件路径删除文件
     *
     * @param filePath 文件路径
     * @return 删除结果
     */
    @Operation(summary = "deleteFileByPath", description = "根据文件路径删除文件接口")
    @DeleteMapping("/delete/path")
    public Result<Boolean> deleteFileByPath(
            @Parameter(description = "文件路径", required = true) @RequestParam("filePath") String filePath,
            @Parameter(description = "业务桶编码", required = false) @RequestParam(value = "bucketCode", required = false) BusinessBucketEnum bucketCode
    ) {
        try {
            boolean result = minIOUtil.removeObjectByPath(filePath, resolveBucketCode(bucketCode));
            if (result) {
                return Result.success(true);
            } else {
                return Result.fail(MinIOConstants.FILE_DELETE_FAILED_MESSAGE);
            }
        } catch (Exception e) {
            throw new BusinessException(MinIOConstants.FILE_DELETE_BY_URL_FAILED_MESSAGE + ": " + e.getMessage());
        }
    }

    /**
     * 根据文件路径批量删除文件
     *
     * @param filePaths 文件路径列表
     * @return 删除结果
     */
    @Operation(summary = "batchDeleteFilesByPath", description = "根据文件路径批量删除文件接口")
    @DeleteMapping("/batch-delete/path")
    public Result<Map<String, String>> batchDeleteFilesByPath(
            @Parameter(name = "filePaths", description = "文件路径列表", required = true) @RequestBody List<String> filePaths,
            @Parameter(description = "业务桶编码", required = false) @RequestParam(value = "bucketCode", required = false) BusinessBucketEnum bucketCode
    ) {
        try {
            Map<String, String> result = minIOUtil.removeObjectsByPath(filePaths, resolveBucketCode(bucketCode));
            return Result.success(result);
        } catch (Exception e) {
            throw new BusinessException(MinIOConstants.FILE_DELETE_BATCH_BY_URL_FAILED_MESSAGE + ": " + e.getMessage());
        }
    }

    private String resolveBucketCode(BusinessBucketEnum bucketEnum) {
        return bucketEnum == null ? null : bucketEnum.getBucketCode();
    }
} 