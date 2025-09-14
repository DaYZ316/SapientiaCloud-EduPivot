package com.dayz.sapientiacloud_edupivot.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.ThreadReplyQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.ThreadReply;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.ThreadReplyVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ThreadReplyMapper extends BaseMapper<ThreadReply> {

    List<ThreadReplyVO> listThreadReply(ThreadReplyQueryDTO threadReplyQueryDTO);

    List<ThreadReplyVO> listThreadReplyTreeByThreadId(UUID threadId);

    ThreadReplyVO getThreadReplyById(UUID replyId);

    Boolean addThreadReply(ThreadReply threadReply);

    Boolean updateThreadReply(ThreadReply threadReply);

    Boolean removeThreadReplyById(UUID replyId);

    Integer removeThreadReplyByIds(List<UUID> replyIds);
}
