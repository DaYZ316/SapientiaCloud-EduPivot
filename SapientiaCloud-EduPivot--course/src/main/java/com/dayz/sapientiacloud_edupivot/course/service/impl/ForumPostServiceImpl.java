package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
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
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ForumPostServiceImpl implements IForumPostService {

    private final ForumPostRepository forumPostRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional(readOnly = true)
    public PageInfo<ForumPostVO> listForumPost(ForumPostQueryDTO forumPostQueryDTO) {
        if (forumPostQueryDTO == null) {
            throw new BusinessException(ForumPostEnum.POST_REQUIRED);
        }

        // 构建查询条件
        Query query = new Query();
        Criteria criteria = new Criteria();

        // 论坛ID
        if (forumPostQueryDTO.getForumId() != null) {
            criteria.and(ForumPostConstants.FIELD_FORUM_ID).is(forumPostQueryDTO.getForumId());
        }

        // 课程ID
        if (forumPostQueryDTO.getCourseId() != null) {
            criteria.and(ForumPostConstants.FIELD_COURSE_ID).is(forumPostQueryDTO.getCourseId());
        }

        // 作者ID
        if (forumPostQueryDTO.getSysUserId() != null) {
            criteria.and(ForumPostConstants.FIELD_SYS_USER_ID).is(forumPostQueryDTO.getSysUserId());
        }

        // 帖子标题模糊查询
        if (StringUtils.hasText(forumPostQueryDTO.getTitle())) {
            criteria.and(ForumPostConstants.FIELD_TITLE).regex(forumPostQueryDTO.getTitle(), ForumPostConstants.REGEX_CASE_INSENSITIVE);
        }

        // 帖子类型
        if (forumPostQueryDTO.getPostType() != null) {
            criteria.and(ForumPostConstants.FIELD_POST_TYPE).is(forumPostQueryDTO.getPostType());
        }

        // 状态
        if (forumPostQueryDTO.getStatus() != null) {
            criteria.and(ForumPostConstants.FIELD_STATUS).is(forumPostQueryDTO.getStatus());
        }

        // 逻辑删除
        criteria.and(ForumPostConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, ForumPostConstants.FIELD_IS_TOP, ForumPostConstants.FIELD_CREATE_TIME));

        // 分页
        Pageable pageable = PageRequest.of(
                forumPostQueryDTO.getPageNum() - ForumPostConstants.PAGE_NUM_OFFSET,
                forumPostQueryDTO.getPageSize()
        );
        query.with(pageable);

        // 执行查询
        List<ForumPost> posts = mongoTemplate.find(query, ForumPost.class);
        long total = mongoTemplate.count(query, ForumPost.class);

        // 转换为VO
        List<ForumPostVO> postVOList = posts.stream()
                .map(this::convertToVO)
                .toList();

        // 构建分页信息
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
    public PageInfo<ForumPostVO> listForumPostByForumId(UUID forumId, ForumPostQueryDTO forumPostQueryDTO) {
        if (forumId == null) {
            throw new BusinessException(ForumPostEnum.FORUM_ID_REQUIRED);
        }

        List<ForumPost> posts = forumPostRepository.findByForumIdOrderByCreateTimeDesc(forumId);
        List<ForumPostVO> postVOList = posts.stream()
                .map(this::convertToVO)
                .toList();

        // 构建分页信息
        PageInfo<ForumPostVO> pageInfo = new PageInfo<>(postVOList);
        pageInfo.setTotal(postVOList.size());
        pageInfo.setPageNum(1);
        pageInfo.setPageSize(postVOList.size());
        pageInfo.setPages(1);

        return pageInfo;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ForumPost", key = "'course:' + #p0", condition = "#p0 != null")
    public PageInfo<ForumPostVO> listForumPostByCourseId(UUID courseId, ForumPostQueryDTO forumPostQueryDTO) {
        if (courseId == null) {
            throw new BusinessException(ForumPostEnum.COURSE_ID_REQUIRED);
        }

        List<ForumPost> posts = forumPostRepository.findByCourseIdOrderByCreateTimeDesc(courseId);
        List<ForumPostVO> postVOList = posts.stream()
                .map(this::convertToVO)
                .toList();

        // 构建分页信息
        PageInfo<ForumPostVO> pageInfo = new PageInfo<>(postVOList);
        pageInfo.setTotal(postVOList.size());
        pageInfo.setPageNum(1);
        pageInfo.setPageSize(postVOList.size());
        pageInfo.setPages(1);

        return pageInfo;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ForumPost", key = "#p0", condition = "#p0 != null")
    public ForumPostVO getForumPostById(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }

        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ForumPostEnum.POST_NOT_EXISTS));

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

        // 验证论坛ID
        if (forumPostDTO.getForumId() == null) {
            throw new BusinessException(ForumPostEnum.FORUM_ID_REQUIRED);
        }

        // 验证课程ID
        if (forumPostDTO.getCourseId() == null) {
            throw new BusinessException(ForumPostEnum.COURSE_ID_REQUIRED);
        }

        // 验证作者ID
        if (forumPostDTO.getSysUserId() == null) {
            throw new BusinessException(ForumPostEnum.AUTHOR_ID_REQUIRED);
        }

        // 验证帖子标题
        if (!StringUtils.hasText(forumPostDTO.getTitle())) {
            throw new BusinessException(ForumPostEnum.POST_TITLE_REQUIRED);
        }

        // 验证帖子内容
        if (!StringUtils.hasText(forumPostDTO.getContent())) {
            throw new BusinessException(ForumPostEnum.POST_CONTENT_REQUIRED);
        }

        // 创建帖子实体
        ForumPost post = new ForumPost();
        BeanUtils.copyProperties(forumPostDTO, post);

        // 设置ID
        post.setId(UuidCreator.getTimeOrderedEpoch());

        // 设置默认值
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

        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        post.setCreateTime(now);
        post.setUpdateTime(now);

        // 保存帖子
        ForumPost savedPost = forumPostRepository.save(post);

        log.info(ForumPostConstants.LOG_ADD_SUCCESS, savedPost.getTitle());
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

        // 检查帖子是否存在
        ForumPost existingPost = forumPostRepository.findById(forumPostDTO.getId())
                .orElseThrow(() -> new BusinessException(ForumPostEnum.POST_NOT_EXISTS));

        // 更新字段
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

        // 更新时间
        existingPost.setUpdateTime(LocalDateTime.now());

        // 保存更新
        forumPostRepository.save(existingPost);

        log.info(ForumPostConstants.LOG_UPDATE_SUCCESS, existingPost.getTitle());
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumPost", key = "#p0"),
            @CacheEvict(value = "ForumPost", allEntries = true)
    })
    public Boolean removeForumPostById(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }

        // 检查帖子是否存在
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ForumPostEnum.POST_NOT_EXISTS));

        // 逻辑删除
        post.setDeleted(DeletedEnum.DELETED.getCode());
        post.setUpdateTime(LocalDateTime.now());
        forumPostRepository.save(post);

        log.info(ForumPostConstants.LOG_DELETE_SUCCESS, post.getTitle());
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumPost", allEntries = true)
    })
    public Integer removeForumPostByIds(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(ForumPostEnum.POST_ID_LIST_REQUIRED);
        }

        int deletedCount = 0;
        for (UUID id : ids) {
            try {
                if (removeForumPostById(id)) {
                    deletedCount++;
                }
            } catch (Exception e) {
                log.warn(ForumPostConstants.LOG_DELETE_FAILED, id, e.getMessage());
            }
        }

        log.info(ForumPostConstants.LOG_BATCH_DELETE_SUCCESS, deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumPost", key = "#p0"),
            @CacheEvict(value = "ForumPost", allEntries = true)
    })
    public Boolean updatePostStatus(UUID id, Integer status) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }
        if (status == null || (status < ForumPostConstants.STATUS_MIN || status > ForumPostConstants.STATUS_MAX)) {
            throw new BusinessException(ForumPostEnum.POST_STATUS_INVALID);
        }

        // 检查帖子是否存在
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ForumPostEnum.POST_NOT_EXISTS));

        // 更新状态
        post.setStatus(status);
        post.setUpdateTime(LocalDateTime.now());
        forumPostRepository.save(post);

        log.info(ForumPostConstants.LOG_UPDATE_STATUS_SUCCESS, post.getTitle(), status);
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumPost", key = "#p0"),
            @CacheEvict(value = "ForumPost", allEntries = true)
    })
    public Boolean setPostTop(UUID id, Integer isTop) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }
        if (isTop == null || (isTop < 0 || isTop > 1)) {
            throw new BusinessException(ForumPostEnum.IS_TOP_INVALID);
        }

        // 检查帖子是否存在
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ForumPostEnum.POST_NOT_EXISTS));

        // 更新置顶状态
        post.setIsTop(isTop);
        post.setUpdateTime(LocalDateTime.now());
        forumPostRepository.save(post);

        log.info(ForumPostConstants.LOG_SET_TOP_SUCCESS, post.getTitle(), isTop);
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumPost", key = "#p0"),
            @CacheEvict(value = "ForumPost", allEntries = true)
    })
    public Boolean setPostEssence(UUID id, Integer isEssence) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }
        if (isEssence == null || (isEssence < 0 || isEssence > 1)) {
            throw new BusinessException(ForumPostEnum.IS_ESSENCE_INVALID);
        }

        // 检查帖子是否存在
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ForumPostEnum.POST_NOT_EXISTS));

        // 更新精华状态
        post.setIsEssence(isEssence);
        post.setUpdateTime(LocalDateTime.now());
        forumPostRepository.save(post);

        log.info(ForumPostConstants.LOG_SET_ESSENCE_SUCCESS, post.getTitle(), isEssence);
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumPost", key = "#p0"),
            @CacheEvict(value = "ForumPost", allEntries = true)
    })
    public Boolean setPostLock(UUID id, Integer isLocked) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }
        if (isLocked == null || (isLocked < 0 || isLocked > 1)) {
            throw new BusinessException(ForumPostEnum.IS_LOCKED_INVALID);
        }

        // 检查帖子是否存在
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ForumPostEnum.POST_NOT_EXISTS));

        // 更新锁定状态
        post.setIsLocked(isLocked);
        post.setUpdateTime(LocalDateTime.now());
        forumPostRepository.save(post);

        log.info(ForumPostConstants.LOG_SET_LOCK_SUCCESS, post.getTitle(), isLocked);
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumPost", key = "#p0")
    })
    public Boolean likePost(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }

        Query query = new Query(Criteria.where(ForumPostConstants.FIELD_ID).is(id));
        Update update = new Update().inc(ForumPostConstants.FIELD_LIKE_COUNT, ForumPostConstants.INCREMENT_VALUE);

        mongoTemplate.updateFirst(query, update, ForumPost.class);

        log.info(ForumPostConstants.LOG_LIKE_SUCCESS, id);
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumPost", key = "#p0")
    })
    public Boolean unlikePost(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }

        Query query = new Query(Criteria.where(ForumPostConstants.FIELD_ID).is(id));
        Update update = new Update().inc(ForumPostConstants.FIELD_LIKE_COUNT, ForumPostConstants.DECREMENT_VALUE);

        mongoTemplate.updateFirst(query, update, ForumPost.class);

        log.info(ForumPostConstants.LOG_UNLIKE_SUCCESS, id);
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumPost", key = "#p0")
    })
    public Boolean sharePost(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }

        Query query = new Query(Criteria.where(ForumPostConstants.FIELD_ID).is(id));
        Update update = new Update().inc(ForumPostConstants.FIELD_SHARE_COUNT, ForumPostConstants.INCREMENT_VALUE);

        mongoTemplate.updateFirst(query, update, ForumPost.class);

        log.info(ForumPostConstants.LOG_SHARE_SUCCESS, id);
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumPost", key = "#p0")
    })
    public Boolean viewPost(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumPostEnum.POST_ID_REQUIRED);
        }

        Query query = new Query(Criteria.where(ForumPostConstants.FIELD_ID).is(id));
        Update update = new Update().inc(ForumPostConstants.FIELD_VIEW_COUNT, ForumPostConstants.INCREMENT_VALUE);

        mongoTemplate.updateFirst(query, update, ForumPost.class);

        log.info(ForumPostConstants.LOG_VIEW_SUCCESS, id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ForumPost", key = "'hot:' + #p0", condition = "#p0 != null")
    public List<ForumPostVO> getHotPosts(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        List<ForumPost> posts = forumPostRepository.findTopPostsByLikeCount(limit);
        return posts.stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ForumPost", key = "'latest:' + #p0", condition = "#p0 != null")
    public List<ForumPostVO> getLatestPosts(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        List<ForumPost> posts = forumPostRepository.findLatestPosts(limit);
        return posts.stream()
                .map(this::convertToVO)
                .toList();
    }

    /**
     * 将PO转换为VO
     *
     * @param post 帖子PO
     * @return 帖子VO
     */
    private ForumPostVO convertToVO(ForumPost post) {
        if (post == null) {
            return null;
        }

        ForumPostVO vo = new ForumPostVO();
        BeanUtils.copyProperties(post, vo);
        return vo;
    }
}
