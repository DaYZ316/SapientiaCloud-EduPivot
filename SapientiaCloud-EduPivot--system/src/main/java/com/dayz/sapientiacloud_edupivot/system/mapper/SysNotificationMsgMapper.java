package com.dayz.sapientiacloud_edupivot.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.system.entity.po.SysNotificationMsg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysNotificationMsgMapper extends BaseMapper<SysNotificationMsg> {
    // 分页查询已发送的消息（按消息正文聚合接收/已读统计）
    java.util.List<com.dayz.sapientiacloud_edupivot.system.entity.vo.NotificationVO> listSentNotificationVO(@Param("query") com.dayz.sapientiacloud_edupivot.system.entity.dto.NotificationQueryDTO query);
}
