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
            14. 所有数学公式必须使用标准 LaTeX 定界符包裹：行内公式使用 $...$，独立公式使用 $$...$$。
            15. 矩阵、分段函数、上下标、分式、根号等数学表达式必须使用合法的 LaTeX 语法，不要输出裸露的 \\begin{bmatrix}...\\end{bmatrix} 或裸露的 x^2、Q^TAQ 文本。
            16. questionContent、options.optionContent、options.explanation、answers.answerContent、answers.explanation 中只要出现数学表达式，都必须遵守上述公式格式要求。
            17. 即使某个字段只包含一个公式、集合表示、矩阵、上下标表达式或符号表达式，也必须完整包裹在 $...$ 或 $$...$$ 中，禁止输出裸露的 \\frac、\\sqrt、\\mathbb、\\in、\\mid、\\{...\\}、x^2 等文本。
            18. 数学公式里如果需要出现中文说明，必须使用 \\text{中文说明}，并且整段公式仍然放在数学定界符内部，不能把 \\text{...} 单独裸露在普通文本里。
            19. 在返回最终 JSON 前，必须逐字段自检 questionContent、options.optionContent、options.explanation、answers.answerContent、answers.explanation，确保不存在未加数学定界符的 LaTeX 命令或符号表达式。
            20. 不要输出“更正”“修正”“重新计算”“最终采用”“调整问题”“思考过程”“推导草稿”等元信息。
            21. 不要在答案或解析中自我否定、来回修改或保留草稿痕迹；只输出最终确定后的题目、答案和解析。
            22. 解析内容必须简洁、确定、可直接展示给学生，禁止出现命题过程、出题策略或编辑说明。
            """;

    private QuestionConstants() {
    }
}
