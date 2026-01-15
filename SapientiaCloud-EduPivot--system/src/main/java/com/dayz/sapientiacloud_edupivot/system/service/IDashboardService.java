package com.dayz.sapientiacloud_edupivot.system.service;

import com.dayz.sapientiacloud_edupivot.system.entity.vo.StatisticsVO;

/**
 * 仪表盘服务接口
 */
public interface IDashboardService {

    /**
     * 获取统计数据
     *
     * @return 统计数据
     */
    StatisticsVO getStatistics();
}
