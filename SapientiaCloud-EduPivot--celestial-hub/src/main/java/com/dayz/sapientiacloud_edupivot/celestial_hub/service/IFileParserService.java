package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import java.io.InputStream;

public interface IFileParserService {

    /**
     * 解析文件内容为文本
     *
     * @param inputStream 文件输入流
     * @param fileName    文件名
     * @param mimeType    MIME类型
     * @return 解析后的文本内容
     */
    String parseFile(InputStream inputStream, String fileName, String mimeType);

    /**
     * 解析PDF文件
     *
     * @param inputStream PDF输入流
     * @return 解析后的文本内容
     */
    String parsePdf(InputStream inputStream);

    /**
     * 解析Word文档（.doc, .docx）
     *
     * @param inputStream Word输入流
     * @param fileName    文件名
     * @return 解析后的文本内容
     */
    String parseWord(InputStream inputStream, String fileName);

    /**
     * 解析Excel文件（.xls, .xlsx）
     *
     * @param inputStream Excel输入流
     * @param fileName    文件名
     * @return 解析后的文本内容
     */
    String parseExcel(InputStream inputStream, String fileName);

    /**
     * 解析文本文件（.txt, .md等）
     *
     * @param inputStream 文本输入流
     * @return 解析后的文本内容
     */
    String parseText(InputStream inputStream);
}

