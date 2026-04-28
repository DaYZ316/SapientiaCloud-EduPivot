package com.dayz.sapientiacloud_edupivot.celestial_hub.utils;

import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.QuestionGenerateRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.enums.QuestionGenerationMode;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Locale helpers for question and paper generation flows.
 */
public final class QuestionGenerationLocaleUtils {

    public static final String LOCALE_ZH_CN = "zh-CN";
    public static final String LOCALE_EN_US = "en-US";

    private QuestionGenerationLocaleUtils() {
    }

    public static String resolveLocale(QuestionGenerateRequestDTO request) {
        String locale = normalizeLocale(request != null ? request.getLocale() : null);
        if (request != null) {
            request.setLocale(locale);
        }
        return locale;
    }

    public static String normalizeLocale(String locale) {
        if (!StringUtils.hasText(locale)) {
            return LOCALE_ZH_CN;
        }

        String normalized = locale.trim().replace('_', '-');
        String lowerCase = normalized.toLowerCase(Locale.ROOT);
        if ("en".equals(lowerCase) || lowerCase.startsWith("en-")) {
            return LOCALE_EN_US;
        }
        if ("zh".equals(lowerCase) || lowerCase.startsWith("zh-")) {
            return LOCALE_ZH_CN;
        }
        return LOCALE_ZH_CN;
    }

    public static boolean isEnglish(String locale) {
        return LOCALE_EN_US.equalsIgnoreCase(normalizeLocale(locale));
    }

    public static String text(String locale, String zhCn, String enUs) {
        return isEnglish(locale) ? enUs : zhCn;
    }

    public static String resolveSessionTitle(String locale, QuestionGenerationMode generationMode) {
        if (generationMode != null && generationMode.isPaper()) {
            return text(locale, "智能出卷", "AI Paper");
        }
        return text(locale, "智能出题", "AI Question");
    }

    public static String resolveResponseSummary(String locale,
                                                QuestionGenerationMode generationMode,
                                                int questionCount) {
        if (generationMode != null && generationMode.isPaper()) {
            return text(
                    locale,
                    String.format("天枢出卷完成，共生成 %d 道题目：", questionCount),
                    String.format("AI paper generation completed with %d questions:", questionCount)
            );
        }
        return text(
                locale,
                String.format("天枢出题完成，共生成 %d 道题目：", questionCount),
                String.format("AI question generation completed with %d questions:", questionCount)
        );
    }

    public static String buildOutputLanguageInstruction(String locale) {
        if (isEnglish(locale)) {
            return "Generate all user-facing content in English, including question titles, question bodies, options, answers, explanations, tags, section titles, and summaries, unless the user explicitly asks for another language.";
        }
        return "所有面向用户展示的内容都必须使用简体中文输出，包括题目标题、题干、选项、答案、解析、标签、分段标题和摘要；除非用户在要求中明确指定其他语言。";
    }

    public static String buildFormulaTextInstruction(String locale) {
        if (isEnglish(locale)) {
            return "When formulas contain natural-language words, wrap those words with \\text{...} and keep them inside the same math delimiters.";
        }
        return "如果公式中需要出现中文说明，必须使用 \\text{中文说明}，并保持在同一数学定界符内部。";
    }
}
