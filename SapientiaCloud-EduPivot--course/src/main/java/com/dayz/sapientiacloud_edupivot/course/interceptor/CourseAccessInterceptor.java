package com.dayz.sapientiacloud_edupivot.course.interceptor;

import com.dayz.sapientiacloud_edupivot.course.common.clients.StudentClient;
import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.StudentVO;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.course.entity.po.Course;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseStudentVO;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseEnum;
import com.dayz.sapientiacloud_edupivot.course.enums.PublicStatusEnum;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseMapper;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseStudentMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 课程访问拦截器
 * 用于验证用户是否有权限访问课程相关接口
 *
 * @author SapientiaCloud
 * @since 2024-01-01
 */
@Slf4j
@Component
public class CourseAccessInterceptor implements HandlerInterceptor {

    // 课程ID可能出现的参数名
    private static final String[] COURSE_ID_PARAM_NAMES = {"courseId", "course_id", "id"};
    // UUID正则表达式
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );
    private final CourseMapper courseMapper;
    private final CourseStudentMapper courseStudentMapper;
    private final StudentClient studentClient;
    public CourseAccessInterceptor(
            CourseMapper courseMapper,
            CourseStudentMapper courseStudentMapper,
            @Lazy StudentClient studentClient) {
        this.courseMapper = courseMapper;
        this.courseStudentMapper = courseStudentMapper;
        this.studentClient = studentClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 提取课程ID
        UUID courseId = extractCourseId(request);

        // 如果没有课程ID，说明不是课程相关接口，直接放行
        if (courseId == null) {
            return true;
        }

        // 如果是管理员，直接放行
        if (UserContextUtil.isAdmin()) {
            log.debug("管理员访问课程: {}", courseId);
            return true;
        }

        // 查询课程信息
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(CourseEnum.COURSE_NOT_EXISTS.getMessage());
        }

        // 如果课程是公开的，直接放行
        if (course.getIsPublic() != null && course.getIsPublic().equals(PublicStatusEnum.PUBLIC.getCode())) {
            log.debug("公开课程访问: {}", courseId);
            return true;
        }

        // 课程是私有的，需要验证学生身份
        // 获取当前用户ID
        UUID currentUserId = UserContextUtil.getCurrentUserId();

        // 从学生表中查询学生信息
        Result<StudentVO> studentResult = studentClient.getStudentByUserId(currentUserId);
        if (studentResult == null || !studentResult.isSuccess() || studentResult.getData() == null) {
            log.warn("用户 {} 不是学生，无法访问私有课程 {}", currentUserId, courseId);
            throw new BusinessException(CourseEnum.COURSE_NOT_PUBLIC_AND_NOT_STUDENT.getMessage());
        }

        StudentVO student = studentResult.getData();
        UUID studentId = student.getId();

        // 在课程学生表中查询用户对应课程中的学生是否存在
        CourseStudentVO courseStudent = courseStudentMapper.getStudentCourseById(studentId, courseId);
        if (courseStudent == null) {
            log.warn("学生 {} 不是课程 {} 的学生，无法访问", studentId, courseId);
            throw new BusinessException(CourseEnum.COURSE_ACCESS_DENIED.getMessage());
        }

        log.debug("学生 {} 访问课程 {} 验证通过", studentId, courseId);
        return true;
    }

    /**
     * 从请求中提取课程ID
     * 优先从路径参数和查询参数中提取，避免读取请求体影响后续处理
     *
     * @param request HTTP请求
     * @return 课程ID，如果未找到则返回null
     */
    private UUID extractCourseId(HttpServletRequest request) {
        // 1. 从路径参数中提取（如 /course/{id} 或 /chapter/course/{courseId}）
        UUID courseId = extractFromPath(request);
        if (courseId != null) {
            return courseId;
        }

        // 2. 从查询参数中提取（如 ?courseId=xxx）
        courseId = extractFromQueryParams(request);
        if (courseId != null) {
            return courseId;
        }

        // 注意：不读取请求体，因为会消费流，影响后续Controller处理
        // 如果需要在请求体中传递课程ID，建议使用路径参数或查询参数
        // 或者使用ContentCachingRequestWrapper在Filter中包装请求

        return null;
    }

    /**
     * 从路径参数中提取课程ID
     */
    private UUID extractFromPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        // 尝试匹配路径中的UUID
        java.util.regex.Matcher matcher = UUID_PATTERN.matcher(path);
        if (matcher.find()) {
            try {
                return UUID.fromString(matcher.group());
            } catch (IllegalArgumentException e) {
                log.debug("路径中的UUID格式无效: {}", matcher.group());
            }
        }
        return null;
    }

    /**
     * 从查询参数中提取课程ID
     */
    private UUID extractFromQueryParams(HttpServletRequest request) {
        for (String paramName : COURSE_ID_PARAM_NAMES) {
            String paramValue = request.getParameter(paramName);
            if (StringUtils.hasText(paramValue)) {
                try {
                    return UUID.fromString(paramValue);
                } catch (IllegalArgumentException e) {
                    log.debug("查询参数 {} 的值不是有效的UUID: {}", paramName, paramValue);
                }
            }
        }
        return null;
    }

}

