package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.ForumReplyDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.ForumReplyQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.ForumReplyVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface IForumReplyService {

    PageInfo<ForumReplyVO> listForumReply(ForumReplyQueryDTO forumReplyQueryDTO);

    PageInfo<ForumReplyVO> listForumReplyByPostId(UUID postId, ForumReplyQueryDTO forumReplyQueryDTO);

    PageInfo<ForumReplyVO> listForumReplyByForumId(UUID forumId, ForumReplyQueryDTO forumReplyQueryDTO);

    PageInfo<ForumReplyVO> listForumReplyByCourseId(UUID courseId, ForumReplyQueryDTO forumReplyQueryDTO);

    ForumReplyVO getForumReplyById(UUID id);

    ForumReplyVO addForumReply(ForumReplyDTO forumReplyDTO);

    Boolean updateForumReply(ForumReplyDTO forumReplyDTO);

    Boolean removeForumReplyById(UUID id);

    Integer removeForumReplyByIds(List<UUID> ids);

    Boolean updateReplyStatus(UUID id, Integer status);

    Boolean likeReply(UUID id);

    Boolean unlikeReply(UUID id);

    Boolean acceptReply(UUID id);

    Boolean unacceptReply(UUID id);

    List<ForumReplyVO> getReplyTree(UUID id);

    ForumReplyVO getReplyStatistics(UUID id);
}
