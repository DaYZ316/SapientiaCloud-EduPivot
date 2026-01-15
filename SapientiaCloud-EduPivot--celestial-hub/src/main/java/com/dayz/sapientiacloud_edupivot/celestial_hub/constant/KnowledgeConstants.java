package com.dayz.sapientiacloud_edupivot.celestial_hub.constant;

/**
 * 知识管理常量
 */
public class KnowledgeConstants {

    // ==================== MongoDB 字段名称 ====================

    public static final String FIELD_ID = "_id";
    public static final String FIELD_COURSE_ID = "course_id";
    public static final String FIELD_CHAPTER_ID = "chapter_id";
    public static final String FIELD_CHAPTER_NAME = "chapter_name";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_IS_DELETED = "is_deleted";
    public static final String FIELD_CREATE_TIME = "create_time";
    public static final String FIELD_UPDATE_TIME = "update_time";

    // ==================== MongoDB 集合名称 ====================

    public static final String COLLECTION_COURSE_CHAPTER = "mg_course_chapter";
    public static final String COLLECTION_QUESTION = "mg_question";
    public static final String COLLECTION_ANSWER = "mg_question_answer";
    public static final String COLLECTION_FORUM_POST = "mg_forum_post";

    // ==================== 内容类型 ====================

    public static final Integer CONTENT_TYPE_CHAPTER = 0;
    public static final Integer CONTENT_TYPE_QUESTION = 1;
    public static final Integer CONTENT_TYPE_TASK = 2;
    public static final Integer CONTENT_TYPE_FORUM = 3;
    public static final Integer CONTENT_TYPE_CHAT = 4;
    public static final Integer CONTENT_TYPE_FILE = 5;

    // ==================== 元数据键 ====================

    public static final String METADATA_CONTENT_TYPE = "contentType";
    public static final String METADATA_CONTENT_ID = "contentId";
    public static final String METADATA_COURSE_ID = "courseId";
    public static final String METADATA_CHAPTER_ID = "chapterId";
    public static final String METADATA_QUESTION_BANK_ID = "questionBankId";
    public static final String METADATA_QUESTION_ID = "questionId";
    public static final String METADATA_TASK_ID = "taskId";
    public static final String METADATA_FORUM_ID = "forumId";
    public static final String METADATA_POST_ID = "postId";
    public static final String METADATA_SESSION_ID = "sessionId";
    public static final String METADATA_MESSAGE_ID = "chatMessageId";
    public static final String METADATA_USER_ID = "userId";
    public static final String METADATA_CREATE_TIME = "createTime";
    public static final String METADATA_FILE_ID = "fileId";
    public static final String METADATA_TITLE = "title";
    public static final String METADATA_SCORE = "score";
    /**
     * 向量唯一标识（用于关联向量库与知识向量表）
     */
    public static final String METADATA_VECTOR_ID = "vectorId";

    // ==================== 默认值 ====================

    public static final int DEFAULT_TOP_K = 5;
    public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.7;
    public static final int DEFAULT_DELETED = 0;
    public static final int DEFAULT_STATUS = 0;
    public static final String DEFAULT_EMPTY_STRING = "";
    public static final double DEFAULT_SCORE = 0.0;

    // ==================== 向量模型 ====================

    public static final String EMBEDDING_MODEL_TEXT_V1 = "text-embedding-v1";

    // ==================== 批次处理 ====================

    /**
     * DashScope API 单次请求的文本数量限制
     */
    public static final int EMBEDDING_BATCH_SIZE = 25;

    // ==================== 删除标记 ====================

    public static final int DELETED_FALSE = 0;
    public static final int DELETED_TRUE = 1;

    // ==================== 状态值 ====================

    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_DISABLED = 1;

    // ==================== 元数据标签键 ====================

    /**
     * 标签元数据键
     */
    public static final String METADATA_TAGS = "tags";
    public static final String METADATA_EMBEDDING_MODEL = "embeddingModel";

    /**
     * Chunk索引字段
     */
    public static final String METADATA_CHUNK_INDEX = "chunkIndex";

    // ==================== 聊天内容格式 ====================

    /**
     * 用户消息前缀
     */
    public static final String CHAT_USER_PREFIX = "User: ";

    /**
     * AI消息前缀
     */
    public static final String CHAT_AI_PREFIX = "\nAI: ";

    // ==================== 问题文本构建 ====================

    /**
     * 选项标签
     */
    public static final String QUESTION_OPTIONS_LABEL = "\n选项：\n";

    /**
     * 答案标签
     */
    public static final String QUESTION_ANSWERS_LABEL = "\n答案：\n";

    /**
     * 答案序号前缀
     */
    public static final String ANSWER_ORDER_PREFIX = "第";

    /**
     * 答案序号后缀
     */
    public static final String ANSWER_ORDER_SUFFIX = "空：";

    // ==================== 文本分片参数 ====================

    /**
     * 默认文本分片大小
     */
    public static final int DEFAULT_CHUNK_SIZE = 1000;

    /**
     * 默认文本分片重叠大小
     */
    public static final int DEFAULT_CHUNK_OVERLAP = 150;

    // ==================== 聊天向量化参数 ====================
    /**
     * 聊天内容用于向量化时的最大长度限制（字符数）。
     * DashScope 向量模型要求单条输入长度在 [1, 2048] 范围内（按 token 计），
     * 这里采用字符长度进行近似控制，预留一定余量以避免触发上游长度校验错误。
     */
    public static final int CHAT_EMBEDDING_MAX_LENGTH = 2000;

    // ==================== 标题处理 ====================

    /**
     * 标题最大长度
     */
    public static final int MAX_TITLE_LENGTH = 120;

    // ==================== 调试分数行前缀 ====================

    /**
     * 距离分数行前缀
     */
    public static final String DEBUG_DISTANCE_PREFIX = "distance:";

    /**
     * 向量分数行前缀
     */
    public static final String DEBUG_VECTOR_SCORE_PREFIX = "vector_score:";

    /**
     * 距离元数据键
     */
    public static final String METADATA_DISTANCE = "distance";

    /**
     * 向量分数元数据键
     */
    public static final String METADATA_VECTOR_SCORE = "vector_score";

    // ==================== 文件片段格式 ====================

    /**
     * 文件片段前缀
     */
    public static final String FILE_CHUNK_PREFIX = "【文件片段 ";

    /**
     * 文件片段后缀
     */
    public static final String FILE_CHUNK_SUFFIX = "】\n";

    private KnowledgeConstants() {
        // 禁止实例化
    }
}

