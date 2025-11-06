package com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseStudentEnum implements BaseEnum {

    // 通用验证错误
    COURSE_STUDENT_REQUIRED(45001, "选课信息不能为空"),
    COURSE_STUDENT_ID_REQUIRED(45002, "选课ID不能为空"),
    COURSE_STUDENT_INFO_REQUIRED(45003, "选课信息不能为空"),
    COURSE_STUDENT_INFO_OR_ID_REQUIRED(45004, "选课信息或ID不能为空"),
    COURSE_STUDENT_ID_LIST_REQUIRED(45005, "选课ID列表不能为空"),
    COURSE_STUDENT_NOT_EXISTS(45006, "选课记录不存在"),
    COURSE_STUDENT_STATUS_INVALID(45007, "选课状态值无效"),

    // 课程相关错误
    COURSE_ID_REQUIRED(45008, "课程ID不能为空"),
    COURSE_NOT_EXISTS(45009, "课程不存在"),

    // 学生相关错误
    STUDENT_ID_REQUIRED(45010, "学生ID不能为空"),
    STUDENT_NOT_EXISTS(45011, "学生不存在"),

    // 选课相关错误
    ENROLLMENT_INFO_INCOMPLETE(45012, "选课信息不完整"),
    ALREADY_ENROLLED(45013, "您已经选过这门课程"),
    COURSE_NOT_AVAILABLE(45014, "课程当前状态不可选"),
    ENROLLMENT_RECORD_NOT_FOUND(45015, "未找到有效的选课记录"),
    ENROLLMENT_RECORD_NOT_EXISTS(45016, "未找到选课记录"),

    // 成绩相关错误
    GRADE_INVALID_RANGE(45017, "成绩必须在0-100之间"),
    GRADE_UPDATE_LIST_REQUIRED(45018, "成绩更新列表不能为空"),

    // 权限相关错误
    NO_PERMISSION_TO_ENROLL(45019, "没有选课权限"),
    NO_PERMISSION_TO_DROP(45020, "没有退课权限"),
    NO_PERMISSION_TO_VIEW_GRADE(45021, "没有查看成绩权限"),
    NO_PERMISSION_TO_UPDATE_GRADE(45022, "没有更新成绩权限"),

    // 选课限制相关错误
    COURSE_FULL(45023, "课程已满员"),
    ENROLLMENT_PERIOD_EXPIRED(45024, "选课时间已过期"),
    PREREQUISITE_NOT_MET(45025, "未满足先修课程要求"),

    // 分页相关错误
    PAGE_NUM_REQUIRED(45026, "页码不能为空"),
    PAGE_SIZE_REQUIRED(45027, "页面大小不能为空");

    private final int code;

    private final String message;
}
