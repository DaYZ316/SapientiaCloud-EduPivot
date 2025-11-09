package com.dayz.sapientiacloud_edupivot.course.common.exception;

import com.dayz.sapientiacloud_edupivot.course.common.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.course.common.enums.SysPermissionEnum;
import com.dayz.sapientiacloud_edupivot.course.common.enums.SysRoleEnum;
import com.dayz.sapientiacloud_edupivot.course.common.enums.SysUserEnum;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.utils.EnumUtil;
import com.dayz.sapientiacloud_edupivot.course.enums.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Objects;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String USER = "user";

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        SysUserEnum sysUserEnum = EnumUtil.getByAttribute(SysUserEnum.class, e.getMessage(), SysUserEnum::getMessage);
        if (sysUserEnum != null) {
            return Result.fail(sysUserEnum.getMessage());
        }
        SysRoleEnum sysRoleEnum = EnumUtil.getByAttribute(SysRoleEnum.class, e.getMessage(), SysRoleEnum::getMessage);
        if (sysRoleEnum != null) {
            return Result.fail(sysRoleEnum.getMessage());
        }
        SysPermissionEnum sysPermissionEnum = EnumUtil.getByAttribute(SysPermissionEnum.class, e.getMessage(), SysPermissionEnum::getMessage);
        if (sysPermissionEnum != null) {
            return Result.fail(sysPermissionEnum.getMessage());
        }
        CourseEnum courseEnum = EnumUtil.getByAttribute(CourseEnum.class, e.getMessage(), CourseEnum::getMessage);
        if (courseEnum != null) {
            return Result.fail(courseEnum.getMessage());
        }
        CourseChapterEnum courseChapterEnum = EnumUtil.getByAttribute(CourseChapterEnum.class, e.getMessage(), CourseChapterEnum::getMessage);
        if (courseChapterEnum != null) {
            return Result.fail(courseChapterEnum.getMessage());
        }
        CourseForumEnum courseForumEnum = EnumUtil.getByAttribute(CourseForumEnum.class, e.getMessage(), CourseForumEnum::getMessage);
        if (courseForumEnum != null) {
            return Result.fail(courseForumEnum.getMessage());
        }
        ForumPostEnum forumPostEnum = EnumUtil.getByAttribute(ForumPostEnum.class, e.getMessage(), ForumPostEnum::getMessage);
        if (forumPostEnum != null) {
            return Result.fail(forumPostEnum.getMessage());
        }
        ForumReplyEnum forumReplyEnum = EnumUtil.getByAttribute(ForumReplyEnum.class, e.getMessage(), ForumReplyEnum::getMessage);
        if (forumReplyEnum != null) {
            return Result.fail(forumReplyEnum.getMessage());
        }
        CourseTaskEnum courseTaskEnum = EnumUtil.getByAttribute(CourseTaskEnum.class, e.getMessage(), CourseTaskEnum::getMessage);
        if (courseTaskEnum != null) {
            return Result.fail(courseTaskEnum.getMessage());
        }
        CourseQuestionBankEnum courseQuestionBankEnum = EnumUtil.getByAttribute(CourseQuestionBankEnum.class, e.getMessage(), CourseQuestionBankEnum::getMessage);
        if (courseQuestionBankEnum != null) {
            return Result.fail(courseQuestionBankEnum.getMessage());
        }
        QuestionEnum questionEnum = EnumUtil.getByAttribute(QuestionEnum.class, e.getMessage(), QuestionEnum::getMessage);
        if (questionEnum != null) {
            return Result.fail(questionEnum.getMessage());
        }
        QuestionAnswerEnum questionAnswerEnum = EnumUtil.getByAttribute(QuestionAnswerEnum.class, e.getMessage(), QuestionAnswerEnum::getMessage);
        if (questionAnswerEnum != null) {
            return Result.fail(questionAnswerEnum.getMessage());
        }
        QuestionOptionEnum questionOptionEnum = EnumUtil.getByAttribute(QuestionOptionEnum.class, e.getMessage(), QuestionOptionEnum::getMessage);
        if (questionOptionEnum != null) {
            return Result.fail(questionOptionEnum.getMessage());
        }
        ResultEnum resultEnum = EnumUtil.getByAttribute(ResultEnum.class, e.getMessage(), ResultEnum::getMessage);
        return Result.fail(Objects.requireNonNullElse(resultEnum, ResultEnum.SYSTEM_ERROR));
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException() {
        return Result.fail(ResultEnum.SYSTEM_ERROR);
    }

    /**
     * 处理权限异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<String> handleAccessDeniedException() {
        return Result.fail(ResultEnum.FORBIDDEN);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        return Result.fail("参数验证失败: " + e.getMessage());
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Map<String, String>> handleBindException(BindException e) {
        return Result.fail("参数绑定失败:" + e.getMessage());
    }
} 