package com.dayz.sapientiacloud_edupivot.celestial_hub.service.impl;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.FileParserConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.FileDocumentEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.FileTypeEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.IFileParserService;
import com.dayz.sapientiacloud_edupivot.celestial_hub.utils.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class FileParserServiceImpl implements IFileParserService {

    @Override
    public String parseFile(InputStream inputStream, String fileName, String mimeType) {
        if (inputStream == null) {
            throw new BusinessException(FileDocumentEnum.FILE_PARSE_FAILED);
        }

        try {
            FileTypeEnum fileType = FileTypeEnum.fromMimeType(mimeType);
            if (fileType == null) {
                fileType = FileTypeEnum.fromExtension(FileUtil.getFileExtension(fileName));
            }

            if (fileType == null) {
                throw new BusinessException(FileDocumentEnum.FILE_TYPE_NOT_SUPPORTED);
            }

            return switch (fileType) {
                case PDF -> parsePdf(inputStream);
                case DOC, DOCX -> parseWord(inputStream, fileName);
                case XLS, XLSX -> parseExcel(inputStream, fileName);
                case TXT, MD, RTF -> parseText(inputStream);
            };
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse file: fileName={}, mimeType={}, error={}", fileName, mimeType, e.getMessage(), e);
            throw new BusinessException(FileDocumentEnum.FILE_PARSE_FAILED);
        }
    }

    @Override
    public String parsePdf(InputStream inputStream) {
        try {
            byte[] pdfBytes = IOUtils.toByteArray(inputStream);
            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(1);
                stripper.setEndPage(document.getNumberOfPages());
                return stripper.getText(document);
            }
        } catch (IOException e) {
            log.error("Failed to parse PDF: {}", e.getMessage(), e);
            throw new BusinessException(FileDocumentEnum.FILE_PARSE_FAILED);
        }
    }

    @Override
    public String parseWord(InputStream inputStream, String fileName) {
        try {
            String extension = FileUtil.getFileExtension(fileName).toLowerCase();
            StringBuilder text = new StringBuilder();

            if (FileTypeEnum.DOCX.getExtension().equals(extension)) {
                try (XWPFDocument document = new XWPFDocument(inputStream)) {
                    for (XWPFParagraph paragraph : document.getParagraphs()) {
                        String paraText = paragraph.getText();
                        if (paraText != null && !paraText.trim().isEmpty()) {
                            text.append(paraText).append(FileParserConstants.LINE_SEPARATOR);
                        }
                    }
                }
            } else if (FileTypeEnum.DOC.getExtension().equals(extension)) {
                // .doc格式需要使用HWPF，但POI对.doc支持有限，建议用户使用.docx
                throw new BusinessException(FileDocumentEnum.FILE_TYPE_NOT_SUPPORTED);
            }

            return text.toString().trim();
        } catch (IOException e) {
            log.error("Failed to parse Word document: {}", e.getMessage(), e);
            throw new BusinessException(FileDocumentEnum.FILE_PARSE_FAILED);
        }
    }

    @Override
    public String parseExcel(InputStream inputStream, String fileName) {
        try {
            String extension = FileUtil.getFileExtension(fileName).toLowerCase();
            StringBuilder text = new StringBuilder();
            Workbook workbook;

            if (FileTypeEnum.XLSX.getExtension().equals(extension)) {
                workbook = new XSSFWorkbook(inputStream);
            } else if (FileTypeEnum.XLS.getExtension().equals(extension)) {
                workbook = new HSSFWorkbook(inputStream);
            } else {
                throw new BusinessException(FileDocumentEnum.FILE_TYPE_NOT_SUPPORTED);
            }

            try {
                int sheetCount = workbook.getNumberOfSheets();
                for (int i = 0; i < sheetCount; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    if (sheet != null) {
                        text.append(FileParserConstants.EXCEL_SHEET_PREFIX)
                                .append(sheet.getSheetName())
                                .append(FileParserConstants.LINE_SEPARATOR);
                        for (Row row : sheet) {
                            StringBuilder rowText = new StringBuilder();
                            for (Cell cell : row) {
                                String cellValue = getCellValue(cell);
                                if (cellValue != null && !cellValue.trim().isEmpty()) {
                                    rowText.append(cellValue).append(FileParserConstants.EXCEL_CELL_SEPARATOR);
                                }
                            }
                            if (!rowText.isEmpty()) {
                                text.append(rowText.toString().trim()).append(FileParserConstants.LINE_SEPARATOR);
                            }
                        }
                        text.append(FileParserConstants.LINE_SEPARATOR);
                    }
                }
            } finally {
                workbook.close();
            }

            return text.toString().trim();
        } catch (IOException e) {
            log.error("Failed to parse Excel file: {}", e.getMessage(), e);
            throw new BusinessException(FileDocumentEnum.FILE_PARSE_FAILED);
        }
    }

    @Override
    public String parseText(InputStream inputStream) {
        try {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to parse text file: {}", e.getMessage(), e);
            throw new BusinessException(FileDocumentEnum.FILE_PARSE_FAILED);
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return FileParserConstants.EMPTY_STRING;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> FileParserConstants.EMPTY_STRING;
        };
    }
}

