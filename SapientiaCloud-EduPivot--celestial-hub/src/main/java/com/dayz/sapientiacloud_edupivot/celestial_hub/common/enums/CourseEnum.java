package com.dayz.sapientiacloud_edupivot.celestial_hub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseEnum implements BaseEnum {

    // 通用验证错误
    COURSE_REQUIRED(40001, "课程不能为空"),
    COURSE_ID_REQUIRED(40002, "课程ID不能为空"),
    COURSE_INFO_REQUIRED(40003, "课程信息不能为空"),
    COURSE_INFO_OR_ID_REQUIRED(40004, "课程信息或ID不能为空"),
    COURSE_ID_LIST_REQUIRED(40005, "课程ID列表不能为空"),
    COURSE_NOT_EXISTS(40006, "课程不存在"),
    COURSE_NAME_EXISTS(40007, "课程名称已存在"),
    COURSE_STATUS_INVALID(40008, "课程状态值无效 (0=正常, 1=停课)"),

    // 教师相关错误
    TEACHER_ID_REQUIRED(40009, "教师ID不能为空"),

    // 学生相关错误
    STUDENT_ID_REQUIRED(40010, "学生ID不能为空"),
    STUDENT_ID_AND_COURSE_ID_REQUIRED(40011, "学生ID和课程ID不能为空"),

    // 选课相关错误
    ENROLLMENT_INFO_INCOMPLETE(40012, "选课信息不完整"),
    ALREADY_ENROLLED(40013, "您已经选过这门课程"),
    COURSE_NOT_AVAILABLE(40014, "课程当前状态不可选"),
    ENROLLMENT_RECORD_NOT_FOUND(40015, "未找到有效的选课记录"),
    ENROLLMENT_RECORD_NOT_EXISTS(40016, "未找到选课记录"),

    // 成绩相关错误
    GRADE_INVALID_RANGE(40017, "成绩必须在0-100之间"),
    GRADE_UPDATE_LIST_REQUIRED(40018, "成绩更新列表不能为空"),

    // 分页相关错误
    PAGE_NUM_REQUIRED(40019, "页码不能为空"),
    PAGE_SIZE_REQUIRED(40020, "页面大小不能为空"),

    // 教师团队相关错误
    TEACHER_ID_LIST_REQUIRED(40021, "教师ID列表不能为空"),

    // 课程删除相关错误
    COURSE_HAS_STUDENTS(40022, "课程中还有学生，无法删除");

    private final int code;

    private final String message;
}
