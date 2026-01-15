package com.dayz.sapientiacloud_edupivot.classroom.enums;

import com.dayz.sapientiacloud_edupivot.classroom.common.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseRecordStudentEnum implements BaseEnum {

    // 通用验证错误
    COURSE_RECORD_STUDENT_REQUIRED(51001, "学生座位记录不能为空"),
    COURSE_RECORD_STUDENT_NOT_EXISTS(51002, "学生座位记录不存在"),
    COURSE_RECORD_STUDENT_INFO_REQUIRED(51003, "学生座位信息不能为空"),

    // ID相关
    RECORD_ID_REQUIRED(51004, "课程记录ID不能为空"),
    STUDENT_ID_REQUIRED(51005, "学生ID不能为空"),
    COURSE_ID_REQUIRED(51006, "课程ID不能为空"),
    RECORD_ID_AND_STUDENT_ID_REQUIRED(51007, "课程记录ID和学生ID不能为空"),

    // 学生资格验证
    STUDENT_NOT_EXISTS(51008, "学生不存在"),
    STUDENT_NOT_ENROLLED(51009, "学生未选修该课程，无法进入教室"),
    STUDENT_NOT_IN_LIST(51010, "学生不在应到名单中"),
    STUDENT_ALREADY_SEATED(51011, "学生已经选座，请勿重复操作"),

    // 座位相关
    SEAT_INDEX_REQUIRED(51012, "座位编号不能为空"),
    SEAT_INDEX_INVALID(51013, "座位编号无效"),
    SEAT_ALREADY_OCCUPIED(51014, "该座位已被占用，请选择其他座位"),
    SEAT_NOT_AVAILABLE(51015, "该座位不可用"),
    LOCATION_REQUIRED(51016, "座位坐标不能为空"),
    LOCATION_INVALID(51017, "座位坐标无效"),

    // 座位状态相关
    SEAT_STATUS_INVALID(51018, "座位状态值无效 (0=正常, 2=已预留, 3=已占用)"),

    // 课程记录状态相关
    COURSE_RECORD_NOT_EXISTS(51019, "课程记录不存在"),
    COURSE_RECORD_NOT_STARTED(51020, "课程尚未开始，无法选座"),
    COURSE_RECORD_ENDED(51021, "课程已结束，无法选座"),
    COURSE_RECORD_CANCELLED(51022, "课程已取消"),

    // 删除相关
    CANNOT_REMOVE_DURING_CLASS(51023, "课程进行中，无法移除学生"),

    // 批量操作相关
    BATCH_LIST_REQUIRED(51024, "批量操作列表不能为空"),
    BATCH_LIST_EMPTY(51025, "批量操作列表为空"),

    // 座位容量相关
    CLASSROOM_FULL(51026, "教室座位已满"),
    SEAT_COUNT_EXCEEDED(51027, "座位数量超过教室容量");

    private final int code;
    private final String message;
}
