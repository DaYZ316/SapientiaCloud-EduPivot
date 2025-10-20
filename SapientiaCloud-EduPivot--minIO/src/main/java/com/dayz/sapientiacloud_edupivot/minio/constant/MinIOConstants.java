package com.dayz.sapientiacloud_edupivot.minio.constant;

/**
 * MinIO相关常量
 *
 * @author LANDH
 */
public class MinIOConstants {

    // ==================== 错误消息常量 ====================

    // MinIO客户端相关错误消息
    public static final String MINIO_CLIENT_INIT_FAILED_MESSAGE = "MinIO客户端初始化失败";
    public static final String MINIO_BUCKET_CREATE_FAILED_MESSAGE = "创建存储桶失败";
    public static final String MINIO_GET_BUCKETS_FAILED_MESSAGE = "获取存储桶列表失败";

    // 文件上传相关错误消息
    public static final String FILE_UPLOAD_FAILED_MESSAGE = "文件上传失败";
    public static final String FILE_CANNOT_BE_EMPTY_MESSAGE = "上传文件不能为空";
    public static final String FILE_SIZE_LIMIT_EXCEEDED_MESSAGE = "文件大小超出系统限制，请压缩或分片上传";
    public static final String BYTES_UPLOAD_FAILED_MESSAGE = "字节数组上传失败";

    // 文件下载相关错误消息
    public static final String FILE_DOWNLOAD_FAILED_MESSAGE = "文件下载失败";
    public static final String FILE_NOT_FOUND_MESSAGE = "文件不存在";

    // 文件URL相关错误消息
    public static final String FILE_URL_GENERATION_FAILED_MESSAGE = "获取文件URL失败";

    // 文件删除相关错误消息
    public static final String FILE_DELETE_FAILED_MESSAGE = "删除文件失败";
    public static final String BATCH_FILE_DELETE_FAILED_MESSAGE = "批量删除文件失败";
    public static final String DELETE_FILE_BY_URL_FAILED_MESSAGE = "根据URL删除文件失败";
    public static final String BATCH_DELETE_FILES_BY_URL_FAILED_MESSAGE = "根据URL批量删除文件失败";
    public static final String FILE_DELETE_BY_URL_FAILED_MESSAGE = "根据文件路径删除文件失败";
    public static final String FILE_DELETE_BATCH_BY_URL_FAILED_MESSAGE = "根据文件路径批量删除文件失败";

    // 文件列表相关错误消息
    public static final String FILE_LIST_FAILED_MESSAGE = "列出文件失败";

    // 文件信息相关错误消息
    public static final String FILE_INFO_FAILED_MESSAGE = "获取文件信息失败";
    public static final String BATCH_FILE_INFO_FAILED_MESSAGE = "批量获取文件信息失败";
    public static final String BATCH_FILE_INFO_BY_PATH_FAILED_MESSAGE = "通过路径数组批量获取文件信息失败";
    public static final String FILE_INFO_BY_PATH_FAILED_MESSAGE = "根据文件路径获取文件信息失败";
    public static final String FILE_INFO_BATCH_FAILED_MESSAGE = "批量获取文件信息失败";
    public static final String FILE_INFO_BATCH_BY_PATHS_FAILED_MESSAGE = "根据文件路径批量获取文件信息失败";

    // 通用错误消息
    public static final String FILE_OPERATION_FAILED_MESSAGE = "文件操作失败";
    public static final String INVALID_FILE_FORMAT_MESSAGE = "无效的文件格式";
    public static final String INVALID_FILE_PATH_MESSAGE = "无效的文件路径";
    public static final String FILE_BIND_FAILED_MESSAGE = "文件绑定失败";
    public static final String INVALID_URL_FORMAT_MESSAGE = "无效的URL格式";
    public static final String UNABLE_TO_EXTRACT_OBJECT_NAME_MESSAGE = "无法从URL中提取对象名称";

    // 状态消息
    public static final String SUCCESS_STATUS = "成功";
    public static final String FAILED_STATUS = "失败";

    // ==================== 日志消息常量 ====================

    // MinIO客户端相关日志
    public static final String MINIO_CLIENT_INIT_ERROR_LOG = "MinIO客户端初始化失败: {}";
    public static final String BUCKET_CREATE_ERROR_LOG = "创建存储桶失败: {}";
    public static final String BUCKET_LIST_ERROR_LOG = "获取存储桶列表失败: {}";

    // 文件上传相关日志
    public static final String FILE_UPLOAD_ERROR_LOG = "文件上传失败: {}";
    public static final String BYTES_UPLOAD_ERROR_LOG = "字节数组上传失败: {}";

    // 文件下载相关日志
    public static final String FILE_DOWNLOAD_ERROR_LOG = "文件下载失败: {}";
    public static final String SEND_ERROR_RESPONSE_ERROR_LOG = "发送错误响应失败";

    // 文件URL相关日志
    public static final String FILE_URL_ERROR_LOG = "获取文件URL失败: {}";

    // 文件删除相关日志
    public static final String FILE_DELETE_ERROR_LOG = "删除文件失败: {}";
    public static final String BATCH_FILE_DELETE_ERROR_LOG = "批量删除文件失败: {}";
    public static final String DELETE_FILE_BY_URL_ERROR_LOG = "根据URL删除文件失败: {}";
    public static final String BATCH_DELETE_FILES_BY_URL_ERROR_LOG = "根据URL批量删除文件失败: {}";
    public static final String FILE_DELETE_BY_URL_ERROR_LOG = "根据文件路径删除文件失败: {}";
    public static final String FILE_DELETE_BATCH_BY_URL_ERROR_LOG = "根据文件路径批量删除文件失败: {}";

    // 文件列表相关日志
    public static final String FILE_LIST_ERROR_LOG = "列出文件失败: {}";

    // 文件信息相关日志
    public static final String FILE_INFO_ERROR_LOG = "获取文件信息失败: {}";
    public static final String FILE_INFO_WARNING_LOG = "获取文件信息失败: {}, 错误: {}";
    public static final String FILE_INFO_BY_PATH_WARNING_LOG = "通过路径获取文件信息失败: {}, 错误: {}";
    public static final String BATCH_FILE_INFO_ERROR_LOG = "批量获取文件信息失败: {}";
    public static final String BATCH_FILE_INFO_BY_PATH_ERROR_LOG = "通过路径数组批量获取文件信息失败: {}";
    public static final String FILE_INFO_BY_PATH_ERROR_LOG = "根据文件路径获取文件信息失败: {}";
    public static final String FILE_INFO_BATCH_ERROR_LOG = "批量获取文件信息失败: {}";
    public static final String FILE_INFO_BATCH_BY_PATHS_ERROR_LOG = "根据文件路径批量获取文件信息失败: {}";

    // URL解析相关日志
    public static final String URL_PARSE_ERROR_LOG = "解析URL失败: {}, 错误: {}";

    // 私有构造函数，防止实例化
    private MinIOConstants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }
}
