package com.dayz.sapientiacloud_edupivot.celestial_hub.service;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionAnswerSimpleDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionOptionSimpleDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionPaperExportRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class QuestionPaperExportService {

    private static final String DEFAULT_PAPER_NAME = "AI生成试卷";
    private static final String WORD_MIME_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String DEFAULT_WORD_FONT = "Microsoft YaHei";
    private static final String ANSWER_SECTION_TITLE = "参考答案与解析";
    private static final List<Integer> QUESTION_TYPE_EXPORT_ORDER = List.of(0, 1, 2, 3, 4);
    private static final List<String> FONT_CANDIDATES = List.of(
            "src/main/resources/fonts/NotoSansCJKsc-Regular.otf",
            "src/main/resources/fonts/SourceHanSansSC-Regular.otf",
            "C:/Windows/Fonts/simhei.ttf",
            "C:/Windows/Fonts/simkai.ttf",
            "C:/Windows/Fonts/msyh.ttc",
            "C:/Windows/Fonts/simsun.ttc",
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
            "/usr/share/fonts/opentype/noto/NotoSansCJKSC-Regular.otf",
            "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
    );

    public ExportedPaperFile exportPdf(QuestionPaperExportRequestDTO request) {
        ExportContext context = normalizeRequest(request);

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDFont font = loadPdfFont(document);
            try (PdfPaperWriter writer = new PdfPaperWriter(document, font)) {
                writer.writeCentered(context.paperName(), 18F);
                writer.addSpacer(12F);
                writer.writeParagraph(buildSummaryLine(context), 11F);
                writer.addSpacer(10F);

                for (int sectionIndex = 0; sectionIndex < context.sections().size(); sectionIndex++) {
                    ExportSection section = context.sections().get(sectionIndex);
                    writer.writeParagraph(section.title(), 14F);
                    writer.addSpacer(6F);

                    for (int questionIndex = 0; questionIndex < section.questions().size(); questionIndex++) {
                        QuestionResponseDTO question = section.questions().get(questionIndex);
                        writeQuestionToPdf(writer, questionIndex + 1, question, context.includeAnswers());
                        if (questionIndex < section.questions().size() - 1) {
                            writer.addSpacer(12F);
                        }
                    }

                    if (sectionIndex < context.sections().size() - 1) {
                        writer.addSpacer(16F);
                    }
                }
            }

            document.save(outputStream);
            return new ExportedPaperFile(
                    buildFileName(context.paperName(), "pdf"),
                    "application/pdf",
                    outputStream.toByteArray()
            );
        } catch (IOException e) {
            log.error("Failed to export paper as PDF. paperName={}", context.paperName(), e);
            throw new BusinessException(ResultEnum.FAIL.getCode(), "导出PDF失败");
        }
    }

    public ExportedPaperFile exportWord(QuestionPaperExportRequestDTO request) {
        ExportContext context = normalizeRequest(request);

        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            writePaperToWord(document, context);
            document.write(outputStream);
            return new ExportedPaperFile(
                    buildFileName(context.paperName(), "docx"),
                    WORD_MIME_TYPE,
                    outputStream.toByteArray()
            );
        } catch (IOException e) {
            log.error("Failed to export paper as Word. paperName={}", context.paperName(), e);
            throw new BusinessException(ResultEnum.FAIL.getCode(), "导出Word失败");
        }
    }

    private ExportContext normalizeRequest(QuestionPaperExportRequestDTO request) {
        if (request == null || CollectionUtils.isEmpty(request.getQuestions())) {
            throw new BusinessException(ResultEnum.PARAM_ERROR.getCode(), "试卷题目不能为空");
        }

        List<QuestionResponseDTO> questions = request.getQuestions().stream()
                .filter(Objects::nonNull)
                .toList();
        if (questions.isEmpty()) {
            throw new BusinessException(ResultEnum.PARAM_ERROR.getCode(), "试卷题目不能为空");
        }

        String paperName = StringUtils.hasText(request.getPaperName())
                ? request.getPaperName().trim()
                : DEFAULT_PAPER_NAME;
        boolean includeAnswers = Boolean.TRUE.equals(request.getIncludeAnswers());
        List<ExportSection> sections = buildSections(questions);

        return new ExportContext(paperName, questions, sections, includeAnswers);
    }

    private List<ExportSection> buildSections(List<QuestionResponseDTO> questions) {
        Map<Integer, List<QuestionResponseDTO>> grouped = new LinkedHashMap<>();
        for (Integer questionType : QUESTION_TYPE_EXPORT_ORDER) {
            grouped.put(questionType, new ArrayList<>());
        }
        grouped.put(-1, new ArrayList<>());

        for (QuestionResponseDTO question : questions) {
            Integer questionType = question.getQuestionType();
            List<QuestionResponseDTO> sectionQuestions = grouped.get(questionType);
            if (sectionQuestions == null) {
                grouped.get(-1).add(question);
            } else {
                sectionQuestions.add(question);
            }
        }

        List<ExportSection> sections = new ArrayList<>();
        int sectionOrder = 1;
        for (Map.Entry<Integer, List<QuestionResponseDTO>> entry : grouped.entrySet()) {
            List<QuestionResponseDTO> sectionQuestions = entry.getValue();
            if (sectionQuestions.isEmpty()) {
                continue;
            }

            sections.add(new ExportSection(
                    entry.getKey(),
                    buildSectionTitle(sectionOrder++, entry.getKey(), sectionQuestions),
                    sectionQuestions
            ));
        }
        return sections;
    }

    private String buildSectionTitle(int sectionOrder,
                                     Integer questionType,
                                     List<QuestionResponseDTO> questions) {
        return "第" + sectionOrder + "部分 " + getQuestionTypeLabel(questionType)
                + "（共" + questions.size() + "题，"
                + formatScore(sumScore(questions)) + "分）";
    }

    private void writePaperToWord(XWPFDocument document, ExportContext context) {
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setBold(true);
        titleRun.setFontFamily(DEFAULT_WORD_FONT);
        titleRun.setFontSize(18);
        titleRun.setText(context.paperName());

        XWPFParagraph summaryParagraph = document.createParagraph();
        XWPFRun summaryRun = summaryParagraph.createRun();
        summaryRun.setFontFamily(DEFAULT_WORD_FONT);
        summaryRun.setFontSize(11);
        summaryRun.setText(buildSummaryLine(context));

        for (int sectionIndex = 0; sectionIndex < context.sections().size(); sectionIndex++) {
            ExportSection section = context.sections().get(sectionIndex);
            addWordSectionTitle(document, section.title());

            for (int questionIndex = 0; questionIndex < section.questions().size(); questionIndex++) {
                QuestionResponseDTO question = section.questions().get(questionIndex);

                XWPFParagraph headingParagraph = document.createParagraph();
                XWPFRun headingRun = headingParagraph.createRun();
                headingRun.setBold(true);
                headingRun.setFontFamily(DEFAULT_WORD_FONT);
                headingRun.setFontSize(13);
                headingRun.setText(formatQuestionHeading(questionIndex + 1, question));

                addPlainTextParagraph(document, question.getQuestionContent(), 12);

                if (!CollectionUtils.isEmpty(question.getOptions())) {
                    for (int optionIndex = 0; optionIndex < question.getOptions().size(); optionIndex++) {
                        QuestionOptionSimpleDTO option = question.getOptions().get(optionIndex);
                        if (option == null) {
                            continue;
                        }
                        String optionLine = getOptionLabel(option, optionIndex) + ". "
                                + toPlainText(option.getOptionContent());
                        addPlainTextParagraph(document, optionLine, 12);
                    }
                }

                if (context.includeAnswers()) {
                    writeAnswerSectionToWord(document, question);
                }
            }

            if (sectionIndex < context.sections().size() - 1) {
                document.createParagraph();
            }
        }
    }

    private void addWordSectionTitle(XWPFDocument document, String title) {
        XWPFParagraph sectionParagraph = document.createParagraph();
        XWPFRun sectionRun = sectionParagraph.createRun();
        sectionRun.setBold(true);
        sectionRun.setFontFamily(DEFAULT_WORD_FONT);
        sectionRun.setFontSize(15);
        sectionRun.setText(title);
    }

    private void writeAnswerSectionToWord(XWPFDocument document, QuestionResponseDTO question) {
        List<String> answers = buildAnswerSections(question);
        if (answers.isEmpty()) {
            return;
        }

        XWPFParagraph answerTitleParagraph = document.createParagraph();
        XWPFRun answerTitleRun = answerTitleParagraph.createRun();
        answerTitleRun.setBold(true);
        answerTitleRun.setFontFamily(DEFAULT_WORD_FONT);
        answerTitleRun.setFontSize(12);
        answerTitleRun.setText(ANSWER_SECTION_TITLE);

        for (String line : answers) {
            addPlainTextParagraph(document, line, 11);
        }
    }

    private void writeQuestionToPdf(PdfPaperWriter writer,
                                    int order,
                                    QuestionResponseDTO question,
                                    boolean includeAnswers) throws IOException {
        writer.writeParagraph(formatQuestionHeading(order, question), 13F);
        writer.addSpacer(4F);
        writer.writeParagraph(toPlainText(question.getQuestionContent()), 12F);

        if (!CollectionUtils.isEmpty(question.getOptions())) {
            writer.addSpacer(6F);
            for (int optionIndex = 0; optionIndex < question.getOptions().size(); optionIndex++) {
                QuestionOptionSimpleDTO option = question.getOptions().get(optionIndex);
                if (option == null) {
                    continue;
                }
                String optionLine = getOptionLabel(option, optionIndex) + ". "
                        + toPlainText(option.getOptionContent());
                writer.writeParagraph(optionLine, 12F);
            }
        }

        if (!includeAnswers) {
            return;
        }

        List<String> answers = buildAnswerSections(question);
        if (answers.isEmpty()) {
            return;
        }

        writer.addSpacer(6F);
        writer.writeParagraph(ANSWER_SECTION_TITLE, 12F);
        for (String line : answers) {
            writer.writeParagraph(line, 11F);
        }
    }

    private void addPlainTextParagraph(XWPFDocument document, String content, int fontSize) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setFontFamily(DEFAULT_WORD_FONT);
        run.setFontSize(fontSize);
        String[] lines = toPlainText(content).split("\\R");
        for (int i = 0; i < lines.length; i++) {
            run.setText(lines[i]);
            if (i < lines.length - 1) {
                run.addBreak();
            }
        }
    }

    private PDFont loadPdfFont(PDDocument document) throws IOException {
        for (String candidate : FONT_CANDIDATES) {
            Path path = Path.of(candidate);
            if (!Files.isRegularFile(path)) {
                continue;
            }
            try (InputStream inputStream = Files.newInputStream(path)) {
                log.debug("Loading PDF export font from {}", candidate);
                return PDType0Font.load(document, inputStream);
            } catch (Exception e) {
                log.debug("Failed to load PDF export font from {}", candidate, e);
            }
        }

        log.warn("No CJK font found for PDF export. Falling back to Helvetica, which may not render Chinese correctly.");
        return new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    }

    private String buildSummaryLine(ExportContext context) {
        return "题目数：" + context.questions().size()
                + "    总分：" + formatScore(sumScore(context.questions()))
                + "    预计时长：" + sumEstimatedTime(context.questions()) + "分钟"
                + "    导出版本：" + (context.includeAnswers() ? "答案版" : "试题版");
    }

    private BigDecimal sumScore(List<QuestionResponseDTO> questions) {
        BigDecimal total = BigDecimal.ZERO;
        for (QuestionResponseDTO question : questions) {
            if (question != null && question.getScore() != null) {
                total = total.add(question.getScore());
            }
        }
        return total;
    }

    private int sumEstimatedTime(List<QuestionResponseDTO> questions) {
        int total = 0;
        for (QuestionResponseDTO question : questions) {
            if (question != null && question.getEstimatedTime() != null) {
                total += question.getEstimatedTime();
            }
        }
        return total;
    }

    private String formatQuestionHeading(int order, QuestionResponseDTO question) {
        String title = resolveQuestionTitle(question);
        String difficulty = getDifficultyLabel(question.getDifficulty());
        return order + ". " + title
                + "（难度：" + difficulty
                + "，分值：" + formatScore(question.getScore())
                + "，预计：" + safeEstimatedTime(question.getEstimatedTime()) + "分钟）";
    }

    private String resolveQuestionTitle(QuestionResponseDTO question) {
        if (StringUtils.hasText(question.getQuestionTitle())) {
            return toPlainText(question.getQuestionTitle());
        }
        if (StringUtils.hasText(question.getQuestionContent())) {
            String plainContent = toPlainText(question.getQuestionContent());
            String[] lines = plainContent.split("\\R");
            if (lines.length > 0 && StringUtils.hasText(lines[0])) {
                return lines[0].length() > 40 ? lines[0].substring(0, 40) + "..." : lines[0];
            }
        }
        return "未命名题目";
    }

    private List<String> buildAnswerSections(QuestionResponseDTO question) {
        List<String> lines = new ArrayList<>();

        if (!CollectionUtils.isEmpty(question.getOptions())) {
            List<String> correctLabels = new ArrayList<>();
            for (int i = 0; i < question.getOptions().size(); i++) {
                QuestionOptionSimpleDTO option = question.getOptions().get(i);
                if (option == null || option.getIsCorrect() == null || option.getIsCorrect() != 1) {
                    continue;
                }
                String label = getOptionLabel(option, i);
                correctLabels.add(label);
                if (StringUtils.hasText(option.getExplanation())) {
                    lines.add(label + " 解析：" + toPlainText(option.getExplanation()));
                }
            }
            if (!correctLabels.isEmpty()) {
                lines.add(0, "正确答案：" + String.join(", ", correctLabels));
            }
        }

        if (!CollectionUtils.isEmpty(question.getAnswers())) {
            List<QuestionAnswerSimpleDTO> answers = question.getAnswers().stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(
                            QuestionAnswerSimpleDTO::getSortOrder,
                            Comparator.nullsLast(Integer::compareTo)
                    ))
                    .toList();

            for (int i = 0; i < answers.size(); i++) {
                QuestionAnswerSimpleDTO answer = answers.get(i);
                String prefix = answers.size() > 1 ? "答案" + (i + 1) + "：" : "答案：";
                lines.add(prefix + toPlainText(answer.getAnswerContent()));
                if (StringUtils.hasText(answer.getExplanation())) {
                    lines.add("解析：" + toPlainText(answer.getExplanation()));
                }
            }
        }

        return lines;
    }

    private String getQuestionTypeLabel(Integer questionType) {
        if (questionType == null) {
            return "题目";
        }
        return switch (questionType) {
            case 0 -> "单选题";
            case 1 -> "多选题";
            case 2 -> "判断题";
            case 3 -> "填空题";
            case 4 -> "简答题";
            default -> "其他题型";
        };
    }

    private String getDifficultyLabel(Integer difficulty) {
        if (difficulty == null) {
            return "未知";
        }
        return switch (difficulty) {
            case 1 -> "简单";
            case 2 -> "中等";
            case 3 -> "困难";
            default -> "未知";
        };
    }

    private String formatScore(BigDecimal score) {
        if (score == null) {
            return "0";
        }
        return score.stripTrailingZeros().toPlainString();
    }

    private int safeEstimatedTime(Integer estimatedTime) {
        return estimatedTime == null ? 0 : Math.max(estimatedTime, 0);
    }

    private String getOptionLabel(QuestionOptionSimpleDTO option, int optionIndex) {
        if (option != null && StringUtils.hasText(option.getOptionLabel())) {
            return option.getOptionLabel().trim();
        }
        return String.valueOf((char) ('A' + optionIndex));
    }

    private String buildFileName(String paperName, String extension) {
        String safeName = sanitizeFileName(paperName);
        if (!StringUtils.hasText(safeName)) {
            safeName = DEFAULT_PAPER_NAME;
        }
        return safeName + "." + extension;
    }

    private String sanitizeFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return DEFAULT_PAPER_NAME;
        }
        return fileName.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String toPlainText(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }

        return content
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replace("\t", "    ")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("<[^>]+>", "")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("**", "")
                .replace("__", "")
                .replace("`", "")
                .replaceAll("(?m)^#{1,6}\\s*", "")
                .replaceAll("!\\[([^\\]]*)]\\(([^)]+)\\)", "$1 $2")
                .replaceAll("\\[([^\\]]+)]\\(([^)]+)\\)", "$1 $2")
                .replaceAll("(?m)^>\\s?", "")
                .replaceAll("(?m)^[-*+]\\s+", "- ")
                .replaceAll("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F]", "")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    public record ExportedPaperFile(String fileName, String contentType, byte[] content) {
    }

    private record ExportContext(String paperName,
                                 List<QuestionResponseDTO> questions,
                                 List<ExportSection> sections,
                                 boolean includeAnswers) {
    }

    private record ExportSection(Integer questionType,
                                 String title,
                                 List<QuestionResponseDTO> questions) {
    }

    private static final class PdfPaperWriter implements Closeable {

        private static final float MARGIN_LEFT = 56F;
        private static final float MARGIN_RIGHT = 56F;
        private static final float MARGIN_TOP = 60F;
        private static final float MARGIN_BOTTOM = 56F;
        private static final float LINE_GAP = 4F;

        private final PDDocument document;
        private final PDFont font;

        private PDPage currentPage;
        private PDPageContentStream contentStream;
        private float cursorY;

        private PdfPaperWriter(PDDocument document, PDFont font) throws IOException {
            this.document = document;
            this.font = font;
            startNewPage();
        }

        private void writeCentered(String text, float fontSize) throws IOException {
            for (String line : wrapText(text, fontSize)) {
                ensureSpace(fontSize + LINE_GAP);
                float width = textWidth(line, fontSize);
                float pageWidth = currentPage.getMediaBox().getWidth();
                float x = Math.max(MARGIN_LEFT, (pageWidth - width) / 2);
                writeLine(x, line, fontSize);
            }
        }

        private void writeParagraph(String text, float fontSize) throws IOException {
            if (!StringUtils.hasText(text)) {
                return;
            }
            String[] paragraphs = text.split("\\R", -1);
            for (String paragraph : paragraphs) {
                if (!StringUtils.hasText(paragraph)) {
                    addSpacer(fontSize * 0.5F);
                    continue;
                }
                for (String line : wrapText(paragraph, fontSize)) {
                    writeLine(MARGIN_LEFT, line, fontSize);
                }
            }
        }

        private void addSpacer(float space) throws IOException {
            ensureSpace(space);
            cursorY -= space;
        }

        private void writeLine(float x, String text, float fontSize) throws IOException {
            ensureSpace(fontSize + LINE_GAP);
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(x, cursorY);
            contentStream.showText(text == null ? "" : text);
            contentStream.endText();
            cursorY -= (fontSize + LINE_GAP);
        }

        private void ensureSpace(float requiredHeight) throws IOException {
            if (contentStream == null || cursorY - requiredHeight < MARGIN_BOTTOM) {
                startNewPage();
            }
        }

        private void startNewPage() throws IOException {
            closeCurrentStream();
            currentPage = new PDPage(PDRectangle.A4);
            document.addPage(currentPage);
            contentStream = new PDPageContentStream(document, currentPage);
            cursorY = currentPage.getMediaBox().getHeight() - MARGIN_TOP;
        }

        private float usableWidth() {
            return currentPage.getMediaBox().getWidth() - MARGIN_LEFT - MARGIN_RIGHT;
        }

        private float textWidth(String text, float fontSize) throws IOException {
            if (!StringUtils.hasText(text)) {
                return 0F;
            }
            return font.getStringWidth(text) / 1000F * fontSize;
        }

        private List<String> wrapText(String text, float fontSize) throws IOException {
            List<String> lines = new ArrayList<>();
            if (!StringUtils.hasText(text)) {
                lines.add("");
                return lines;
            }

            float maxWidth = usableWidth();
            StringBuilder currentLine = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char currentChar = text.charAt(i);
                String candidate = currentLine + String.valueOf(currentChar);
                if (currentLine.length() == 0 || textWidth(candidate, fontSize) <= maxWidth) {
                    currentLine.append(currentChar);
                    continue;
                }
                lines.add(currentLine.toString());
                currentLine.setLength(0);
                currentLine.append(currentChar);
            }
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
            return lines;
        }

        private void closeCurrentStream() throws IOException {
            if (contentStream != null) {
                contentStream.close();
                contentStream = null;
            }
        }

        @Override
        public void close() throws IOException {
            closeCurrentStream();
        }
    }
}
