package com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseTaskEnum implements BaseEnum {

    // 通用验证错误
    TASK_REQUIRED(44001, "任务不能为空"),
    TASK_ID_REQUIRED(44002, "任务ID不能为空"),
    TASK_INFO_REQUIRED(44003, "任务信息不能为空"),
    TASK_INFO_OR_ID_REQUIRED(44004, "任务信息或ID不能为空"),
    TASK_ID_LIST_REQUIRED(44005, "任务ID列表不能为空"),
    TASK_NOT_EXISTS(44006, "任务不存在"),
    TASK_NAME_REQUIRED(44007, "任务名称不能为空"),
    TASK_DESCRIPTION_REQUIRED(44008, "任务描述不能为空"),
    TASK_CONTENT_REQUIRED(44009, "任务内容不能为空"),
    TASK_STATUS_INVALID(44010, "任务状态值无效 (0=草稿, 1=发布, 2=进行中, 3=已结束)"),

    // 课程相关错误
    COURSE_ID_REQUIRED(44011, "课程ID不能为空"),
    COURSE_NOT_EXISTS(44012, "课程不存在"),

    // 用户相关错误
    USER_ID_REQUIRED(44013, "用户ID不能为空"),
    USER_NOT_EXISTS(44014, "用户不存在"),

    // 任务类型相关错误
    TASK_TYPE_INVALID(44015, "任务类型值无效 (0=作业, 1=测验, 2=项目, 3=实验)"),
    TASK_TYPE_REQUIRED(44016, "任务类型不能为空"),

    // 时间相关错误
    START_TIME_REQUIRED(44017, "开始时间不能为空"),
    END_TIME_REQUIRED(44018, "结束时间不能为空"),
    START_TIME_INVALID(44019, "开始时间格式无效"),
    END_TIME_INVALID(44020, "结束时间格式无效"),
    END_TIME_BEFORE_START_TIME(44021, "结束时间不能早于开始时间"),

    // 分数相关错误
    MAX_SCORE_INVALID(44022, "满分必须大于0"),
    MAX_SCORE_REQUIRED(44023, "满分不能为空"),

    // 提交相关错误
    MAX_SUBMIT_COUNT_INVALID(44024, "最大提交次数不能小于0"),
    ALLOW_LATE_SUBMIT_INVALID(44025, "迟交设置值无效 (0=不允许, 1=允许)"),
    AUTO_GRADE_INVALID(44026, "自动评分设置值无效 (0=手动评分, 1=自动评分)"),

    // 难度相关错误
    DIFFICULTY_INVALID(44027, "难度等级值无效 (1=简单, 2=中等, 3=困难)"),
    ESTIMATED_TIME_INVALID(44028, "预计完成时间必须大于0"),

    // 统计相关错误
    VIEW_COUNT_INVALID(44029, "浏览次数无效"),

    // 权限相关错误
    NO_PERMISSION_TO_CREATE(44030, "没有创建任务权限"),
    NO_PERMISSION_TO_EDIT(44031, "没有编辑任务权限"),
    NO_PERMISSION_TO_DELETE(44032, "没有删除任务权限"),
    NO_PERMISSION_TO_PUBLISH(44033, "没有发布任务权限"),

    // 分页相关错误
    PAGE_NUM_REQUIRED(44034, "页码不能为空"),
    PAGE_SIZE_REQUIRED(44035, "页面大小不能为空");

    private final int code;

    private final String message;
}
