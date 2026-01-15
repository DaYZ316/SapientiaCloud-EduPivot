package com.dayz.sapientiacloud_edupivot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysNotificationUser;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.NotificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysNotificationUserMapper extends BaseMapper<SysNotificationUser> {

    /**
     * 分页查询用户通知列表（联表查询）
     */
    List<NotificationVO> listNotificationVO(@Param("query") NotificationQueryDTO query);
}
