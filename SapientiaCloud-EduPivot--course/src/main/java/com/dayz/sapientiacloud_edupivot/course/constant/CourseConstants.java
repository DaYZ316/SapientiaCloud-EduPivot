package com.dayz.sapientiacloud_edupivot.course.constant;

import com.dayz.sapientiacloud_edupivot.course.enums.PublicStatusEnum;

/**
 * 课程模块常量定义
 */
public final class CourseConstants {

    private CourseConstants() {
    }

    /**
     * 课程公开状态最小值（0=仅课程成员可见）
     */
    public static final int IS_PUBLIC_MIN = PublicStatusEnum.COURSE_MEMBERS_ONLY.getCode();

    /**
     * 课程公开状态最大值（1=公开）
     */
    public static final int IS_PUBLIC_MAX = PublicStatusEnum.PUBLIC.getCode();

    /**
     * 课程公开状态默认值（仅课程成员可见）
     */
    public static final int DEFAULT_IS_PUBLIC = PublicStatusEnum.COURSE_MEMBERS_ONLY.getCode();
}


