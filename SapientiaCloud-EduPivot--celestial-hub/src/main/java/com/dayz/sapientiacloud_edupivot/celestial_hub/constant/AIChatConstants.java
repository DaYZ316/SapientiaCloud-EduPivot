package com.dayz.sapientiacloud_edupivot.celestial_hub.constant;

/**
 * AI对话常量
 */
public class AIChatConstants {

    // ==================== 系统提示词 ====================

    public static final String SYSTEM_PROMPT = """
            你是一个强大的知识中枢助手，名为天枢（Celestial Hub），负责解答学习问题、讲解知识、拆解思路并辅助用户理解内容。
            你的首要目标是：准确理解用户意图，输出清晰、可信、结构化且适合学习场景的答案。

            请严格遵循以下规则：

            一、回答原则
            1. 回答必须准确、清晰、有条理，优先帮助用户理解问题，而不是只给结论。
            2. 对复杂问题使用分步骤讲解；对简单问题直接回答，不要为了凑格式而过度展开。
            3. 如系统提供“参考内容”、知识库内容或文件内容，优先结合这些信息作答；若参考内容不足，应明确说明，不要编造。
            4. 适当引用课程内容、定义、公式、示例和推导过程，鼓励学生思考。
            5. 除非用户明确要求“只要答案”或“直接给结论”，否则不要只丢最终答案；优先给出思路、步骤、判断依据和检查方法。
            6. 使用友好、专业、克制的语气，不要使用表情、颜文字或图标。
            7. 如果不确定答案，要明确说明不确定点、已知条件和需要补充的信息。

            二、默认输出格式
            8. 默认使用标准 Markdown 输出，结构要稳定、清晰、可直接渲染。
            9. 复杂问题优先按以下顺序组织内容，简单问题可按需裁剪，不必机械套模板：
               - `## 核心结论`
               - `## 详细说明`
               - `## 示例`
               - `## 注意事项`
            10. 标题层级最多使用三级：`#`、`##`、`###`；没有必要时不要强行加标题。
            11. 步骤说明使用编号列表；并列信息使用无序列表；只有在确实需要比较时才使用表格，且表头与列数必须保持一致。
            12. 代码必须使用带语言标识的代码块，例如 ```java；示例应尽量完整、可运行，并补充必要注释。
            13. 数学内容必须使用 KaTeX / LaTeX 兼容语法：行内公式使用 `$...$`，块级公式使用 `$$...$$`；不要输出无法渲染的公式写法。任何裸露的 `\\frac`、`\\sqrt`、`\\mathbb`、`\\in`、`\\mid`、`\\{...\\}`、`x^2`、`Q^TAQ` 都必须先包进数学定界符。
            14. 行内代码、字段名、命令、路径和关键字使用反引号包裹，避免普通文本与代码混杂。

            三、用户指定格式优先
            15. 如果用户明确指定输出格式为 Markdown、HTML、LaTeX、JSON 或其他固定格式，必须严格遵守用户指定格式，且不要混用其他格式。
            16. 当用户要求“只输出代码”“只输出 HTML”“只输出 LaTeX”“只输出 JSON”等纯格式结果时，只输出目标内容本体，不要添加解释、前言、后记或多余包裹文字。
            17. 输出 HTML 时，确保标签闭合、层级清晰、结构完整、可直接渲染；输出 LaTeX 时，确保语法正确、环境完整、特殊字符已正确转义；输出 JSON 时，确保字段、层级和语法完全合法。

            四、边界与质量控制
            18. 不要输出思维链、内部推理过程或“我将如何思考”之类的元信息，只输出对用户有用的最终答案。
            19. 不要重复用户问题，不要堆砌空话，不要生成与问题无关的模板内容。
            20. 若用户提供模板、字段顺序、示例或目标结构，优先对齐其格式和顺序。
            """;

    // ==================== 角色类型 ====================

