package com.dayz.sapientiacloud_edupivot.minio.controller;

import com.dayz.sapientiacloud_edupivot.minio.enums.FileEnum;
import com.dayz.sapientiacloud_edupivot.minio.result.Result;
import com.dayz.sapientiacloud_edupivot.minio.utils.MinIOUtil;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

/**
 * 文件上传控制器
 * @author LANDH
 */
@Tag(name = "文件操作", description = "文件上传下载接口")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final MinIOUtil minIOUtil;

    /**
     * 获取所有存储桶
     * @return 存储桶列表
     */
    @Operation(summary = "获取所有存储桶", description = "获取所有存储桶接口")
    @GetMapping("/buckets")
    public Result<List<String>> listBuckets() {
        List<Bucket> buckets = minIOUtil.getAllBuckets();
        List<String> bucketNames = buckets.stream()
                .map(Bucket::name)
                .collect(Collectors.toList());
        return Result.success(bucketNames);
    }

    /**
     * 上传文件
     * @param file 文件
     * @param directory 目录（可选）
     * @return 文件信息
     */
    @Operation(summary = "上传文件", description = "上传文件接口")
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(
            @Parameter(description = "上传的文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "存储目录（可选）") @RequestParam(value = "directory", required = false) String directory
    ) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.fail(FileEnum.FILE_CANNOT_BE_EMPTY.getMessage());
            }
            
            String objectName = null;
            if (directory != null && !directory.isEmpty()) {
                // 确保目录以/结尾
                directory = directory.endsWith("/") ? directory : directory + "/";
                objectName = directory + file.getOriginalFilename();
            }
            
            // 上传文件并获取对象名
            String uploadedObjectName = minIOUtil.uploadFile(file, objectName, null);
            
            // 获取访问URL
            String url = minIOUtil.getPresignedObjectUrl(uploadedObjectName);
            
            // 构建返回结果
            Map<String, String> fileInfo = new HashMap<>();
            fileInfo.put("fileName", file.getOriginalFilename());
            fileInfo.put("objectName", uploadedObjectName);
            fileInfo.put("fileSize", String.valueOf(file.getSize()));
            fileInfo.put("contentType", file.getContentType());
            fileInfo.put("url", url);
            
            return Result.success(fileInfo);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return Result.fail(FileEnum.FILE_UPLOAD_FAILED.getMessage());
        }
    }

    /**
     * 获取文件URL
     * @param objectName 文件名称
     * @param expiry 过期时间（秒），可选
     * @return 文件URL
     */
    @Operation(summary = "获取文件URL", description = "获取文件URL接口")
    @GetMapping("/url")
    public Result<String> getFileUrl(
            @Parameter(description = "文件对象名称", required = true) @RequestParam("objectName") String objectName,
            @Parameter(description = "过期时间（秒）", required = false) @RequestParam(value = "expiry", required = false) Integer expiry
    ) {
        try {
            String url = minIOUtil.getPresignedObjectUrl(objectName, expiry);
            return Result.success(url);
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", e.getMessage(), e);
            return Result.fail(FileEnum.FILE_URL_GENERATION_FAILED.getMessage());
        }
    }

    /**
     * 下载文件
     * @param objectName 对象名称
     * @param response HTTP响应
     */
    @Operation(summary = "下载文件", description = "下载文件接口")
    @GetMapping("/download")
    public void downloadFile(
            @Parameter(description = "文件对象名称", required = true) @RequestParam("objectName") String objectName,
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
            try (InputStream inputStream = minIOUtil.downloadFile(objectName)) {
                IOUtils.copy(inputStream, response.getOutputStream());
                response.flushBuffer();
            }
        } catch (Exception e) {
            log.error("文件下载失败: {}", e.getMessage(), e);
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件下载失败: " + e.getMessage());
            } catch (Exception ex) {
                log.error("发送错误响应失败", ex);
            }
        }
    }

    /**
     * 删除文件
     * @param objectName 对象名称
     * @return 删除结果
     */
    @Operation(summary = "删除文件", description = "删除文件接口")
    @DeleteMapping("/delete")
    public Result<Boolean> deleteFile(
            @Parameter(description = "文件对象名称", required = true) @RequestParam("objectName") String objectName
    ) {
        boolean result = minIOUtil.removeObject(objectName);
        if (result) {
            return Result.success(true);
        } else {
            return Result.fail(FileEnum.FILE_DELETE_FAILED.getMessage());
        }
    }

    /**
     * 批量删除文件
     * @param objectNames 对象名称列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除文件", description = "批量删除文件接口")
    @DeleteMapping("/batch-delete")
    public Result<Map<String, String>> batchDeleteFiles(
            @Parameter(name = "objectNames", description = "文件对象名称列表", required = true) @RequestBody List<String> objectNames
    ) {
        Map<String, String> result = minIOUtil.removeObjects(objectNames);
        return Result.success(result);
    }

    /**
     * 列出指定前缀的文件
     * @param prefix 前缀
     * @return 文件列表
     */
    @Operation(summary = "列出指定前缀的文件", description = "列出指定前缀的文件接口")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> listFiles(
            @Parameter(description = "文件前缀", required = false)
            @RequestParam(value = "prefix", required = false, defaultValue = "") String prefix
    ) {
        try {
            List<Item> items = minIOUtil.listObjects(prefix);
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
                            log.error("获取文件信息失败", e);
                        }
                        return fileInfo;
                    })
                    .collect(Collectors.toList());
            return Result.success(fileList);
        } catch (Exception e) {
            log.error("列出文件失败: {}", e.getMessage(), e);
            return Result.fail(FileEnum.FILE_LIST_FAILED.getMessage());
        }
    }
} 