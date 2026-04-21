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
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.util.Units;

@Slf4j
@Service
public class QuestionPaperExportService {

    private static final String DEFAULT_PAPER_NAME = "AI生成试卷";
    private static final String WORD_MIME_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String DEFAULT_WORD_FONT = "Microsoft YaHei";
    private static final String ANSWER_SECTION_TITLE = "参考答案与解析";
    private static final List<Integer> QUESTION_TYPE_EXPORT_ORDER = List.of(0, 1, 2, 3, 4);
    private static final List<String> CLASSPATH_FONT_CANDIDATES = List.of(
            "fonts/NotoSansSC-VF.ttf"
    );
    private static final List<String> FILE_SYSTEM_FONT_CANDIDATES = List.of(
            "C:/Windows/Fonts/simhei.ttf",
            "C:/Windows/Fonts/simkai.ttf",
            "C:/Windows/Fonts/NotoSansSC-VF.ttf",
            "C:/Windows/Fonts/NotoSerifSC-VF.ttf",
            "C:/Windows/Fonts/msyh.ttc",
            "C:/Windows/Fonts/simsun.ttc",
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc",
            "/usr/share/fonts/opentype/noto/NotoSansCJKSC-Regular.otf",
            "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
    );
    private static final String PDF_FONT_VALIDATION_TEXT = "高试卷答案";
    private static final String FORMULA_PLACEHOLDER_PREFIX = "@@FORMULA_";
    private static final String FORMULA_PLACEHOLDER_SUFFIX = "@@";
    private static final Pattern FORMULA_PLACEHOLDER_PATTERN = Pattern.compile("@@FORMULA_\\d+@@");
    private static final List<String> EXPORT_NOISE_MARKERS = List.of(
            "现更正", "更正：", "更正:", "修正：", "修正:", "应重新计算", "最终采用", "最终确定",
            "调整问题", "回到三变量", "为避免与已有题重复", "为符合高难度设计", "为确保难度和新颖性",
            "经审慎考虑", "为创新", "为简化", "为匹配难度", "采用经典题", "采用标准题型", "节省时间",
            "标准答案应为", "原题要求", "但已有题", "为节省时间"
    );
    private static final Set<String> BARE_LATEX_ENVIRONMENTS = Set.of(
            "matrix", "pmatrix", "bmatrix", "Bmatrix", "vmatrix", "Vmatrix",
            "smallmatrix", "cases", "array", "aligned", "align", "align*", "gather", "gather*"
    );
    private static final float FORMULA_RENDER_SCALE = 2F;
    private static final float PDF_TEXT_ASCENT_RATIO = 0.82F;
    private static final float PDF_TEXT_DESCENT_RATIO = 0.22F;
    private static final float WORD_MAX_CONTENT_WIDTH_PT = 480F;

    private final Map<FormulaRenderKey, RenderedFormula> renderedFormulaCache = new ConcurrentHashMap<>();

