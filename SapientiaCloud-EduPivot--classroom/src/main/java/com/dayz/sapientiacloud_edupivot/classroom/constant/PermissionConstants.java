package com.dayz.sapientiacloud_edupivot.classroom.constant;

public class PermissionConstants {

    // 课程记录管理权限
    public static final String COURSE_RECORD_QUERY = "classroom:record:query";
    public static final String COURSE_RECORD_ADD = "classroom:record:add";
    public static final String COURSE_RECORD_EDIT = "classroom:record:edit";
    public static final String COURSE_RECORD_DELETE = "classroom:record:delete";

    // 学生座位管理权限
    public static final String COURSE_RECORD_STUDENT_QUERY = "classroom:record:student:query";
    public static final String COURSE_RECORD_STUDENT_ADD = "classroom:record:student:add";
    public static final String COURSE_RECORD_STUDENT_EDIT = "classroom:record:student:edit";
    public static final String COURSE_RECORD_STUDENT_DELETE = "classroom:record:student:delete";

    private PermissionConstants() {
        // 禁止实例化
    }
}
