package com.dayz.sapientiacloud_edupivot.celestial_hub.service.impl;

import com.dayz.sapientiacloud_edupivot.celestial_hub.clients.MinIOClient;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.FileDocumentConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileQueryDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileUploadDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileVectorizeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.FileDocument;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.FileInfo;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.FileDocumentVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.BusinessBucketEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.FileDocumentEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.FileStatusEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.FileTypeEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.repository.FileDocumentRepository;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.FileVectorizeKafkaService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.IFileDocumentService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.FileUtil;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileDocumentServiceImpl implements IFileDocumentService {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#.##");
    private static final int MAX_CONCURRENT_UPLOADS = 5;

    private final FileDocumentRepository fileDocumentRepository;
    private final MinIOClient minIOClient;
    private final MongoTemplate mongoTemplate;
    private final FileVectorizeKafkaService fileVectorizeKafkaService;

    // 用于并发上传的线程池
    private final ExecutorService uploadExecutor = Executors.newFixedThreadPool(MAX_CONCURRENT_UPLOADS);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileDocumentVO uploadFile(MultipartFile file, FileUploadDTO request) {
        UUID userId = UserContextUtil.getCurrentUserId();
        return uploadFileInternal(file, request, userId);
    }

    private FileDocumentVO uploadFileInternal(MultipartFile file, FileUploadDTO request, UUID userId) {
        // 验证文件
        validateFile(file);

        // 上传到MinIO
        String bucketCode = BusinessBucketEnum.AI_QA_ASSET.getBucketCode();
        String directory = FileDocumentConstants.FILE_STORAGE_DIRECTORY_PREFIX + userId + FileDocumentConstants.PATH_SEPARATOR;
        Result<Map<String, String>> uploadResult = minIOClient.uploadFile(file, directory, bucketCode);

        if (uploadResult == null || !uploadResult.isSuccess()) {
            throw new BusinessException(FileDocumentEnum.FILE_UPLOAD_FAILED);
        }

        Map<String, String> fileInfo = uploadResult.getData();
        String objectName = fileInfo.get(FileDocumentConstants.MINIO_OBJECT_NAME_FIELD);
        String fileName = file.getOriginalFilename();
        String mimeType = file.getContentType();
        long fileSize = file.getSize();

        // 确定文件类型
        FileTypeEnum fileType = FileTypeEnum.fromMimeType(mimeType);
        if (fileType == null) {
            fileType = FileTypeEnum.fromExtension(FileUtil.getFileExtension(fileName));
        }
        if (fileType == null || !fileType.isParseSupported()) {
            throw new BusinessException(FileDocumentEnum.FILE_TYPE_NOT_SUPPORTED);
        }

        // 创建文件文档实体
        FileDocument fileDocument = new FileDocument();
        fileDocument.setId(UuidCreator.getTimeOrderedEpoch());
        fileDocument.setFileName(fileName);
        fileDocument.setFileType(fileType.getCode());
        fileDocument.setFileSize(fileSize);
        fileDocument.setMimeType(mimeType);
        fileDocument.setStoragePath(objectName);
        fileDocument.setBucketCode(bucketCode);
        fileDocument.setSysUserId(userId);
        fileDocument.setCourseId(request != null ? request.getCourseId() : null);
        fileDocument.setSessionId(request != null ? request.getSessionId() : null);
        fileDocument.setStatus(FileStatusEnum.PROCESSING.getCode());
        fileDocument.setIsVectorized(false);
        fileDocument.setVectorCount(0);
        fileDocument.setCreateTime(LocalDateTime.now());
        fileDocument.setUpdateTime(LocalDateTime.now());

        // 保存到数据库
        fileDocument = fileDocumentRepository.save(fileDocument);

        // 通过Kafka异步解析和向量化
        if (request != null && Boolean.TRUE.equals(request.getAutoVectorize())) {
            sendVectorizeTaskToKafka(fileDocument);
        }

        return convertToVO(fileDocument);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FileDocumentVO> uploadFiles(List<MultipartFile> files, FileUploadDTO request) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        UUID userId = UserContextUtil.getCurrentUserId();

        // 使用 CompletableFuture 实现并发上传，提高性能
        List<CompletableFuture<FileDocumentVO>> futures = files.stream()
                .map(file -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return uploadFileInternal(file, request, userId);
                    } catch (Exception e) {
                        log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                        return null; // 返回null表示上传失败
                    }
                }, uploadExecutor))
                .toList();

        // 等待所有上传任务完成
        List<FileDocumentVO> results = new ArrayList<>();
        for (CompletableFuture<FileDocumentVO> future : futures) {
            try {
                FileDocumentVO vo = future.get(30, TimeUnit.SECONDS);
                if (vo != null) {
                    results.add(vo);
                }
            } catch (Exception e) {
                log.error("Error waiting for file upload completion", e);
            }
        }

        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public FileDocumentVO getFileById(UUID id) {
        FileDocument fileDocument = getFileDocumentById(id);
        return convertToVO(fileDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public FileDocument getFileDocumentById(UUID id) {
        if (id == null) {
            throw new BusinessException(FileDocumentEnum.FILE_ID_REQUIRED);
        }
        return fileDocumentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(FileDocumentEnum.FILE_NOT_EXISTS));
    }

    @Override
    @Transactional(readOnly = true)
    public InputStream downloadFile(UUID id) {
        FileDocument fileDocument = getFileDocumentById(id);
        // 从MinIO下载文件
        Result<byte[]> downloadResult = minIOClient.downloadFile(
                fileDocument.getStoragePath(),
                fileDocument.getBucketCode()
        );

        if (downloadResult == null || !downloadResult.isSuccess() || downloadResult.getData() == null) {
            throw new BusinessException(FileDocumentEnum.FILE_DOWNLOAD_FAILED);
        }

        return new java.io.ByteArrayInputStream(downloadResult.getData());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteFile(UUID id) {
        FileDocument fileDocument = getFileDocumentById(id);

        // 从MinIO删除文件
        try {
            Result<Boolean> deleteResult = minIOClient.deleteFile(
                    fileDocument.getStoragePath(),
                    fileDocument.getBucketCode()
            );
            if (deleteResult == null || !deleteResult.isSuccess()) {
                log.warn("Failed to delete file from MinIO: {}", fileDocument.getStoragePath());
            }
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}", e.getMessage(), e);
        }

        // 更新状态为已删除
        fileDocument.setStatus(FileStatusEnum.DELETED.getCode());
        fileDocument.setUpdateTime(LocalDateTime.now());
        fileDocumentRepository.save(fileDocument);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileDocumentVO> listFiles(FileQueryDTO query, Pageable page) {
        Query mongoQuery = buildQuery(query);
        long total = mongoTemplate.count(mongoQuery, FileDocument.class);
        List<FileDocument> files = mongoTemplate.find(mongoQuery.with(page), FileDocument.class);
        // 批量转换，避免潜在的N+1问题
        List<FileDocumentVO> vos = convertToVOList(files);
        return new PageImpl<>(vos, page, total);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean vectorizeFile(UUID id) {
        FileDocument fileDocument = getFileDocumentById(id);
        sendVectorizeTaskToKafka(fileDocument);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileDocument> getFilesByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        return fileDocumentRepository.findActiveByIds(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileInfo> getFileInfosBySessionId(UUID sessionId) {
        if (sessionId == null) {
            return new ArrayList<>();
        }
        List<FileDocument> documents = fileDocumentRepository.findActiveBySessionId(sessionId);
        if (documents == null || documents.isEmpty()) {
            return new ArrayList<>();
        }
        return documents.stream()
                .map(this::convertToFileInfo)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 发送文件向量化任务到Kafka
     */
    private void sendVectorizeTaskToKafka(FileDocument fileDocument) {
        try {
            FileVectorizeRequestDTO request = new FileVectorizeRequestDTO();
            request.setFileId(fileDocument.getId());
            request.setFileName(fileDocument.getFileName());
            request.setFileType(fileDocument.getFileType());
            request.setStoragePath(fileDocument.getStoragePath());
            request.setBucketCode(fileDocument.getBucketCode());
            request.setMimeType(fileDocument.getMimeType());
            request.setCourseId(fileDocument.getCourseId());
            request.setSessionId(fileDocument.getSessionId());
            request.setUserId(fileDocument.getSysUserId());

            fileVectorizeKafkaService.sendVectorizeTask(request);
            log.debug("文件向量化任务已发送到Kafka, fileId: {}", fileDocument.getId());
        } catch (Exception e) {
            log.error("发送文件向量化任务到Kafka失败, fileId: {}, error: {}",
                    fileDocument.getId(), e.getMessage(), e);
            // 更新状态为失败
            fileDocument.setStatus(FileStatusEnum.FAILED.getCode());
            fileDocument.setParseError(FileDocumentConstants.ERROR_VECTORIZE_TASK_FAILED_PREFIX + e.getMessage());
            fileDocument.setUpdateTime(LocalDateTime.now());
            fileDocumentRepository.save(fileDocument);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(FileDocumentEnum.FILE_UPLOAD_FAILED);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(FileDocumentEnum.FILE_SIZE_EXCEEDED);
        }

        String fileName = file.getOriginalFilename();
        if (!StringUtils.hasText(fileName)) {
            throw new BusinessException(FileDocumentEnum.FILE_UPLOAD_FAILED);
        }

        String extension = FileUtil.getFileExtension(fileName);
        if (!FileTypeEnum.isValidExtension(extension)) {
            throw new BusinessException(FileDocumentEnum.FILE_TYPE_NOT_SUPPORTED);
        }
    }

    private Query buildQuery(FileQueryDTO query) {
        Query mongoQuery = new Query();

        if (query.getSessionId() != null) {
            mongoQuery.addCriteria(Criteria.where(FileDocumentConstants.FIELD_SESSION_ID).is(query.getSessionId()));
        }
        if (query.getCourseId() != null) {
            mongoQuery.addCriteria(Criteria.where(FileDocumentConstants.FIELD_COURSE_ID).is(query.getCourseId()));
        }
        if (query.getUserId() != null) {
            mongoQuery.addCriteria(Criteria.where(FileDocumentConstants.FIELD_SYS_USER_ID).is(query.getUserId()));
        }
        if (query.getFileTypes() != null && !query.getFileTypes().isEmpty()) {
            mongoQuery.addCriteria(Criteria.where(FileDocumentConstants.FIELD_FILE_TYPE).in(query.getFileTypes()));
        }
        if (query.getStatuses() != null && !query.getStatuses().isEmpty()) {
            mongoQuery.addCriteria(Criteria.where(FileDocumentConstants.FIELD_STATUS).in(query.getStatuses()));
        } else {
            // 默认不包含已删除的文件
            mongoQuery.addCriteria(Criteria.where(FileDocumentConstants.FIELD_STATUS).ne(FileStatusEnum.DELETED.getCode()));
        }
        if (query.getIsVectorized() != null) {
            mongoQuery.addCriteria(Criteria.where(FileDocumentConstants.FIELD_IS_VECTORIZED).is(query.getIsVectorized()));
        }
        if (StringUtils.hasText(query.getFileName())) {
            mongoQuery.addCriteria(Criteria.where(FileDocumentConstants.FIELD_FILE_NAME).regex(query.getFileName(), FileDocumentConstants.REGEX_CASE_INSENSITIVE));
        }

        return mongoQuery;
    }

    /**
     * 批量转换PO为VO，避免潜在的N+1问题
     *
     * @param fileDocuments 文件文档PO列表
     * @return 文件文档VO列表
     */
    private List<FileDocumentVO> convertToVOList(List<FileDocument> fileDocuments) {
        if (fileDocuments == null || fileDocuments.isEmpty()) {
            return new ArrayList<>();
        }
        return fileDocuments.stream()
                .map(this::convertToVO)
                .toList();
    }

    /**
     * 转换PO为VO
     *
     * @param fileDocument 文件文档PO
     * @return 文件文档VO
     */
    private FileDocumentVO convertToVO(FileDocument fileDocument) {
        if (fileDocument == null) {
            return null;
        }
        FileDocumentVO vo = new FileDocumentVO();
        vo.setId(fileDocument.getId());
        vo.setFileName(fileDocument.getFileName());
        vo.setFileType(fileDocument.getFileType());
        FileTypeEnum fileTypeEnum = FileTypeEnum.fromCode(fileDocument.getFileType());
        vo.setFileTypeName(fileTypeEnum != null ? fileTypeEnum.getMessage() : "");
        vo.setFileSize(fileDocument.getFileSize());
        vo.setFileSizeFormatted(formatFileSize(fileDocument.getFileSize()));
        vo.setMimeType(fileDocument.getMimeType());
        vo.setStoragePath(fileDocument.getStoragePath());
        vo.setSysUserId(fileDocument.getSysUserId());
        vo.setCourseId(fileDocument.getCourseId());
        vo.setSessionId(fileDocument.getSessionId());
        vo.setStatus(fileDocument.getStatus());
        FileStatusEnum statusEnum = FileStatusEnum.fromCode(fileDocument.getStatus());
        vo.setStatusName(statusEnum != null ? statusEnum.getMessage() : "");
        vo.setIsVectorized(fileDocument.getIsVectorized());
        vo.setVectorCount(fileDocument.getVectorCount());
        vo.setCreateTime(fileDocument.getCreateTime());
        vo.setUpdateTime(fileDocument.getUpdateTime());
        return vo;
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + FileDocumentConstants.SIZE_UNIT_BYTE;
        } else if (size < 1024 * 1024) {
            return SIZE_FORMAT.format(size / 1024.0) + FileDocumentConstants.SIZE_UNIT_KB;
        } else if (size < 1024 * 1024 * 1024) {
            return SIZE_FORMAT.format(size / (1024.0 * 1024.0)) + FileDocumentConstants.SIZE_UNIT_MB;
        } else {
            return SIZE_FORMAT.format(size / (1024.0 * 1024.0 * 1024.0)) + FileDocumentConstants.SIZE_UNIT_GB;
        }
    }

    private FileInfo convertToFileInfo(FileDocument fileDocument) {
        if (fileDocument == null) {
            return null;
        }
        String storagePath = fileDocument.getStoragePath();
        FileInfo.FileInfoBuilder builder = FileInfo.builder()
                .objectName(storagePath)
                .fileName(fileDocument.getFileName())
                .size(fileDocument.getFileSize())
                .contentType(fileDocument.getMimeType())
                .lastModified(fileDocument.getUpdateTime() != null ? fileDocument.getUpdateTime() : fileDocument.getCreateTime())
                .isDir(Boolean.FALSE)
                .extension(FileUtil.getFileExtension(fileDocument.getFileName()))
                .path(extractPath(storagePath))
                .bucketCode(fileDocument.getBucketCode())
                .error(Boolean.FALSE);

        BusinessBucketEnum bucketEnum = BusinessBucketEnum.fromBucketCode(fileDocument.getBucketCode());
        if (bucketEnum != null) {
            builder.bucketName(bucketEnum.getDefaultBucketName());
        }

        builder.url(resolveFileUrl(fileDocument));
        return builder.build();
    }

    private String resolveFileUrl(FileDocument fileDocument) {
        if (fileDocument == null) {
            return null;
        }
        try {
            Result<String> urlResult = minIOClient.getFileUrl(
                    fileDocument.getStoragePath(),
                    null,
                    fileDocument.getBucketCode()
            );
            if (urlResult != null && urlResult.isSuccess()) {
                return urlResult.getData();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch file url from MinIO, fileId: {}, error: {}", fileDocument.getId(), e.getMessage());
        }
        return null;
    }

    private String extractPath(String storagePath) {
        if (!StringUtils.hasText(storagePath)) {
            return "";
        }
        int lastSeparator = storagePath.lastIndexOf(FileDocumentConstants.PATH_SEPARATOR);
        if (lastSeparator < 0) {
            return "";
        }
        return storagePath.substring(0, lastSeparator + 1);
    }
}