    public static final String MATH_LATEX_STYLE_PROMPT = """
            数学与 LaTeX 书写补充规则：
            1. 同一套题或同一份输出中的向量、矩阵、单位矩阵记号必须统一，默认优先使用 \\boldsymbol{x}、\\boldsymbol{A}、\\boldsymbol{I} 这类写法；如果改用 \\mathbf{x}、\\mathbf{A}、\\mathbf{I} 风格，则整套内容都保持一致，不要混用 \\mathbf{I} 与 \\boldsymbol{A}、\\boldsymbol{x}。
            2. 当括号、方括号或花括号包裹分式、矩阵、分段函数、求和、积分或其他较高公式时，必须使用可伸缩定界符，例如 \\left( ... \\right)、\\left[ ... \\right]、\\left\\{ ... \\right\\}。
            3. 例如需要包裹分式时，优先写成 \\left( \\frac{a}{b} \\right)，不要写成普通高度不匹配的 (\\frac{a}{b})。
            4. 公式内部如果存在逗号分隔、条件并列、推导转折或说明性短语，为提升可读性，可适度使用 \\, 或 \\quad 调整视觉间距；例如 a_i \\ge 0, \\, \\sum_i a_i = 1 这类结构应保证疏密自然，但不要机械堆砌间距命令。
            5. 以上一致性检查同样适用于题干、选项、答案、解析以及一般数学问答内容。
            """;

    public static final Integer ROLE_USER = 0;
    public static final Integer ROLE_ASSISTANT = 1;
    public static final Integer ROLE_SYSTEM = 2;

    // ==================== 模型名称 ====================

    public static final String MODEL_QWEN3_MAX = "qwen3-max";

    // ==================== 会话相关 ====================

    public static final String DEFAULT_SESSION_TITLE = "新对话";
    public static final int DEFAULT_SESSION_TYPE = 0;
    public static final int DEFAULT_MESSAGE_COUNT = 0;

    // ==================== 状态值 ====================

    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_ARCHIVED = 1;

    public static final int PINNED_FALSE = 0;
    public static final int PINNED_TRUE = 1;

    public static final int FAVORITE_FALSE = 0;
    public static final int FAVORITE_TRUE = 1;

    public static final int DELETED_FALSE = 0;
    public static final int DELETED_TRUE = 1;

    public static final int FEEDBACK_NONE = 0;
    public static final int FEEDBACK_LIKE = 1;
    public static final int FEEDBACK_DISLIKE = -1;

    public static final int MESSAGE_TYPE_TEXT = 0;
    public static final int MESSAGE_TYPE_IMAGE = 1;

    // ==================== 音频状态 ====================

    public static final int AUDIO_STATUS_NONE = 0;
    public static final int AUDIO_STATUS_PENDING = 1;
    public static final int AUDIO_STATUS_PROCESSING = 2;
    public static final int AUDIO_STATUS_READY = 3;
    public static final int AUDIO_STATUS_FAILED = 4;
    public static final int AUDIO_STATUS_CANCELED = 5;

    public static final String AUDIO_FORMAT_WAV = "wav";

    // ==================== 默认限制 ====================

    public static final int DEFAULT_HISTORY_LIMIT = 10;
    public static final int DEFAULT_RAG_TOP_K = 3;
    public static final double DEFAULT_RAG_SIMILARITY_THRESHOLD = 0.7;
    public static final int DEFAULT_TITLE_MAX_LENGTH = 20;
    public static final int DEFAULT_PREVIEW_MAX_LENGTH = 50;

    // ==================== 文本处理 ====================

    public static final String ELLIPSIS = "...";
    public static final String RAG_PREFIX = "参考内容：\n";
    public static final String RAG_ITEM_FORMAT = "【%s】%s\n%s";
    public static final String RAG_SEPARATOR = "\n\n";

    // ==================== Token 估算 ====================

    public static final double TOKEN_ESTIMATE_RATIO = 1.5;

    // ==================== 分页相关 ====================

    public static final int PAGE_NUM_OFFSET = 1;

    // ==================== 消息计数增量 ====================

    public static final int MESSAGE_COUNT_INCREMENT = 2;
    public static final int MESSAGE_COUNT_INCREMENT_SINGLE = 1;

    // ==================== MongoDB 字段名 ====================

    public static final String FIELD_ID = "_id";
    public static final String FIELD_SYS_USER_ID = "sys_user_id";
    public static final String FIELD_SESSION_ID = "session_id";
    public static final String FIELD_SESSION_TITLE = "session_title";
    public static final String FIELD_SESSION_TYPE = "session_type";
    public static final String FIELD_IS_PINNED = "is_pinned";
    public static final String FIELD_IS_FAVORITE = "is_favorite";
    public static final String FIELD_UPDATE_TIME = "update_time";
    public static final String FIELD_CREATE_TIME = "create_time";
    public static final String FIELD_COUNT = "count";

    // ==================== MongoDB 集合名 ====================

    public static final String COLLECTION_CHAT_MESSAGE = "mg_chat_message";
    public static final String COLLECTION_CHAT_SESSION = "mg_chat_session";

    private AIChatConstants() {
        // 禁止实例化
    }
}

