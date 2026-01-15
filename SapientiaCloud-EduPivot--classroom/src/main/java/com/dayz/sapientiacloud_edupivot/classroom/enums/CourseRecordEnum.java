package com.dayz.sapientiacloud_edupivot.classroom.enums;

import com.dayz.sapientiacloud_edupivot.classroom.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseRecordEnum implements BaseEnum {

    // 通用验证错误
    COURSE_RECORD_REQUIRED(50001, "课程记录不能为空"),
    COURSE_RECORD_ID_REQUIRED(50002, "课程记录ID不能为空"),
    COURSE_RECORD_NOT_EXISTS(50003, "课程记录不存在"),
    COURSE_RECORD_INFO_REQUIRED(50004, "课程记录信息不能为空"),
    COURSE_RECORD_INFO_OR_ID_REQUIRED(50005, "课程记录信息或ID不能为空"),
    COURSE_RECORD_ID_LIST_REQUIRED(50006, "课程记录ID列表不能为空"),

    // 课程记录状态相关
    COURSE_RECORD_ALREADY_STARTED(50007, "课程已开始，无法修改"),
    COURSE_RECORD_ALREADY_ENDED(50008, "课程已结束，无法修改"),
    COURSE_RECORD_NOT_STARTED(50009, "课程尚未开始"),
    COURSE_RECORD_STATUS_INVALID(50010, "课程状态值无效 (0=未开始, 1=进行中, 2=已结束, 3=取消)"),

    // 课程和教师相关
    COURSE_ID_REQUIRED(50011, "课程ID不能为空"),
    COURSE_NOT_EXISTS(50012, "课程不存在"),
    TEACHER_ID_REQUIRED(50013, "教师ID不能为空"),
    TEACHER_NOT_EXISTS(50014, "教师不存在"),

    // 教室配置相关
    CLASSROOM_TYPE_REQUIRED(50015, "教室类型不能为空"),
    CLASSROOM_TYPE_INVALID(50016, "教室类型值无效 (0=小型教室, 1=中型教室, 2=大型教室, 3=超大型教室)"),

    // 学生相关
    STUDENT_IDS_EMPTY(50020, "学生ID列表为空"),
    NO_ENROLLED_STUDENTS(50021, "该课程没有选课学生"),

    // 删除相关
    COURSE_RECORD_HAS_STUDENTS(50022, "课程记录中还有学生座位记录，无法删除"),
    COURSE_RECORD_IN_PROGRESS(50023, "课程正在进行中，无法删除"),

    // 时间相关
    START_TIME_REQUIRED(50024, "开始时间不能为空"),
    END_TIME_BEFORE_START(50025, "结束时间不能早于开始时间"),

    // 分页相关
    PAGE_NUM_REQUIRED(50026, "页码不能为空"),
    PAGE_SIZE_REQUIRED(50027, "页面大小不能为空");

    private final int code;
    private final String message;
}
