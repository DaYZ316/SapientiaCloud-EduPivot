package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import java.io.InputStream;

public interface IFileParserService {

    String parseFile(InputStream inputStream, String fileName, String mimeType);

    String parsePdf(InputStream inputStream);

    String parseWord(InputStream inputStream, String fileName);

    String parseExcel(InputStream inputStream, String fileName);

    String parseText(InputStream inputStream);
}

