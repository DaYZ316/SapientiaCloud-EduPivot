package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.ThreadReplyDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.ThreadReplyQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.ThreadReplyVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface IThreadReplyService {

    PageInfo<ThreadReplyVO> listThreadReply(ThreadReplyQueryDTO threadReplyQueryDTO);

    List<ThreadReplyVO> listThreadReplyTreeByThreadId(UUID courseId);

    ThreadReplyVO getThreadReplyById(UUID replyId);

    ThreadReplyVO addThreadReply(ThreadReplyDTO threadReplyDTO);

    Boolean updateThreadReply(ThreadReplyDTO threadReplyDTO);

    Boolean removeThreadReplyById(UUID replyId);

    Integer removeThreadReplyByIds(List<UUID> replyIds);
}
