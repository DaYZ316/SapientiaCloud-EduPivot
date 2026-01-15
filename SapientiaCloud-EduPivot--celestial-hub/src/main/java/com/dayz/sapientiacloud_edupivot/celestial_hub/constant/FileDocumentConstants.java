package com.dayz.sapientiacloud_edupivot.celestial_hub.constant;

/**
 * 文件文档常量
 */
public class FileDocumentConstants {

    // ==================== 文件存储路径 ====================

    /**
     * 文件存储目录前缀
     */
    public static final String FILE_STORAGE_DIRECTORY_PREFIX = "file-documents/";

    /**
     * 路径分隔符
     */
    public static final String PATH_SEPARATOR = "/";

    // ==================== MinIO 响应字段 ====================

    /**
     * MinIO 上传响应中的对象名称字段
     */
    public static final String MINIO_OBJECT_NAME_FIELD = "objectName";

    // ==================== MongoDB 字段名称 ====================

    /**
     * 会话ID字段
     */
    public static final String FIELD_SESSION_ID = "session_id";

    /**
     * 课程ID字段
     */
    public static final String FIELD_COURSE_ID = "course_id";

    /**
     * 用户ID字段
     */
    public static final String FIELD_SYS_USER_ID = "sys_user_id";

    /**
     * 文件类型字段
     */
    public static final String FIELD_FILE_TYPE = "file_type";

    /**
     * 状态字段
     */
    public static final String FIELD_STATUS = "status";

    /**
     * 是否向量化字段
     */
    public static final String FIELD_IS_VECTORIZED = "is_vectorized";

    /**
     * 文件名字段
     */
    public static final String FIELD_FILE_NAME = "file_name";

    // ==================== 文件大小单位 ====================

    /**
     * 字节单位
     */
    public static final String SIZE_UNIT_BYTE = " B";

    /**
     * 千字节单位
     */
    public static final String SIZE_UNIT_KB = " KB";

    /**
     * 兆字节单位
     */
    public static final String SIZE_UNIT_MB = " MB";

    /**
     * 吉字节单位
     */
    public static final String SIZE_UNIT_GB = " GB";

    // ==================== 正则表达式标志 ====================

    /**
     * 大小写不敏感标志
     */
    public static final String REGEX_CASE_INSENSITIVE = "i";

    // ==================== 错误消息 ====================

    /**
     * 发送向量化任务失败错误消息前缀
     */
    public static final String ERROR_VECTORIZE_TASK_FAILED_PREFIX = "发送向量化任务失败: ";

    private FileDocumentConstants() {
        // 禁止实例化
    }
}

