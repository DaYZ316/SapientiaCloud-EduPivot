package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.dayz.sapientiacloud_edupivot.course.common.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.SysUserVO;
import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.constant.ForumPostConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.ForumPostDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.ForumPostQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.ForumPost;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.ForumPostVO;
import com.dayz.sapientiacloud_edupivot.course.enums.ForumPostEnum;
import com.dayz.sapientiacloud_edupivot.course.repository.ForumPostRepository;
import com.dayz.sapientiacloud_edupivot.course.service.IForumPostService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageInfo;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumPostServiceImpl implements IForumPostService {

    private final ForumPostRepository forumPostRepository;
    private final MongoTemplate mongoTemplate;
    private final SysUserClient sysUserClient;

    @Override
    @Transactional(readOnly = true)
    public PageInfo<ForumPostVO> listForumPost(ForumPostQueryDTO forumPostQueryDTO) {
        if (forumPostQueryDTO == null) {
            throw new BusinessException(ForumPostEnum.POST_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();

        if (forumPostQueryDTO.getForumId() != null) {
            criteria.and(ForumPostConstants.FIELD_FORUM_ID).is(forumPostQueryDTO.getForumId());
        }

        if (forumPostQueryDTO.getCourseId() != null) {
            criteria.and(ForumPostConstants.FIELD_COURSE_ID).is(forumPostQueryDTO.getCourseId());
        }

        if (forumPostQueryDTO.getSysUserId() != null) {
            criteria.and(ForumPostConstants.FIELD_SYS_USER_ID).is(forumPostQueryDTO.getSysUserId());
        }

        if (StringUtils.hasText(forumPostQueryDTO.getTitle())) {
            criteria.and(ForumPostConstants.FIELD_TITLE).regex(forumPostQueryDTO.getTitle(), ForumPostConstants.REGEX_CASE_INSENSITIVE);
        }

        if (forumPostQueryDTO.getPostType() != null) {
            criteria.and(ForumPostConstants.FIELD_POST_TYPE).is(forumPostQueryDTO.getPostType());
        }

        if (forumPostQueryDTO.getStatus() != null) {
            criteria.and(ForumPostConstants.FIELD_STATUS).is(forumPostQueryDTO.getStatus());
        }

        criteria.and(ForumPostConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());

        query.addCriteria(criteria);
        query.with(Sort.by(
                Sort.Order.desc(ForumPostConstants.FIELD_IS_TOP),
                Sort.Order.desc(ForumPostConstants.FIELD_IS_ESSENCE),
                Sort.Order.desc(ForumPostConstants.FIELD_LIKE_COUNT),
                Sort.Order.desc(ForumPostConstants.FIELD_CREATE_TIME)
        ));

        Query countQuery = new Query();
        countQuery.addCriteria(criteria);

        Pageable pageable = PageRequest.of(
                forumPostQueryDTO.getPageNum() - ForumPostConstants.PAGE_NUM_OFFSET,
                forumPostQueryDTO.getPageSize()
        );
        query.with(pageable);

        List<ForumPost> posts = mongoTemplate.find(query, ForumPost.class);
        long total = mongoTemplate.count(countQuery, ForumPost.class);

        List<ForumPostVO> postVOList = convertToVOList(posts);

        PageInfo<ForumPostVO> pageInfo = new PageInfo<>(postVOList);
        pageInfo.setTotal(total);
        pageInfo.setPageNum(forumPostQueryDTO.getPageNum());
        pageInfo.setPageSize(forumPostQueryDTO.getPageSize());
        pageInfo.setPages((int) Math.ceil((double) total / forumPostQueryDTO.getPageSize()));

        return pageInfo;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ForumPost", key = "'forum:' + #p0", condition = "#p0 != null")
    public List<ForumPostVO> listAllForumPostByForumId(UUID forumId) {
        if (forumId == null) {
            throw new BusinessException(ForumPostEnum.FORUM_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumPostConstants.FIELD_FORUM_ID).is(forumId);
        criteria.and(ForumPostConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(
                Sort.Order.desc(ForumPostConstants.FIELD_IS_TOP),
                Sort.Order.desc(ForumPostConstants.FIELD_IS_ESSENCE),
                Sort.Order.desc(ForumPostConstants.FIELD_LIKE_COUNT),
                Sort.Order.desc(ForumPostConstants.FIELD_CREATE_TIME)
        ));

        List<ForumPost> posts = mongoTemplate.find(query, ForumPost.class);
        return convertToVOList(posts);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ForumPost", key = "'course:' + #p0", condition = "#p0 != null")
    public List<ForumPostVO> listAllForumPostByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(ForumPostEnum.COURSE_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumPostConstants.FIELD_COURSE_ID).is(courseId);
        criteria.and(ForumPostConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(
                Sort.Order.desc(ForumPostConstants.FIELD_IS_TOP),
                Sort.Order.desc(ForumPostConstants.FIELD_IS_ESSENCE),
                Sort.Order.desc(ForumPostConstants.FIELD_LIKE_COUNT),
                Sort.Order.desc(ForumPostConstants.FIELD_CREATE_TIME)
        ));

        List<ForumPost> posts = mongoTemplate.find(query, ForumPost.class);
        return convertToVOList(posts);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ForumPost", key = "#p0", condition = "#p0 != null")
    public ForumPostVO getForumPostById(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumPostConstants.FIELD_ID).is(id);
        criteria.and(ForumPostConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        ForumPost post = mongoTemplate.findOne(query, ForumPost.class);
        if (post == null) {
            throw new BusinessException(ForumPostEnum.POST_NOT_EXISTS);
        }

        return convertToVO(post);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumPost", key = "'forum:' + #p0.forumId"),
            @CacheEvict(value = "ForumPost", key = "'course:' + #p0.courseId")
    })
    public ForumPostVO addForumPost(ForumPostDTO forumPostDTO) {
        if (forumPostDTO == null) {
            throw new BusinessException(ForumPostEnum.POST_REQUIRED);
        }

        if (forumPostDTO.getForumId() == null) {
            throw new BusinessException(ForumPostEnum.FORUM_ID_REQUIRED);
        }

        if (forumPostDTO.getCourseId() == null) {
            throw new BusinessException(ForumPostEnum.COURSE_ID_REQUIRED);
        }

        if (forumPostDTO.getSysUserId() == null) {
            throw new BusinessException(ForumPostEnum.AUTHOR_ID_REQUIRED);
        }

        if (!StringUtils.hasText(forumPostDTO.getTitle())) {
            throw new BusinessException(ForumPostEnum.POST_TITLE_REQUIRED);
        }

        if (!StringUtils.hasText(forumPostDTO.getContent())) {
            throw new BusinessException(ForumPostEnum.POST_CONTENT_REQUIRED);
        }

        ForumPost post = new ForumPost();
        BeanUtils.copyProperties(forumPostDTO, post);

        post.setId(UuidCreator.getTimeOrderedEpoch());

        if (post.getStatus() == null) {
            post.setStatus(StatusEnum.NORMAL.getCode());
        }
        if (post.getViewCount() == null) {
            post.setViewCount(ForumPostConstants.DEFAULT_VIEW_COUNT);
        }
        if (post.getLikeCount() == null) {
            post.setLikeCount(ForumPostConstants.DEFAULT_LIKE_COUNT);
        }
        if (post.getReplyCount() == null) {
            post.setReplyCount(ForumPostConstants.DEFAULT_REPLY_COUNT);
        }
        if (post.getShareCount() == null) {
            post.setShareCount(ForumPostConstants.DEFAULT_SHARE_COUNT);
        }
        if (post.getIsTop() == null) {
            post.setIsTop(ForumPostConstants.DEFAULT_IS_TOP);
        }
        if (post.getIsEssence() == null) {
            post.setIsEssence(ForumPostConstants.DEFAULT_IS_ESSENCE);
        }
        if (post.getIsLocked() == null) {
            post.setIsLocked(ForumPostConstants.DEFAULT_IS_LOCKED);
        }
        if (post.getIsAnonymous() == null) {
            post.setIsAnonymous(ForumPostConstants.DEFAULT_IS_ANONYMOUS);
        }
        if (post.getDeleted() == null) {
            post.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        }

        LocalDateTime now = LocalDateTime.now();
        post.setCreateTime(now);
        post.setUpdateTime(now);

        ForumPost savedPost = forumPostRepository.save(post);

        return convertToVO(savedPost);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumPost", key = "#p0.id"),
            @CacheEvict(value = "ForumPost", key = "'forum:' + #p0.forumId"),
            @CacheEvict(value = "ForumPost", key = "'course:' + #p0.courseId")
    })
    public Boolean updateForumPost(ForumPostDTO forumPostDTO) {
        if (forumPostDTO == null || forumPostDTO.getId() == null) {
            throw new BusinessException(ForumPostEnum.POST_INFO_OR_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumPostConstants.FIELD_ID).is(forumPostDTO.getId());
        criteria.and(ForumPostConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        ForumPost existingPost = mongoTemplate.findOne(query, ForumPost.class);
        if (existingPost == null) {
            throw new BusinessException(ForumPostEnum.POST_NOT_EXISTS);
        }

        if (StringUtils.hasText(forumPostDTO.getTitle())) {
            existingPost.setTitle(forumPostDTO.getTitle());
        }
        if (StringUtils.hasText(forumPostDTO.getContent())) {
            existingPost.setContent(forumPostDTO.getContent());
        }
        if (forumPostDTO.getPostType() != null) {
            existingPost.setPostType(forumPostDTO.getPostType());
        }
        if (forumPostDTO.getIsAnonymous() != null) {
            existingPost.setIsAnonymous(forumPostDTO.getIsAnonymous());
        }
        if (forumPostDTO.getAttachmentUrls() != null) {
            existingPost.setAttachmentUrls(forumPostDTO.getAttachmentUrls());
        }
        if (forumPostDTO.getImageUrls() != null) {
            existingPost.setImageUrls(forumPostDTO.getImageUrls());
        }
        if (forumPostDTO.getTags() != null) {
            existingPost.setTags(forumPostDTO.getTags());
        }
        if (forumPostDTO.getStatus() != null) {
            existingPost.setStatus(forumPostDTO.getStatus());
        }
        if (forumPostDTO.getChapterId() != null) {
            existingPost.setChapterId(forumPostDTO.getChapterId());
        }

        existingPost.setUpdateTime(LocalDateTime.now());

        forumPostRepository.save(existingPost);

        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = "ForumPost", allEntries = true)
    public Boolean removeForumPostById(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumPostConstants.FIELD_ID).is(id);
        criteria.and(ForumPostConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        ForumPost post = mongoTemplate.findOne(query, ForumPost.class);
        if (post == null) {
            throw new BusinessException(ForumPostEnum.POST_NOT_EXISTS);
        }

        post.setDeleted(DeletedEnum.DELETED.getCode());
        post.setUpdateTime(LocalDateTime.now());
        forumPostRepository.save(post);

        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = "ForumPost", allEntries = true)
    public Integer removeForumPostByIds(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(ForumPostEnum.POST_ID_LIST_REQUIRED);
        }

        // 使用批量操作避免N+1问题
        Query query = new Query(Criteria.where(ForumPostConstants.FIELD_ID).in(ids)
                .and(ForumPostConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode()));
        Update update = new Update()
                .set(ForumPostConstants.FIELD_IS_DELETED, DeletedEnum.DELETED.getCode())
                .set(ForumPostConstants.FIELD_UPDATE_TIME, LocalDateTime.now());

        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, ForumPost.class);
        int deletedCount = (int) updateResult.getModifiedCount();

        return deletedCount;
    }

    @Override
    @Transactional
    @CacheEvict(value = "ForumPost", allEntries = true)
    public Boolean updatePostStatus(UUID id, Integer status) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }
        if (status == null || (status < ForumPostConstants.STATUS_MIN || status > ForumPostConstants.STATUS_MAX)) {
            throw new BusinessException(ForumPostEnum.POST_STATUS_INVALID);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumPostConstants.FIELD_ID).is(id);
        criteria.and(ForumPostConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        ForumPost post = mongoTemplate.findOne(query, ForumPost.class);
        if (post == null) {
            throw new BusinessException(ForumPostEnum.POST_NOT_EXISTS);
        }

        post.setStatus(status);
        post.setUpdateTime(LocalDateTime.now());
        forumPostRepository.save(post);

        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = "ForumPost", allEntries = true)
    public Boolean setPostTop(UUID id, Integer isTop) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }
        if (isTop == null || (isTop < 0 || isTop > 1)) {
            throw new BusinessException(ForumPostEnum.IS_TOP_INVALID);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumPostConstants.FIELD_ID).is(id);
        criteria.and(ForumPostConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        ForumPost post = mongoTemplate.findOne(query, ForumPost.class);
        if (post == null) {
            throw new BusinessException(ForumPostEnum.POST_NOT_EXISTS);
        }

        post.setIsTop(isTop);
        post.setUpdateTime(LocalDateTime.now());
        forumPostRepository.save(post);

        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = "ForumPost", allEntries = true)
    public Boolean setPostEssence(UUID id, Integer isEssence) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }
        if (isEssence == null || (isEssence < 0 || isEssence > 1)) {
            throw new BusinessException(ForumPostEnum.IS_ESSENCE_INVALID);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumPostConstants.FIELD_ID).is(id);
        criteria.and(ForumPostConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        ForumPost post = mongoTemplate.findOne(query, ForumPost.class);
        if (post == null) {
            throw new BusinessException(ForumPostEnum.POST_NOT_EXISTS);
        }

        post.setIsEssence(isEssence);
        post.setUpdateTime(LocalDateTime.now());
        forumPostRepository.save(post);

        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = "ForumPost", allEntries = true)
    public Boolean setPostLock(UUID id, Integer isLocked) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }
        if (isLocked == null || (isLocked < 0 || isLocked > 1)) {
            throw new BusinessException(ForumPostEnum.IS_LOCKED_INVALID);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumPostConstants.FIELD_ID).is(id);
        criteria.and(ForumPostConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        ForumPost post = mongoTemplate.findOne(query, ForumPost.class);
        if (post == null) {
            throw new BusinessException(ForumPostEnum.POST_NOT_EXISTS);
        }

        post.setIsLocked(isLocked);
        post.setUpdateTime(LocalDateTime.now());
        forumPostRepository.save(post);

        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = "ForumPost", key = "#p0")
    public Boolean likePost(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }

        Query query = new Query(Criteria.where(ForumPostConstants.FIELD_ID).is(id));
        Update update = new Update().inc(ForumPostConstants.FIELD_LIKE_COUNT, ForumPostConstants.INCREMENT_VALUE);

        mongoTemplate.updateFirst(query, update, ForumPost.class);

        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = "ForumPost", key = "#p0")
    public Boolean unlikePost(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }

        Query query = new Query(Criteria.where(ForumPostConstants.FIELD_ID).is(id));
        Update update = new Update().inc(ForumPostConstants.FIELD_LIKE_COUNT, ForumPostConstants.DECREMENT_VALUE);

        mongoTemplate.updateFirst(query, update, ForumPost.class);

        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = "ForumPost", key = "#p0")
    public Boolean sharePost(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }

        Query query = new Query(Criteria.where(ForumPostConstants.FIELD_ID).is(id));
        Update update = new Update().inc(ForumPostConstants.FIELD_SHARE_COUNT, ForumPostConstants.INCREMENT_VALUE);

        mongoTemplate.updateFirst(query, update, ForumPost.class);

        return true;
    }

    @Override
    @Transactional
    @CacheEvict(value = "ForumPost", key = "#p0")
    public Boolean viewPost(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }

        Query query = new Query(Criteria.where(ForumPostConstants.FIELD_ID).is(id));
        Update update = new Update().inc(ForumPostConstants.FIELD_VIEW_COUNT, ForumPostConstants.INCREMENT_VALUE);

        mongoTemplate.updateFirst(query, update, ForumPost.class);

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ForumPost", key = "'hot:' + #p0", condition = "#p0 != null")
    public List<ForumPostVO> getHotPosts(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumPostConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(
                Sort.Order.desc(ForumPostConstants.FIELD_IS_TOP),
                Sort.Order.desc(ForumPostConstants.FIELD_IS_ESSENCE),
                Sort.Order.desc(ForumPostConstants.FIELD_LIKE_COUNT),
                Sort.Order.desc(ForumPostConstants.FIELD_CREATE_TIME)
        ));
        query.limit(limit);

        List<ForumPost> posts = mongoTemplate.find(query, ForumPost.class);
        return convertToVOList(posts);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ForumPost", key = "'latest:' + #p0", condition = "#p0 != null")
    public List<ForumPostVO> getLatestPosts(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumPostConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(
                Sort.Order.desc(ForumPostConstants.FIELD_IS_TOP),
                Sort.Order.desc(ForumPostConstants.FIELD_IS_ESSENCE),
                Sort.Order.desc(ForumPostConstants.FIELD_LIKE_COUNT),
                Sort.Order.desc(ForumPostConstants.FIELD_CREATE_TIME)
        ));
        query.limit(limit);

        List<ForumPost> posts = mongoTemplate.find(query, ForumPost.class);
        return convertToVOList(posts);
    }

    private List<ForumPostVO> convertToVOList(List<ForumPost> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return new ArrayList<>();
        }

        Map<UUID, SysUserVO> userMap = getUserMap();

        return posts.stream()
                .map(post -> convertToVOWithUserMap(post, userMap))
                .toList();
    }

    private ForumPostVO convertToVO(ForumPost post) {
        if (post == null) {
            return null;
        }

        Map<UUID, SysUserVO> userMap = getUserMap();

        return convertToVOWithUserMap(post, userMap);
    }

    private ForumPostVO convertToVOWithUserMap(ForumPost post, Map<UUID, SysUserVO> userMap) {
        if (post == null) {
            return null;
        }

        ForumPostVO vo = new ForumPostVO();
        BeanUtils.copyProperties(post, vo);

        fillUserInfoWithMap(vo, post.getSysUserId(), userMap);

        return vo;
    }

    private Map<UUID, SysUserVO> getUserMap() {
        Result<List<SysUserVO>> result = sysUserClient.listAllSysUser();
        if (result != null && result.getData() != null) {
            return result.getData().stream()
                    .collect(Collectors.toMap(SysUserVO::getId, user -> user));
        }
        return new HashMap<>();
    }

    private void fillUserInfoWithMap(ForumPostVO vo, UUID sysUserId, Map<UUID, SysUserVO> userMap) {
        if (sysUserId == null || userMap.isEmpty()) {
            return;
        }

        SysUserVO user = userMap.get(sysUserId);
        if (user != null) {
            vo.setUserName(user.getNickName() != null ? user.getNickName() : user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "ForumPost", allEntries = true)
    public Boolean updateReplyCount(UUID postId, Long replyCount) {
        if (postId == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }
        if (replyCount == null || replyCount < 0) {
            throw new BusinessException(ForumPostEnum.REPLY_COUNT_INVALID);
        }

        Query query = new Query(Criteria.where(ForumPostConstants.FIELD_ID).is(postId));
        ForumPost post = mongoTemplate.findOne(query, ForumPost.class);
        if (post == null) {
            throw new BusinessException(ForumPostEnum.POST_NOT_EXISTS);
        }

        Update update = new Update().set(ForumPostConstants.FIELD_REPLY_COUNT, replyCount);
        mongoTemplate.updateFirst(query, update, ForumPost.class);

        return true;
    }
}
