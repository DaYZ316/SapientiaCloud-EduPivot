package com.dayz.sapientiacloud_edupivot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationQueryDTO;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysNotification;
import com.dayz.sapientiacloud_edupivot.system.entity.vo.NotificationVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysNotificationMapper extends BaseMapper<SysNotification> {

    List<NotificationVO> listNotification(NotificationQueryDTO queryDTO);
}

