package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileQueryDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.FileUploadDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.po.FileDocument;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.FileDocumentVO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface IFileDocumentService {

    /**
     * 上传文件
     *
     * @param file    文件
     * @param request 上传请求
     * @return 文件文档VO
     */
    FileDocumentVO uploadFile(MultipartFile file, FileUploadDTO request);

    /**
     * 批量上传文件
     *
     * @param files   文件列表
     * @param request 上传请求
     * @return 文件文档VO列表
     */
    List<FileDocumentVO> uploadFiles(List<MultipartFile> files, FileUploadDTO request);

    /**
     * 根据ID获取文件
     *
     * @param id 文件ID
     * @return 文件文档VO
     */
    FileDocumentVO getFileById(UUID id);

    /**
     * 根据ID获取文件实体
     *
     * @param id 文件ID
     * @return 文件文档实体
     */
    FileDocument getFileDocumentById(UUID id);

    /**
     * 下载文件
     *
     * @param id 文件ID
     * @return 文件输入流
     */
    InputStream downloadFile(UUID id);

    /**
     * 删除文件
     *
     * @param id 文件ID
     * @return 是否成功
     */
    Boolean deleteFile(UUID id);

    /**
     * 查询文件列表
     *
     * @param query 查询条件
     * @param page  分页参数
     * @return 分页结果
     */
    Page<FileDocumentVO> listFiles(FileQueryDTO query, org.springframework.data.domain.Pageable page);

    /**
     * 向量化文件
     *
     * @param id 文件ID
     * @return 是否成功
     */
    Boolean vectorizeFile(UUID id);

    /**
     * 根据文件ID列表获取文件
     *
     * @param ids 文件ID列表
     * @return 文件列表
     */
    List<FileDocument> getFilesByIds(List<UUID> ids);
}

