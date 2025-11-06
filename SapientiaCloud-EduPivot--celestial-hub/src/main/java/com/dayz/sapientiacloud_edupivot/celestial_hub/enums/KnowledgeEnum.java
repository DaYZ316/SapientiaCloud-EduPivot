package com.dayz.sapientiacloud_edupivot.celestial_hub.enums;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KnowledgeEnum implements BaseEnum {

    // 向量化相关错误
    VECTORIZE_REQUEST_REQUIRED(50101, "向量化请求不能为空"),
    VECTORIZE_FAILED(50102, "向量化失败"),
    VECTORIZE_NO_CONTENT(50103, "没有找到需要向量化的内容"),
    FETCH_CONTENT_FAILED(50104, "获取内容失败"),

    // 检索相关错误
    SEARCH_QUERY_REQUIRED(50111, "检索关键词不能为空"),
    SEARCH_FAILED(50112, "知识检索失败"),

    // 向量数据相关错误
    VECTOR_NOT_EXISTS(50121, "向量数据不存在"),
    VECTOR_DELETE_FAILED(50122, "向量数据删除失败"),

    // 参数相关错误
    COURSE_ID_REQUIRED(50131, "课程ID不能为空"),
    CHAPTER_ID_REQUIRED(50132, "章节ID不能为空"),
    CONTENT_TYPE_INVALID(50133, "内容类型无效");

    private final int code;

    private final String message;
}

