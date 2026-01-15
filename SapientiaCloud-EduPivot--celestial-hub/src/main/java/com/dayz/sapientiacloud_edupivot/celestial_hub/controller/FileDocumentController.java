package com.dayz.sapientiacloud_edupivot.celestial_hub.controller;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileQueryDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileUploadDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.FileInfo;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.FileDocumentVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.IFileDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "文件文档管理", description = "文件上传、下载、查询接口")
@RestController
@RequestMapping("/file-document")
@RequiredArgsConstructor
public class FileDocumentController extends BaseController {

    private final IFileDocumentService fileDocumentService;

    @Operation(summary = "uploadFile", description = "上传单个文件")
    @PostMapping("/upload")
    public Result<FileDocumentVO> uploadFile(
            @Parameter(description = "上传的文件", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "课程ID（可选）") @RequestParam(value = "courseId", required = false) UUID courseId,
            @Parameter(description = "会话ID（可选）") @RequestParam(value = "sessionId", required = false) UUID sessionId,
            @Parameter(description = "是否自动向量化") @RequestParam(value = "autoVectorize", required = false, defaultValue = "false") Boolean autoVectorize
    ) {
        FileUploadDTO request = new FileUploadDTO();
        request.setCourseId(courseId);
        request.setSessionId(sessionId);
        request.setAutoVectorize(autoVectorize);
        FileDocumentVO vo = fileDocumentService.uploadFile(file, request);
        return Result.success(vo);
    }

    @Operation(summary = "uploadFiles", description = "批量上传多个文件")
    @PostMapping("/upload/batch")
    public Result<List<FileDocumentVO>> uploadFiles(
            @Parameter(description = "上传的文件列表", required = true) @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "课程ID（可选）") @RequestParam(value = "courseId", required = false) UUID courseId,
            @Parameter(description = "会话ID（可选）") @RequestParam(value = "sessionId", required = false) UUID sessionId,
            @Parameter(description = "是否自动向量化") @RequestParam(value = "autoVectorize", required = false, defaultValue = "false") Boolean autoVectorize
    ) {
        FileUploadDTO request = new FileUploadDTO();
        request.setCourseId(courseId);
        request.setSessionId(sessionId);
        request.setAutoVectorize(autoVectorize);
        List<FileDocumentVO> vos = fileDocumentService.uploadFiles(files, request);
        return Result.success(vos);
    }

    @Operation(summary = "getFileById", description = "根据文件ID获取文件信息")
    @GetMapping("/{id}")
    public Result<FileDocumentVO> getFileById(
            @Parameter(description = "文件ID", required = true) @PathVariable("id") UUID id
    ) {
        FileDocumentVO vo = fileDocumentService.getFileById(id);
        return Result.success(vo);
    }

    @Operation(summary = "deleteFile", description = "根据文件ID删除文件")
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteFile(
            @Parameter(description = "文件ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = fileDocumentService.deleteFile(id);
        return Result.success(result);
    }

    @Operation(summary = "listFiles", description = "分页查询文件列表")
    @GetMapping("/list")
    public Result<Page<FileDocumentVO>> listFiles(
            @Parameter(description = "会话ID") @RequestParam(value = "sessionId", required = false) UUID sessionId,
            @Parameter(description = "课程ID") @RequestParam(value = "courseId", required = false) UUID courseId,
            @Parameter(description = "用户ID") @RequestParam(value = "userId", required = false) UUID userId,
            @Parameter(description = "是否已向量化") @RequestParam(value = "isVectorized", required = false) Boolean isVectorized,
            @Parameter(description = "文件名（模糊查询）") @RequestParam(value = "fileName", required = false) String fileName,
            @Parameter(description = "页码", required = false) @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", required = false) @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize
    ) {
        FileQueryDTO query = new FileQueryDTO();
        query.setSessionId(sessionId);
        query.setCourseId(courseId);
        query.setUserId(userId);
        query.setIsVectorized(isVectorized);
        query.setFileName(fileName);

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Page<FileDocumentVO> page = fileDocumentService.listFiles(query, pageable);
        return Result.success(page);
    }

    @Operation(summary = "vectorizeFile", description = "手动触发文件向量化")
    @PostMapping("/{id}/vectorize")
    public Result<Boolean> vectorizeFile(
            @Parameter(description = "文件ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = fileDocumentService.vectorizeFile(id);
        return Result.success(result);
    }

    @Operation(summary = "getFilesBySessionId", description = "根据会话ID获取文件信息列表")
    @GetMapping("/session/{sessionId}/files")
    public Result<List<FileInfo>> getFilesBySessionId(
            @Parameter(description = "会话ID", required = true) @PathVariable("sessionId") UUID sessionId
    ) {
        List<FileInfo> files = fileDocumentService.getFileInfosBySessionId(sessionId);
        return Result.success(files);
    }
}

