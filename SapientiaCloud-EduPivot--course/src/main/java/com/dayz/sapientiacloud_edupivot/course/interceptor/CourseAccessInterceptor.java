package com.dayz.sapientiacloud_edupivot.course.interceptor;

import com.dayz.sapientiacloud_edupivot.course.common.enums.ResultEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseVO;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseEnum;
import com.dayz.sapientiacloud_edupivot.course.enums.PublicStatusEnum;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseAssistantTeacherMapper;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseMapper;
import com.dayz.sapientiacloud_edupivot.course.mapper.CourseStudentMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.UUID;

/**
 * 课程访问权限拦截器
 * 检查用户对课程的访问权限
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseAccessInterceptor implements HandlerInterceptor {

    private final CourseMapper courseMapper;
    private final CourseAssistantTeacherMapper courseAssistantTeacherMapper;
    private final CourseStudentMapper courseStudentMapper;

    /**
     * 从请求路径中提取课程ID
     * 只有当路径中包含/course/段落时，才会提取后续的UUID作为courseId
     * 支持的路径模式：
     * - /course/{courseId}
     * - /course/{courseId}/...
     * - 其他包含课程ID的参数
     */
    private UUID extractCourseId(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String[] pathSegments = requestURI.split("/");

        // 检查是否为course相关的路径模式
        for (int i = 0; i < pathSegments.length - 1; i++) {
            if ("course".equals(pathSegments[i])) {
                // 找到course段落，后一个段落应该是courseId
                String potentialCourseId = pathSegments[i + 1];
                try {
                    return UUID.fromString(potentialCourseId);
                } catch (IllegalArgumentException e) {
                    // 不是有效的UUID，继续查找
                }
            }
        }

        // 检查查询参数
        String courseIdParam = request.getParameter("courseId");
        if (courseIdParam != null) {
            try {
                return UUID.fromString(courseIdParam);
            } catch (IllegalArgumentException e) {
                // 参数不是有效的UUID
            }
        }

        return null;
    }

    /**
     * 判断是否为列表查询请求
     * CourseController中包含list的方法和CourseTeacherController中的listCourseByTeacherId不进行拦截
     */
    private boolean isListRequest(String requestURI, String requestMethod) {
        // 只对GET请求进行判断
        if (!"GET".equalsIgnoreCase(requestMethod)) {
            return false;
        }

        // 检查是否为CourseController中的列表查询接口
        if (requestURI.endsWith("/list") ||
            requestURI.endsWith("/all") ||
            requestURI.contains("/public/list")) {
            return true;
        }

        // 检查是否为CourseTeacherController中的listCourseByTeacherId接口
        return requestURI.endsWith("/course-teacher/teacher");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        // 不拦截列表查询相关接口
        if (isListRequest(requestURI, requestMethod)) {
            return true;
        }

        UUID courseId = extractCourseId(request);
        if (courseId == null) {
            // 如果没有找到课程ID，则不进行拦截
            return true;
        }

        // 获取课程信息
        CourseVO courseVO = courseMapper.getCourseById(courseId);
        if (courseVO == null) {
            throw new BusinessException(CourseEnum.COURSE_NOT_EXISTS);
        }

        // 如果课程为公开，直接放行
        if (courseVO.getIsPublic().equals(PublicStatusEnum.PUBLIC.getCode())) {
            return true;
        }

        // 课程为私有，进行权限检查
        return checkPrivateCourseAccess(courseId);
    }

    /**
     * 检查私有课程的访问权限
     */
    private boolean checkPrivateCourseAccess(UUID courseId) {
        try {
            // 获取当前用户信息
            UUID currentUserId = UserContextUtil.getCurrentUserId();
            List<String> userRoles = UserContextUtil.getCurrentUserRoles();

            // 1. 检查是否为管理员
            if (UserContextUtil.isAdmin()) {
                log.debug("管理员访问私有课程: courseId={}, userId={}", courseId, currentUserId);
                return true;
            }

            // 2. 检查是否为教师角色并在辅助教师列表中
            if (userRoles.contains("TEACHER") || userRoles.contains("teacher")) {
                List<UUID> assistantTeacherIds = courseAssistantTeacherMapper.listAssistantIdsByCourseId(courseId);
                if (assistantTeacherIds.contains(currentUserId)) {
                    log.debug("教师访问私有课程: courseId={}, userId={}", courseId, currentUserId);
                    return true;
                } else {
                    log.warn("教师无权限访问私有课程: courseId={}, userId={}", courseId, currentUserId);
                    throw new BusinessException(ResultEnum.FORBIDDEN.getMessage());
                }
            }

            // 3. 检查是否为学生角色并在课程学生列表中
            if (userRoles.contains("STUDENT") || userRoles.contains("student")) {
                boolean isStudentInCourse = courseStudentMapper.listAllCourseStudentByCourseId(courseId)
                        .stream()
                        .anyMatch(student -> currentUserId.equals(student.getStudentId()));
                if (isStudentInCourse) {
                    log.debug("学生访问私有课程: courseId={}, userId={}", courseId, currentUserId);
                    return true;
                } else {
                    log.warn("学生无权限访问私有课程: courseId={}, userId={}", courseId, currentUserId);
                    throw new BusinessException(ResultEnum.FORBIDDEN.getMessage());
                }
            }

            // 其他角色无权限访问
            log.warn("用户无权限访问私有课程: courseId={}, userId={}, roles={}", courseId, currentUserId, userRoles);
            throw new BusinessException(ResultEnum.FORBIDDEN.getMessage());

        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            }
            log.error("检查课程访问权限时发生错误: courseId={}, error={}", courseId, e.getMessage(), e);
            throw new BusinessException("权限检查失败");
        }
    }
}
