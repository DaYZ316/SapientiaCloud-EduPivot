package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileQueryDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileUploadDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.FileDocument;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.FileInfo;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.FileDocumentVO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface IFileDocumentService {

    FileDocumentVO uploadFile(MultipartFile file, FileUploadDTO request);

    List<FileDocumentVO> uploadFiles(List<MultipartFile> files, FileUploadDTO request);

    FileDocumentVO getFileById(UUID id);

    FileDocument getFileDocumentById(UUID id);

    InputStream downloadFile(UUID id);

    Boolean deleteFile(UUID id);

    Page<FileDocumentVO> listFiles(FileQueryDTO query, org.springframework.data.domain.Pageable page);

    Boolean vectorizeFile(UUID id);

    List<FileDocument> getFilesByIds(List<UUID> ids);

    List<FileInfo> getFileInfosBySessionId(UUID sessionId);
}