    public ExportedPaperFile exportPdf(QuestionPaperExportRequestDTO request) {
        ExportContext context = normalizeRequest(request);

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDFont font = loadPdfFont(document);
            try (PdfPaperWriter writer = new PdfPaperWriter(document, font)) {
                writer.writeCentered(context.paperName(), 18F, true);
                writer.addSpacer(12F);
                writer.writeParagraph(buildSummaryLine(context), 11F);
                writer.addSpacer(10F);

                for (int sectionIndex = 0; sectionIndex < context.sections().size(); sectionIndex++) {
                    ExportSection section = context.sections().get(sectionIndex);
                    writer.writeParagraph(section.title(), 14F, true);
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
        } catch (Exception e) {
            log.error("Failed to export paper as PDF. paperName={}", context.paperName(), e);
            throw new BusinessException(ResultEnum.FAIL.getCode(), "PDF export failed");
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
        } catch (Exception e) {
            log.error("Failed to export paper as Word. paperName={}", context.paperName(), e);
            throw new BusinessException(ResultEnum.FAIL.getCode(), "Word export failed");
        }
    }

    private ExportContext normalizeRequest(QuestionPaperExportRequestDTO request) {
        if (request == null || CollectionUtils.isEmpty(request.getQuestions())) {
            throw new BusinessException(ResultEnum.PARAM_ERROR.getCode(), "试卷题目不能为空");
        }

        List<QuestionResponseDTO> questions = request.getQuestions().stream()
                .filter(Objects::nonNull)
                .map(this::sanitizeQuestionForExport)
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

    private QuestionResponseDTO sanitizeQuestionForExport(QuestionResponseDTO source) {
        QuestionResponseDTO sanitized = new QuestionResponseDTO();
        sanitized.setId(source.getId());
        sanitized.setSysUserId(source.getSysUserId());
        sanitized.setRequestId(source.getRequestId());
        sanitized.setQuestionTitle(sanitizeExportContent(source.getQuestionTitle()));
        sanitized.setQuestionContent(sanitizeExportContent(source.getQuestionContent()));
        sanitized.setQuestionType(source.getQuestionType());
        sanitized.setDifficulty(source.getDifficulty());
        sanitized.setScore(source.getScore());
        sanitized.setEstimatedTime(source.getEstimatedTime());
        sanitized.setTags(source.getTags() == null ? null : List.copyOf(source.getTags()));
        sanitized.setOptions(sanitizeOptionsForExport(source.getOptions()));
        sanitized.setAnswers(sanitizeAnswersForExport(source.getAnswers()));
        return sanitized;
    }

    private List<QuestionOptionSimpleDTO> sanitizeOptionsForExport(List<QuestionOptionSimpleDTO> options) {
        if (CollectionUtils.isEmpty(options)) {
            return options;
        }

        return options.stream()
                .filter(Objects::nonNull)
                .map(this::sanitizeOptionForExport)
                .toList();
    }

    private QuestionOptionSimpleDTO sanitizeOptionForExport(QuestionOptionSimpleDTO source) {
        QuestionOptionSimpleDTO sanitized = new QuestionOptionSimpleDTO();
        sanitized.setId(source.getId());
        sanitized.setQuestionId(source.getQuestionId());
        sanitized.setOptionContent(sanitizeExportContent(source.getOptionContent()));
        sanitized.setOptionLabel(source.getOptionLabel());
        sanitized.setIsCorrect(source.getIsCorrect());
        sanitized.setScore(source.getScore());
        sanitized.setImageUrls(source.getImageUrls() == null ? null : List.copyOf(source.getImageUrls()));
        sanitized.setExplanation(sanitizeExportContent(source.getExplanation()));
        return sanitized;
    }

    private List<QuestionAnswerSimpleDTO> sanitizeAnswersForExport(List<QuestionAnswerSimpleDTO> answers) {
        if (CollectionUtils.isEmpty(answers)) {
            return answers;
        }

        return answers.stream()
                .filter(Objects::nonNull)
                .map(this::sanitizeAnswerForExport)
                .toList();
    }

    private QuestionAnswerSimpleDTO sanitizeAnswerForExport(QuestionAnswerSimpleDTO source) {
        QuestionAnswerSimpleDTO sanitized = new QuestionAnswerSimpleDTO();
        sanitized.setId(source.getId());
        sanitized.setQuestionId(source.getQuestionId());
        sanitized.setAnswerContent(sanitizeExportContent(source.getAnswerContent()));
        sanitized.setExplanation(sanitizeExportContent(source.getExplanation()));
        sanitized.setScore(source.getScore());
        sanitized.setSortOrder(source.getSortOrder());
        return sanitized;
    }

    private String sanitizeExportContent(String content) {
        if (!StringUtils.hasText(content)) {
            return content;
        }

        String normalized = content
                .replace("\r\n", "\n")
                .replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);
        List<String> sanitizedLines = new ArrayList<>();
        boolean previousBlank = true;

        for (String line : lines) {
            String trimmed = line.trim();
            if (shouldDropExportLine(trimmed)) {
                continue;
            }

            if (trimmed.isEmpty()) {
                if (!previousBlank) {
                    sanitizedLines.add("");
                }
                previousBlank = true;
                continue;
            }

            sanitizedLines.add(line.stripTrailing());
            previousBlank = false;
        }

        while (!sanitizedLines.isEmpty() && sanitizedLines.get(0).isBlank()) {
            sanitizedLines.remove(0);
        }
        while (!sanitizedLines.isEmpty() && sanitizedLines.get(sanitizedLines.size() - 1).isBlank()) {
            sanitizedLines.remove(sanitizedLines.size() - 1);
        }

        return String.join("\n", sanitizedLines);
    }

    private boolean shouldDropExportLine(String line) {
        if (!StringUtils.hasText(line)) {
            return false;
        }

        for (String marker : EXPORT_NOISE_MARKERS) {
            if (line.contains(marker)) {
                return true;
            }
        }

        String compact = line.replace(" ", "");
        return containsConflictingJudgement(compact, "非单射", "是单射")
                || containsConflictingJudgement(compact, "不是单射", "是单射")
                || containsConflictingJudgement(compact, "非满射", "是满射")
                || containsConflictingJudgement(compact, "不是满射", "是满射");
    }

    private boolean containsConflictingJudgement(String line, String negative, String positive) {
        return line.contains(negative) && line.contains(positive);
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

    private void writePaperToWord(XWPFDocument document, ExportContext context)
            throws IOException {
        writeWordContent(document, context.paperName(), 18, true, ParagraphAlignment.CENTER);
        writeWordContent(document, buildSummaryLine(context), 11, false, ParagraphAlignment.LEFT);

        for (int sectionIndex = 0; sectionIndex < context.sections().size(); sectionIndex++) {
            ExportSection section = context.sections().get(sectionIndex);
            writeWordContent(document, section.title(), 15, true, ParagraphAlignment.LEFT);

            for (int questionIndex = 0; questionIndex < section.questions().size(); questionIndex++) {
                QuestionResponseDTO question = section.questions().get(questionIndex);
                writeWordContent(
                        document,
                        formatQuestionHeading(questionIndex + 1, question),
                        13,
                        true,
                        ParagraphAlignment.LEFT
                );

                writeWordContent(document, question.getQuestionContent(), 12, false, ParagraphAlignment.LEFT);

                if (!CollectionUtils.isEmpty(question.getOptions())) {
                    for (int optionIndex = 0; optionIndex < question.getOptions().size(); optionIndex++) {
                        QuestionOptionSimpleDTO option = question.getOptions().get(optionIndex);
                        if (option == null) {
                            continue;
                        }
                        String optionLine = getOptionLabel(option, optionIndex) + ". "
                                + defaultString(option.getOptionContent());
                        writeWordContent(document, optionLine, 12, false, ParagraphAlignment.LEFT);
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

    private void writeAnswerSectionToWord(XWPFDocument document, QuestionResponseDTO question)
            throws IOException {
        List<String> answers = buildAnswerSections(question);
        if (answers.isEmpty()) {
            return;
        }

        writeWordContent(document, ANSWER_SECTION_TITLE, 12, true, ParagraphAlignment.LEFT);
        for (String line : answers) {
            writeWordContent(document, line, 11, false, ParagraphAlignment.LEFT);
        }
    }

    private void writeWordContent(XWPFDocument document,
                                  String content,
                                  int fontSize,
                                  boolean bold,
                                  ParagraphAlignment alignment) throws IOException {
        List<RenderBlock> blocks = parseRenderBlocks(content);
        if (blocks.isEmpty()) {
            return;
        }

        for (RenderBlock block : blocks) {
            if (block.type() == RenderBlockType.BLANK_LINE) {
                document.createParagraph();
                continue;
            }

            if (block.type() == RenderBlockType.DISPLAY_FORMULA) {
                XWPFParagraph paragraph = document.createParagraph();
                paragraph.setAlignment(ParagraphAlignment.CENTER);
                if (!appendWordFormula(paragraph, block.formula(), fontSize)) {
                    XWPFRun run = paragraph.createRun();
                    run.setFontFamily(DEFAULT_WORD_FONT);
                    run.setFontSize(fontSize);
                    run.setText(block.formula().originalText());
                }
                continue;
            }

            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setAlignment(alignment);
            appendWordInlineNodes(paragraph, block.inlineNodes(), fontSize, bold);
        }
    }

    private void appendWordInlineNodes(XWPFParagraph paragraph,
                                       List<InlineNode> inlineNodes,
                                       int fontSize,
                                       boolean bold) throws IOException {
        for (InlineNode inlineNode : inlineNodes) {
            if (inlineNode.isText()) {
                if (inlineNode.text() == null || inlineNode.text().isEmpty()) {
                    continue;
                }
                XWPFRun run = paragraph.createRun();
                run.setFontFamily(DEFAULT_WORD_FONT);
                run.setFontSize(fontSize);
                run.setBold(bold);
                run.setText(inlineNode.text());
                continue;
            }

            if (!appendWordFormula(paragraph, inlineNode.formula(), fontSize)) {
                XWPFRun run = paragraph.createRun();
                run.setFontFamily(DEFAULT_WORD_FONT);
                run.setFontSize(fontSize);
                run.setBold(bold);
                run.setText(inlineNode.formula().originalText());
            }
        }
    }

    private boolean appendWordFormula(XWPFParagraph paragraph,
                                      FormulaPlaceholder placeholder,
                                      int fontSize) throws IOException {
        if (paragraph == null || placeholder == null || !StringUtils.hasText(placeholder.latex())) {
            return false;
        }

        if (WordOmmlFormulaConverter.appendFormula(paragraph, placeholder.latex(), placeholder.display())) {
            return true;
        }

        RenderedFormula renderedFormula = renderFormula(placeholder, fontSize);
        if (renderedFormula == null) {
            return false;
        }

        RenderedFormula fittedFormula = placeholder.display()
                ? renderedFormula.fitToWidth(WORD_MAX_CONTENT_WIDTH_PT)
                : renderedFormula;
        return appendWordFormulaImage(paragraph, fittedFormula);
    }

    private boolean appendWordFormulaImage(XWPFParagraph paragraph,
                                           RenderedFormula renderedFormula) throws IOException {
        if (paragraph == null || renderedFormula == null || renderedFormula.imageBytes() == null
                || renderedFormula.imageBytes().length == 0) {
            return false;
        }

        XWPFRun run = paragraph.createRun();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(renderedFormula.imageBytes())) {
            run.addPicture(
                    inputStream,
                    Document.PICTURE_TYPE_PNG,
                    "formula.png",
                    Units.toEMU(renderedFormula.widthPt()),
                    Units.toEMU(renderedFormula.heightPt())
            );
            return true;
        } catch (Exception e) {
            log.warn("Failed to append rendered formula image to Word document.", e);
            return false;
        }
    }

    private void writeQuestionToPdf(PdfPaperWriter writer,
                                    int order,
                                    QuestionResponseDTO question,
                                    boolean includeAnswers) throws IOException {
        writer.writeParagraph(formatQuestionHeading(order, question), 13F, true);
        writer.addSpacer(4F);
        writer.writeParagraph(question.getQuestionContent(), 12F);

        if (!CollectionUtils.isEmpty(question.getOptions())) {
            writer.addSpacer(6F);
            for (int optionIndex = 0; optionIndex < question.getOptions().size(); optionIndex++) {
                QuestionOptionSimpleDTO option = question.getOptions().get(optionIndex);
                if (option == null) {
                    continue;
                }
                String optionLine = getOptionLabel(option, optionIndex) + ". "
                        + defaultString(option.getOptionContent());
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
        writer.writeParagraph(ANSWER_SECTION_TITLE, 12F, true);
        for (String line : answers) {
            writer.writeParagraph(line, 11F);
        }
    }

    private PDFont loadPdfFont(PDDocument document) throws IOException {
        for (String candidate : CLASSPATH_FONT_CANDIDATES) {
            ClassPathResource resource = new ClassPathResource(candidate);
            if (!resource.exists()) {
                continue;
            }
            try (InputStream inputStream = resource.getInputStream()) {
                PDFont font = PDType0Font.load(document, inputStream);
                validatePdfFont(font, "classpath:" + candidate);
                log.debug("Loading PDF export font from classpath:{}", candidate);
                return font;
            } catch (Exception e) {
                log.debug("Failed to load PDF export font from classpath:{}", candidate, e);
            }
        }

        for (String candidate : FILE_SYSTEM_FONT_CANDIDATES) {
            Path path = Path.of(candidate);
            if (!Files.isRegularFile(path)) {
                continue;
            }
            try (InputStream inputStream = Files.newInputStream(path)) {
                PDFont font = PDType0Font.load(document, inputStream);
                validatePdfFont(font, candidate);
                log.debug("Loading PDF export font from {}", candidate);
                return font;
            } catch (Exception e) {
                log.debug("Failed to load PDF export font from {}", candidate, e);
            }
        }

        throw new IOException("No usable CJK font available for PDF export");
    }

    private void validatePdfFont(PDFont font, String fontSource) throws IOException {
        try {
            font.getStringWidth(PDF_FONT_VALIDATION_TEXT);
        } catch (IllegalArgumentException e) {
            throw new IOException("Font does not support required CJK glyphs: " + fontSource, e);
        }
    }

    private String buildSummaryLine(ExportContext context) {
        return "题目数：" + context.questions().size()
                + "    总分：" + formatScore(sumScore(context.questions()))
                + "    预计时长：" + sumEstimatedTime(context.questions()) + "分钟"
                + "    导出版本："
                + (context.includeAnswers() ? "答案版" : "试题版");
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
            return question.getQuestionTitle().trim();
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
                    lines.add(label + " 解析：" + option.getExplanation());
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
                lines.add(prefix + defaultString(answer.getAnswerContent()));
                if (StringUtils.hasText(answer.getExplanation())) {
                    lines.add("解析：" + answer.getExplanation());
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

    private List<RenderBlock> parseRenderBlocks(String content) {
        if (!StringUtils.hasText(content)) {
            return List.of();
        }

        ExtractedContent extractedContent = extractMathPlaceholders(content);
        String plainText = toPlainText(extractedContent.contentWithPlaceholders());
        if (!StringUtils.hasText(plainText)) {
            return List.of();
        }

        String normalized = plainText
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("\\n{3,}", "\n\n");

        List<RenderBlock> blocks = new ArrayList<>();
        String[] lines = normalized.split("\n", -1);
        for (String line : lines) {
            if (!StringUtils.hasText(line.trim())) {
                blocks.add(RenderBlock.blankLine());
                continue;
            }

            List<InlineNode> inlineNodes = tokenizeInline(line, extractedContent.placeholders());
            appendLineBlocks(blocks, inlineNodes);
        }

        return trimBlankBlocks(blocks);
    }

    private void appendLineBlocks(List<RenderBlock> blocks, List<InlineNode> inlineNodes) {
        List<InlineNode> paragraphNodes = new ArrayList<>();
        for (InlineNode inlineNode : inlineNodes) {
            if (inlineNode.isFormula() && inlineNode.formula().display()) {
                addParagraphBlock(blocks, paragraphNodes);
                blocks.add(RenderBlock.displayFormula(inlineNode.formula()));
                continue;
            }
            paragraphNodes.add(inlineNode);
        }
        addParagraphBlock(blocks, paragraphNodes);
    }

    private void addParagraphBlock(List<RenderBlock> blocks, List<InlineNode> paragraphNodes) {
        if (!hasVisibleContent(paragraphNodes)) {
            paragraphNodes.clear();
            return;
        }
        blocks.add(RenderBlock.paragraph(mergeAdjacentTextNodes(paragraphNodes)));
        paragraphNodes.clear();
    }

    private boolean hasVisibleContent(List<InlineNode> inlineNodes) {
        for (InlineNode inlineNode : inlineNodes) {
            if (inlineNode.isFormula()) {
                return true;
            }
            if (StringUtils.hasText(inlineNode.text())) {
                return true;
            }
        }
        return false;
    }

    private List<InlineNode> mergeAdjacentTextNodes(List<InlineNode> inlineNodes) {
        List<InlineNode> merged = new ArrayList<>();
        StringBuilder currentText = new StringBuilder();

        for (InlineNode inlineNode : inlineNodes) {
            if (inlineNode.isText()) {
                currentText.append(inlineNode.text());
                continue;
            }

            if (currentText.length() > 0) {
                merged.add(InlineNode.text(currentText.toString()));
                currentText.setLength(0);
            }
            merged.add(inlineNode);
        }

        if (currentText.length() > 0) {
            merged.add(InlineNode.text(currentText.toString()));
        }
        return merged;
    }

    private List<RenderBlock> trimBlankBlocks(List<RenderBlock> blocks) {
        if (blocks.isEmpty()) {
            return blocks;
        }

        List<RenderBlock> trimmed = new ArrayList<>();
        boolean previousBlank = true;
        for (RenderBlock block : blocks) {
            if (block.type() == RenderBlockType.BLANK_LINE) {
                if (!previousBlank) {
                    trimmed.add(block);
                }
                previousBlank = true;
                continue;
            }

            trimmed.add(block);
            previousBlank = false;
        }

        while (!trimmed.isEmpty() && trimmed.get(trimmed.size() - 1).type() == RenderBlockType.BLANK_LINE) {
            trimmed.remove(trimmed.size() - 1);
        }
        return trimmed;
    }

    private List<InlineNode> tokenizeInline(String line, Map<String, FormulaPlaceholder> placeholders) {
        List<InlineNode> inlineNodes = new ArrayList<>();
        Matcher matcher = FORMULA_PLACEHOLDER_PATTERN.matcher(line);
        int currentIndex = 0;

        while (matcher.find()) {
            if (matcher.start() > currentIndex) {
                inlineNodes.add(InlineNode.text(line.substring(currentIndex, matcher.start())));
            }

            String marker = matcher.group();
            FormulaPlaceholder placeholder = placeholders.get(marker);
            if (placeholder == null) {
                inlineNodes.add(InlineNode.text(marker));
            } else {
                inlineNodes.add(InlineNode.formula(placeholder));
            }
            currentIndex = matcher.end();
        }

        if (currentIndex < line.length()) {
            inlineNodes.add(InlineNode.text(line.substring(currentIndex)));
        }
        return inlineNodes;
    }

    private ExtractedContent extractMathPlaceholders(String content) {
        String normalized = content
                .replace("\r\n", "\n")
                .replace('\r', '\n');

        StringBuilder builder = new StringBuilder();
        Map<String, FormulaPlaceholder> placeholders = new LinkedHashMap<>();
        int currentIndex = 0;
        int formulaIndex = 0;

        while (currentIndex < normalized.length()) {
            FormulaMatch formulaMatch = matchFormulaAt(normalized, currentIndex);
            if (formulaMatch == null) {
                builder.append(normalized.charAt(currentIndex));
                currentIndex++;
                continue;
            }

            String marker = FORMULA_PLACEHOLDER_PREFIX + formulaIndex++ + FORMULA_PLACEHOLDER_SUFFIX;
            FormulaPlaceholder placeholder = new FormulaPlaceholder(
                    marker,
                    decodeHtmlEntities(formulaMatch.latex().trim()),
                    formulaMatch.display(),
                    formulaMatch.originalText()
            );
            placeholders.put(marker, placeholder);

            if (formulaMatch.display()) {
                appendDisplayPlaceholder(builder, marker);
            } else {
                builder.append(marker);
            }
            currentIndex = formulaMatch.nextIndex();
        }

        return new ExtractedContent(builder.toString(), placeholders);
    }

    private void appendDisplayPlaceholder(StringBuilder builder, String marker) {
        if (builder.length() > 0 && builder.charAt(builder.length() - 1) != '\n') {
            builder.append('\n');
        }
        builder.append(marker).append('\n');
    }

    private FormulaMatch matchFormulaAt(String content, int index) {
        if (content.startsWith("$$", index) && !isEscapedDollar(content, index)) {
            int endIndex = findClosingDoubleDollar(content, index + 2);
            if (endIndex >= 0) {
                return new FormulaMatch(
                        content.substring(index + 2, endIndex),
                        true,
                        content.substring(index, endIndex + 2),
                        endIndex + 2
                );
            }
        }

        if (content.startsWith("\\[", index)) {
            int endIndex = content.indexOf("\\]", index + 2);
            if (endIndex >= 0) {
                return new FormulaMatch(
                        content.substring(index + 2, endIndex),
                        true,
                        content.substring(index, endIndex + 2),
                        endIndex + 2
                );
            }
        }

        if (content.startsWith("\\(", index)) {
            int endIndex = content.indexOf("\\)", index + 2);
            if (endIndex >= 0) {
                return new FormulaMatch(
                        content.substring(index + 2, endIndex),
                        false,
                        content.substring(index, endIndex + 2),
                        endIndex + 2
                );
            }
        }

        if (content.charAt(index) == '$'
                && !isEscapedDollar(content, index)
                && (index + 1 >= content.length() || content.charAt(index + 1) != '$')) {
            int endIndex = findClosingInlineDollar(content, index + 1);
            if (endIndex >= 0) {
                return new FormulaMatch(
                        content.substring(index + 1, endIndex),
                        false,
                        content.substring(index, endIndex + 1),
                        endIndex + 1
                );
            }
        }

        FormulaMatch bareEnvironmentMatch = matchBareLatexEnvironment(content, index);
        if (bareEnvironmentMatch != null) {
            return bareEnvironmentMatch;
        }

        return null;
    }

    private FormulaMatch matchBareLatexEnvironment(String content, int index) {
        if (!content.startsWith("\\begin{", index)) {
            return null;
        }

        int envNameStart = index + "\\begin{".length();
        int envNameEnd = content.indexOf('}', envNameStart);
        if (envNameEnd < 0) {
            return null;
        }

        String environment = content.substring(envNameStart, envNameEnd).trim();
        if (!StringUtils.hasText(environment) || !BARE_LATEX_ENVIRONMENTS.contains(environment)) {
            return null;
        }

        String closingToken = "\\end{" + environment + "}";
        int closingIndex = content.indexOf(closingToken, envNameEnd + 1);
        if (closingIndex < 0) {
            return null;
        }

        int nextIndex = closingIndex + closingToken.length();
        return new FormulaMatch(
                content.substring(index, nextIndex),
                isStandaloneFormula(content, index, nextIndex),
                content.substring(index, nextIndex),
                nextIndex
        );
    }

    private boolean isStandaloneFormula(String content, int startIndex, int endIndex) {
        for (int i = startIndex - 1; i >= 0; i--) {
            char current = content.charAt(i);
            if (current == '\n') {
                break;
            }
            if (!Character.isWhitespace(current)) {
                return false;
            }
        }

        for (int i = endIndex; i < content.length(); i++) {
            char current = content.charAt(i);
            if (current == '\n') {
                break;
            }
            if (!Character.isWhitespace(current)) {
                return false;
            }
        }
        return true;
    }

    private int findClosingDoubleDollar(String content, int startIndex) {
        int currentIndex = startIndex;
        while (currentIndex < content.length()) {
            int closingIndex = content.indexOf("$$", currentIndex);
            if (closingIndex < 0) {
                return -1;
            }
            if (!isEscapedDollar(content, closingIndex)) {
                return closingIndex;
            }
            currentIndex = closingIndex + 2;
        }
        return -1;
    }

    private int findClosingInlineDollar(String content, int startIndex) {
        int currentIndex = startIndex;
        while (currentIndex < content.length()) {
            int closingIndex = content.indexOf('$', currentIndex);
            if (closingIndex < 0) {
                return -1;
            }
            boolean singleDollar = closingIndex + 1 >= content.length() || content.charAt(closingIndex + 1) != '$';
            if (!isEscapedDollar(content, closingIndex) && singleDollar) {
                return closingIndex;
            }
            currentIndex = closingIndex + 1;
        }
        return -1;
    }

    private boolean isEscapedDollar(String content, int dollarIndex) {
        int backslashCount = 0;
        for (int i = dollarIndex - 1; i >= 0 && content.charAt(i) == '\\'; i--) {
            backslashCount++;
        }
        return backslashCount % 2 == 1;
    }

    private String decodeHtmlEntities(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        return content
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");
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

    private RenderedFormula renderFormula(FormulaPlaceholder placeholder, float fontSize) {
        if (placeholder == null || !StringUtils.hasText(placeholder.latex())) {
            return null;
        }

        FormulaRenderKey key = new FormulaRenderKey(
                placeholder.latex(),
                placeholder.display(),
                Math.max(10, Math.round(fontSize))
        );
        RenderedFormula cachedFormula = renderedFormulaCache.get(key);
        if (cachedFormula != null) {
            return cachedFormula;
        }

        RenderedFormula renderedFormula = createRenderedFormula(key);
        if (renderedFormula != null) {
            renderedFormulaCache.putIfAbsent(key, renderedFormula);
        }
        return renderedFormula;
    }

    private RenderedFormula createRenderedFormula(FormulaRenderKey key) {
        try {
            TeXFormula texFormula = new TeXFormula(key.latex());
            int style = key.display() ? TeXConstants.STYLE_DISPLAY : TeXConstants.STYLE_TEXT;
            TeXIcon icon = texFormula.createTeXIcon(style, key.fontSize());
            icon.setInsets(new Insets(0, 0, 0, 0));

            int imageWidth = Math.max(1, Math.round(icon.getIconWidth() * FORMULA_RENDER_SCALE));
            int imageHeight = Math.max(1, Math.round(icon.getIconHeight() * FORMULA_RENDER_SCALE));

            BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(Color.BLACK);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.scale(FORMULA_RENDER_SCALE, FORMULA_RENDER_SCALE);
            JLabel label = new JLabel();
            label.setForeground(Color.BLACK);
            icon.paintIcon(label, graphics, 0, 0);
            graphics.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);

            return new RenderedFormula(
                    key,
                    outputStream.toByteArray(),
                    icon.getIconWidth(),
                    icon.getIconHeight(),
                    Math.max(0F, icon.getIconDepth())
            );
        } catch (Exception e) {
            log.warn("Failed to render latex formula. latex={}", key.latex(), e);
            return null;
        }
    }

    private String defaultString(String content) {
        return content == null ? "" : content;
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

    private record ExtractedContent(String contentWithPlaceholders,
                                    Map<String, FormulaPlaceholder> placeholders) {
    }

    private record FormulaMatch(String latex,
                                boolean display,
                                String originalText,
                                int nextIndex) {
    }

    private record FormulaPlaceholder(String marker,
                                      String latex,
                                      boolean display,
                                      String originalText) {
    }

    private enum RenderBlockType {
        PARAGRAPH,
        DISPLAY_FORMULA,
        BLANK_LINE
    }

    private record RenderBlock(RenderBlockType type,
                               List<InlineNode> inlineNodes,
                               FormulaPlaceholder formula) {

        private static RenderBlock paragraph(List<InlineNode> inlineNodes) {
            return new RenderBlock(RenderBlockType.PARAGRAPH, List.copyOf(inlineNodes), null);
        }

        private static RenderBlock displayFormula(FormulaPlaceholder formula) {
            return new RenderBlock(RenderBlockType.DISPLAY_FORMULA, List.of(), formula);
        }

        private static RenderBlock blankLine() {
            return new RenderBlock(RenderBlockType.BLANK_LINE, List.of(), null);
        }
    }

    private record InlineNode(String text, FormulaPlaceholder formula) {

        private static InlineNode text(String text) {
            return new InlineNode(text, null);
        }

        private static InlineNode formula(FormulaPlaceholder formula) {
            return new InlineNode(null, formula);
        }

        private boolean isText() {
            return formula == null;
        }

        private boolean isFormula() {
            return formula != null;
        }
    }

    private record FormulaRenderKey(String latex, boolean display, int fontSize) {
    }

    private record RenderedFormula(FormulaRenderKey key,
                                   byte[] imageBytes,
                                   float widthPt,
                                   float heightPt,
                                   float depthPt) {

        private float aboveBaseline() {
            return Math.max(0F, heightPt - depthPt);
        }

        private RenderedFormula fitToWidth(float maxWidth) {
            if (maxWidth <= 0F || widthPt <= maxWidth) {
                return this;
            }
            float scale = maxWidth / widthPt;
            return new RenderedFormula(key, imageBytes, widthPt * scale, heightPt * scale, depthPt * scale);
        }
    }

    private record PdfInlineFragment(String text,
                                     float fontSize,
                                     boolean bold,
                                     RenderedFormula formula,
                                     FormulaPlaceholder placeholder,
                                     float width,
                                     float aboveBaseline,
                                     float belowBaseline) {

        private static PdfInlineFragment text(String text,
                                              float fontSize,
                                              boolean bold,
                                              float width,
                                              float aboveBaseline,
                                              float belowBaseline) {
            return new PdfInlineFragment(text, fontSize, bold, null, null, width, aboveBaseline, belowBaseline);
        }

        private static PdfInlineFragment formula(RenderedFormula formula, FormulaPlaceholder placeholder) {
            return new PdfInlineFragment(
                    null,
                    0F,
                    false,
                    formula,
                    placeholder,
                    formula.widthPt(),
                    formula.aboveBaseline(),
                    formula.depthPt()
            );
        }

        private boolean isText() {
            return formula == null;
        }
    }

    private final class PdfPaperWriter implements Closeable {

        private static final float MARGIN_LEFT = 56F;
        private static final float MARGIN_RIGHT = 56F;
        private static final float MARGIN_TOP = 60F;
        private static final float MARGIN_BOTTOM = 56F;
        private static final float LINE_GAP = 4F;

        private final PDDocument document;
        private final PDFont font;
        private final Map<FormulaRenderKey, PDImageXObject> imageCache = new HashMap<>();

        private PDPage currentPage;
        private PDPageContentStream contentStream;
        private float cursorY;

        private PdfPaperWriter(PDDocument document, PDFont font) throws IOException {
            this.document = document;
            this.font = font;
            startNewPage();
        }

        private void writeCentered(String content, float fontSize) throws IOException {
            writeCentered(content, fontSize, false);
        }

        private void writeCentered(String content, float fontSize, boolean bold) throws IOException {
            writeBlocks(parseRenderBlocks(content), fontSize, bold, true);
        }

        private void writeParagraph(String content, float fontSize) throws IOException {
            writeParagraph(content, fontSize, false);
        }

        private void writeParagraph(String content, float fontSize, boolean bold) throws IOException {
            writeBlocks(parseRenderBlocks(content), fontSize, bold, false);
        }

        private void writeBlocks(List<RenderBlock> blocks,
                                 float fontSize,
                                 boolean bold,
                                 boolean centeredParagraph) throws IOException {
            for (RenderBlock block : blocks) {
                if (block.type() == RenderBlockType.BLANK_LINE) {
                    addSpacer(fontSize * 0.5F);
                    continue;
                }

                if (block.type() == RenderBlockType.DISPLAY_FORMULA) {
                    writeDisplayFormula(block.formula(), fontSize);
                    continue;
                }

                writeInlineParagraph(block.inlineNodes(), fontSize, bold, centeredParagraph);
            }
        }

        private void writeInlineParagraph(List<InlineNode> inlineNodes,
                                          float fontSize,
                                          boolean bold,
                                          boolean centered) throws IOException {
            List<PdfInlineFragment> fragments = buildInlineFragments(inlineNodes, fontSize, bold);
            List<List<PdfInlineFragment>> wrappedLines = wrapFragments(fragments, usableWidth());
            for (List<PdfInlineFragment> line : wrappedLines) {
                writeWrappedLine(line, centered);
            }
        }

        private List<PdfInlineFragment> buildInlineFragments(List<InlineNode> inlineNodes,
                                                             float fontSize,
                                                             boolean bold) throws IOException {
            List<PdfInlineFragment> fragments = new ArrayList<>();
            for (InlineNode inlineNode : inlineNodes) {
                if (inlineNode.isText()) {
                    if (inlineNode.text() == null) {
                        continue;
                    }
                    for (int i = 0; i < inlineNode.text().length(); i++) {
                        String character = String.valueOf(inlineNode.text().charAt(i));
                        float width = textWidth(character, fontSize);
                        fragments.add(PdfInlineFragment.text(
                                character,
                                fontSize,
                                bold,
                                width,
                                textAscent(fontSize),
                                textDescent(fontSize)
                        ));
                    }
                    continue;
                }

                RenderedFormula renderedFormula = renderFormula(inlineNode.formula(), fontSize);
                if (renderedFormula == null) {
                    String fallback = inlineNode.formula().originalText();
                    for (int i = 0; i < fallback.length(); i++) {
                        String character = String.valueOf(fallback.charAt(i));
                        fragments.add(PdfInlineFragment.text(
                                character,
                                fontSize,
                                bold,
                                textWidth(character, fontSize),
                                textAscent(fontSize),
                                textDescent(fontSize)
                        ));
                    }
                    continue;
                }

                fragments.add(PdfInlineFragment.formula(
                        renderedFormula.fitToWidth(usableWidth()),
                        inlineNode.formula()
                ));
            }
            return fragments;
        }

        private List<List<PdfInlineFragment>> wrapFragments(List<PdfInlineFragment> fragments, float maxWidth) {
            List<List<PdfInlineFragment>> lines = new ArrayList<>();
            List<PdfInlineFragment> currentLine = new ArrayList<>();
            float currentWidth = 0F;

            for (PdfInlineFragment fragment : fragments) {
                if (currentLine.isEmpty() && fragment.isText() && " ".equals(fragment.text())) {
                    continue;
                }

                boolean overflow = !currentLine.isEmpty() && currentWidth + fragment.width() > maxWidth;
                if (overflow) {
                    lines.add(new ArrayList<>(currentLine));
                    currentLine.clear();
                    currentWidth = 0F;
                    if (fragment.isText() && " ".equals(fragment.text())) {
                        continue;
                    }
                }

                currentLine.add(fragment);
                currentWidth += fragment.width();
            }

            if (!currentLine.isEmpty()) {
                lines.add(currentLine);
            }
            if (lines.isEmpty()) {
                lines.add(List.of());
            }
            return lines;
        }

        private void writeWrappedLine(List<PdfInlineFragment> line, boolean centered) throws IOException {
            if (line.isEmpty()) {
                addSpacer(10F);
                return;
            }

            float lineAbove = 0F;
            float lineBelow = 0F;
            float lineWidth = 0F;
            for (PdfInlineFragment fragment : line) {
                lineAbove = Math.max(lineAbove, fragment.aboveBaseline());
                lineBelow = Math.max(lineBelow, fragment.belowBaseline());
                lineWidth += fragment.width();
            }

            float requiredHeight = lineAbove + lineBelow + LINE_GAP;
            ensureSpace(requiredHeight);

            float baselineY = cursorY - lineAbove;
            float x = centered
                    ? MARGIN_LEFT + Math.max(0F, (usableWidth() - lineWidth) / 2F)
                    : MARGIN_LEFT;

            for (PdfInlineFragment fragment : line) {
                if (fragment.isText()) {
                    writeText(x, baselineY, fragment.text(), fragment.fontSize(), fragment.bold());
                } else {
                    drawFormula(fragment.formula(), x, baselineY - fragment.formula().depthPt());
                }
                x += fragment.width();
            }

            cursorY -= requiredHeight;
        }

        private void writeDisplayFormula(FormulaPlaceholder placeholder, float fontSize) throws IOException {
            RenderedFormula renderedFormula = renderFormula(placeholder, fontSize);
            if (renderedFormula == null) {
                writeParagraph(placeholder.originalText(), fontSize, false);
                return;
            }

            RenderedFormula fittedFormula = renderedFormula.fitToWidth(usableWidth());
            float requiredHeight = fittedFormula.heightPt() + LINE_GAP;
            ensureSpace(requiredHeight);

            float x = MARGIN_LEFT + Math.max(0F, (usableWidth() - fittedFormula.widthPt()) / 2F);
            float y = cursorY - fittedFormula.heightPt();
            drawFormula(fittedFormula, x, y);
            cursorY -= requiredHeight;
        }

        private void drawFormula(RenderedFormula renderedFormula, float x, float y) throws IOException {
            PDImageXObject image = imageCache.computeIfAbsent(
                    renderedFormula.key(),
                    ignored -> {
                        try {
                            return PDImageXObject.createFromByteArray(
                                    document,
                                    renderedFormula.imageBytes(),
                                    "formula"
                            );
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    }
            );
            contentStream.drawImage(image, x, y, renderedFormula.widthPt(), renderedFormula.heightPt());
        }

        private void addSpacer(float space) throws IOException {
            ensureSpace(space);
            cursorY -= space;
        }

        private void writeText(float x, float y, String text, float fontSize, boolean bold) throws IOException {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.setRenderingMode(bold ? RenderingMode.FILL_STROKE : RenderingMode.FILL);
            if (bold) {
                contentStream.setLineWidth(Math.max(0.18F, fontSize * 0.03F));
            }
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(text == null ? "" : text);
            contentStream.endText();
            contentStream.setRenderingMode(RenderingMode.FILL);
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
            if (text == null || text.isEmpty()) {
                return 0F;
            }
            return font.getStringWidth(text) / 1000F * fontSize;
        }

        private float textAscent(float fontSize) {
            return fontSize * PDF_TEXT_ASCENT_RATIO;
        }

        private float textDescent(float fontSize) {
            return fontSize * PDF_TEXT_DESCENT_RATIO;
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
