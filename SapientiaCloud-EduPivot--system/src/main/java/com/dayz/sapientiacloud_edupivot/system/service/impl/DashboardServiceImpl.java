package com.dayz.sapientiacloud_edupivot.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dayz.sapientiacloud_edupivot.system.common.clients.CourseClient;
import com.dayz.sapientiacloud_edupivot.system.common.result.Result;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysRole;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.StatisticsVO;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysRoleMapper;
import com.dayz.sapientiacloud_edupivot.system.mapper.SysUserRoleMapper;
import com.dayz.sapientiacloud_edupivot.system.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 仪表盘服务实现类
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    // 学生角色标识
    private static final String STUDENT_ROLE_KEY = "STUDENT";
    // 教师角色标识
    private static final String TEACHER_ROLE_KEY = "TEACHER";
    private final CourseClient courseClient;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public StatisticsVO getStatistics() {
        StatisticsVO statisticsVO = new StatisticsVO();

        // 获取学生总数
        Long studentCount = getUserCountByRoleKey(STUDENT_ROLE_KEY);
        statisticsVO.setStudentCount(studentCount);

        // 获取教师总数
        Long teacherCount = getUserCountByRoleKey(TEACHER_ROLE_KEY);
        statisticsVO.setTeacherCount(teacherCount);

        // 获取课程总数
        Long courseCount = getCourseCount();
        statisticsVO.setCourseCount(courseCount);

        return statisticsVO;
    }

    /**
     * 根据角色key获取用户数量
     *
     * @param roleKey 角色key
     * @return 用户数量
     */
    private Long getUserCountByRoleKey(String roleKey) {
        // 先获取角色ID
        LambdaQueryWrapper<SysRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(SysRole::getRoleKey, roleKey);
        SysRole role = sysRoleMapper.selectOne(roleWrapper);

        if (role == null) {
            return 0L;
        }

        // 根据角色ID获取用户数量
        List<UUID> userIds = sysUserRoleMapper.getUserIdsByRoleId(role.getId());
        return userIds != null ? (long) userIds.size() : 0L;
    }

    /**
     * 获取课程总数
     *
     * @return 课程总数
     */
    private Long getCourseCount() {
        try {
            Result<Long> result = courseClient.getCourseCount();
            return result.getData() != null ? result.getData() : 0L;
        } catch (Exception e) {
            // 如果调用失败，返回0
            return 0L;
        }
    }
}
