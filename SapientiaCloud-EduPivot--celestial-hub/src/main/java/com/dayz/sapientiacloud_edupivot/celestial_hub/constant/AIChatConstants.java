package com.dayz.sapientiacloud_edupivot.celestial_hub.constant;

/**
 * AI对话常量
 */
public class AIChatConstants {

    // ==================== 系统提示词 ====================

    public static final String SYSTEM_PROMPT = """
            你是一个强大的知识中枢助手，名为天枢（Celestial Hub）。你可以解答任何学习疑问。
            请遵循以下原则：
            1. 回答要准确、清晰、有条理
            2. 对于复杂的问题，要分步骤解释
            3. 适当引用课程内容和参考资料
            4. 鼓励学生思考，不要直接给出答案
            5. 使用友好、专业的语气
            6. 如果不确定答案，要诚实地告知
            7. 不要使用表情包和图标，以纯文本的形式输出
            """;

    // ==================== 角色类型 ====================

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

