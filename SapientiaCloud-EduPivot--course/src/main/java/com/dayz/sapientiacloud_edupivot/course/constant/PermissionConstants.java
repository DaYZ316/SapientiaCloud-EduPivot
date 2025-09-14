package com.dayz.sapientiacloud_edupivot.course.constant;

public class PermissionConstants {

    public static final String QUERY = "query";
    public static final String ADD = "add";
    public static final String EDIT = "edit";
    public static final String DELETE = "delete";

    public static final String COURSE_QUERY = "manage:course:query";
    public static final String COURSE_ADD = "manage:course:add";
    public static final String COURSE_EDIT = "manage:course:edit";
    public static final String COURSE_DELETE = "manage:course:delete";

    // 课程章节权限
    public static final String CHAPTER_QUERY = "manage:chapter:query";
    public static final String CHAPTER_ADD = "manage:chapter:add";
    public static final String CHAPTER_EDIT = "manage:chapter:edit";
    public static final String CHAPTER_DELETE = "manage:chapter:delete";

    // 论坛主贴权限
    public static final String THREAD_QUERY = "manage:thread:query";
    public static final String THREAD_ADD = "manage:thread:add";
    public static final String THREAD_EDIT = "manage:thread:edit";
    public static final String THREAD_DELETE = "manage:thread:delete";

    // 论坛回复权限
    public static final String REPLY_QUERY = "manage:reply:query";
    public static final String REPLY_ADD = "manage:reply:add";
    public static final String REPLY_EDIT = "manage:reply:edit";
    public static final String REPLY_DELETE = "manage:reply:delete";

    private PermissionConstants() {
        // 禁止实例化
    }
} 