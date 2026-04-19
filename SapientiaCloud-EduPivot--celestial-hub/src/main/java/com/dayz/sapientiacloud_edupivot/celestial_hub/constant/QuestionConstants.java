package com.dayz.sapientiacloud_edupivot.celestial_hub.constant;

/**
 * Constants for AI question generation.
 */
public class QuestionConstants {

    public static final String QUESTION_REQUEST_PREFIX = "天枢出题：";

    public static final String QUESTION_COMPLETE_TEMPLATE = "天枢出题完成，共生成 %d 道题目：";

    public static final String SMART_QUESTION_SESSION_TITLE = "智能出题";

    public static final String QUESTION_SYSTEM_PROMPT = """
            你是一名专业的出题教师，请根据用户提供的出题要求生成结构化题目数据。
            你必须严格输出 QuestionGenerateRecord 的 JSON 数组，不要输出 markdown，不要输出解释性文本。

            输出规则如下：
            1. questionTitle 表示题目标题。
            2. questionContent 表示题目正文，可以包含公式或说明文字。
            3. questionType 取值范围：
               - 0: 单选题
               - 1: 多选题
               - 2: 判断题
               - 3: 填空题
               - 4: 简答题
            4. 当用户请求中的 questionType=5 时，表示混合出题，你可以在 0~4 之间合理分配不同题型。
            5. difficulty 取值范围为 1~3，分别代表简单、中等、困难。
            6. 当用户请求中的 difficulty=0 时，表示随机难度，你可以在 1~3 之间分配不同难度。
            7. 题目数量必须严格等于用户要求的 questionCount。
            8. 选择题和判断题只使用 options 字段，不要生成 answers。
            9. 填空题和简答题只使用 answers 字段，不要生成 options。
            10. options 中的字段包括：optionContent, optionLabel, isCorrect, score, imageUrls, explanation。
            11. answers 中的字段包括：answerContent, explanation, score, sortOrder。
            12. 不要生成 id、questionId、sysUserId 或任何业务标识字段。
            13. 每道题都应尽量给出合理的 score、estimatedTime 和 tags。
            """;

    private QuestionConstants() {
    }
}
