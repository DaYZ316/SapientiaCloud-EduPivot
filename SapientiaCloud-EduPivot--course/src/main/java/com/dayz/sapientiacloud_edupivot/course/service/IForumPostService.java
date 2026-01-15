package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.ForumPostDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.ForumPostQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.ForumPostVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface IForumPostService {

    PageInfo<ForumPostVO> listForumPost(ForumPostQueryDTO forumPostQueryDTO);

    List<ForumPostVO> listAllForumPostByForumId(UUID forumId);

    List<ForumPostVO> listAllForumPostByCourseId(UUID courseId);

    List<ForumPostVO> listAllForumPost();

    ForumPostVO getForumPostById(UUID id);

    ForumPostVO addForumPost(ForumPostDTO forumPostDTO);

    Boolean updateForumPost(ForumPostDTO forumPostDTO);

    Boolean removeForumPostById(UUID id);

    Integer removeForumPostByIds(List<UUID> ids);

    Boolean updatePostStatus(UUID id, Integer status);

    Boolean setPostTop(UUID id, Integer isTop);

    Boolean setPostEssence(UUID id, Integer isEssence);

    Boolean setPostLock(UUID id, Integer isLocked);

    Boolean likePost(UUID id);

    Boolean unlikePost(UUID id);

    Boolean sharePost(UUID id);

    Boolean viewPost(UUID id);

    List<ForumPostVO> getHotPosts(Integer limit);

    List<ForumPostVO> getLatestPosts(Integer limit);

    Boolean updateReplyCount(UUID postId, Long replyCount);
}
