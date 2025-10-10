package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.dayz.sapientiacloud_edupivot.course.common.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.SysUserVO;
import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.constant.ForumReplyConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.ForumReplyDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.ForumReplyQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.ForumReply;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.ForumReplyVO;
import com.dayz.sapientiacloud_edupivot.course.enums.ForumReplyEnum;
import com.dayz.sapientiacloud_edupivot.course.repository.ForumReplyRepository;
import com.dayz.sapientiacloud_edupivot.course.service.IForumPostService;
import com.dayz.sapientiacloud_edupivot.course.service.IForumReplyService;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 论坛回复服务实现类
 *
 * @author SapientiaCloud
 * @since 2024-01-01
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ForumReplyServiceImpl implements IForumReplyService {

    private final ForumReplyRepository forumReplyRepository;
    private final MongoTemplate mongoTemplate;
    private final SysUserClient sysUserClient;
    private final IForumPostService forumPostService;

    @Override
    @Transactional(readOnly = true)
    public PageInfo<ForumReplyVO> listForumReply(ForumReplyQueryDTO forumReplyQueryDTO) {
        if (forumReplyQueryDTO == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_REQUIRED);
        }

        // 构建查询条件
        Query query = new Query();
        Criteria criteria = new Criteria();

        // 帖子ID
        if (forumReplyQueryDTO.getPostId() != null) {
            criteria.and(ForumReplyConstants.FIELD_POST_ID).is(forumReplyQueryDTO.getPostId());
        }

        // 论坛ID
        if (forumReplyQueryDTO.getForumId() != null) {
            criteria.and(ForumReplyConstants.FIELD_FORUM_ID).is(forumReplyQueryDTO.getForumId());
        }

        // 课程ID
        if (forumReplyQueryDTO.getCourseId() != null) {
            criteria.and(ForumReplyConstants.FIELD_COURSE_ID).is(forumReplyQueryDTO.getCourseId());
        }

        // 作者ID
        if (forumReplyQueryDTO.getSysUserId() != null) {
            criteria.and(ForumReplyConstants.FIELD_SYS_USER_ID).is(forumReplyQueryDTO.getSysUserId());
        }

        // 父回复ID
        if (forumReplyQueryDTO.getParentReplyId() != null) {
            criteria.and(ForumReplyConstants.FIELD_PARENT_REPLY_ID).is(forumReplyQueryDTO.getParentReplyId());
        } else {
            // 如果父回复ID为null，则查询父回复为null的回复（根回复）
            criteria.and(ForumReplyConstants.FIELD_PARENT_REPLY_ID).isNull();
        }

        // 状态
        if (forumReplyQueryDTO.getStatus() != null) {
            criteria.and(ForumReplyConstants.FIELD_STATUS).is(forumReplyQueryDTO.getStatus());
        }

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, ForumReplyConstants.FIELD_LIKE_COUNT, ForumReplyConstants.FIELD_FLOOR_NUMBER));

        // 创建用于计数的查询对象（不包含分页条件）
        Query countQuery = new Query();
        countQuery.addCriteria(criteria);

        // 分页
        Pageable pageable = PageRequest.of(
                forumReplyQueryDTO.getPageNum() - ForumReplyConstants.PAGE_NUM_OFFSET,
                forumReplyQueryDTO.getPageSize()
        );
        query.with(pageable);

        // 执行查询
        List<ForumReply> replies = mongoTemplate.find(query, ForumReply.class);
        long total = mongoTemplate.count(countQuery, ForumReply.class);

        // 转换为VO
        List<ForumReplyVO> replyVOList = convertToVOList(replies);

        // 构建分页信息
        PageInfo<ForumReplyVO> pageInfo = new PageInfo<>(replyVOList);
        pageInfo.setTotal(total);
        pageInfo.setPageNum(forumReplyQueryDTO.getPageNum());
        pageInfo.setPageSize(forumReplyQueryDTO.getPageSize());
        pageInfo.setPages((int) Math.ceil((double) total / forumReplyQueryDTO.getPageSize()));

        return pageInfo;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ForumReply", key = "'post:' + #p0", condition = "#p0 != null")
    public List<ForumReplyVO> listAllForumReplyByPostId(UUID postId) {
        if (postId == null) {
            throw new BusinessException(ForumReplyEnum.POST_ID_REQUIRED);
        }

        // 构建查询条件
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_POST_ID).is(postId);
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, ForumReplyConstants.FIELD_FLOOR_NUMBER, ForumReplyConstants.FIELD_CREATE_TIME));

        List<ForumReply> replies = mongoTemplate.find(query, ForumReply.class);
        return convertToVOList(replies);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ForumReply", key = "'forum:' + #p0", condition = "#p0 != null")
    public List<ForumReplyVO> listAllForumReplyByForumId(UUID forumId) {
        if (forumId == null) {
            throw new BusinessException(ForumReplyEnum.FORUM_ID_REQUIRED);
        }

        // 构建查询条件
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_FORUM_ID).is(forumId);
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, ForumReplyConstants.FIELD_CREATE_TIME));

        List<ForumReply> replies = mongoTemplate.find(query, ForumReply.class);
        return convertToVOList(replies);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ForumReply", key = "'course:' + #p0", condition = "#p0 != null")
    public List<ForumReplyVO> listAllForumReplyByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(ForumReplyEnum.COURSE_ID_REQUIRED);
        }

        // 构建查询条件
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_COURSE_ID).is(courseId);
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, ForumReplyConstants.FIELD_CREATE_TIME));

        List<ForumReply> replies = mongoTemplate.find(query, ForumReply.class);
        return convertToVOList(replies);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ForumReply", key = "#p0", condition = "#p0 != null")
    public ForumReplyVO getForumReplyById(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_ID).is(id);
        query.addCriteria(criteria);

        ForumReply reply = mongoTemplate.findOne(query, ForumReply.class);
        if (reply == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_NOT_EXISTS);
        }

        return convertToVO(reply);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumReply", key = "'post:' + #p0.postId"),
            @CacheEvict(value = "ForumReply", key = "'forum:' + #p0.forumId"),
            @CacheEvict(value = "ForumReply", key = "'course:' + #p0.courseId"),
            @CacheEvict(value = "ForumReply", key = "'tree:' + #p0.postId"),
            @CacheEvict(value = "ReplyChildren", allEntries = true)
    })
    public ForumReplyVO addForumReply(ForumReplyDTO forumReplyDTO) {
        if (forumReplyDTO == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_REQUIRED);
        }

        // 验证帖子ID
        if (forumReplyDTO.getPostId() == null) {
            throw new BusinessException(ForumReplyEnum.POST_ID_REQUIRED);
        }

        // 验证论坛ID
        if (forumReplyDTO.getForumId() == null) {
            throw new BusinessException(ForumReplyEnum.FORUM_ID_REQUIRED);
        }

        // 验证课程ID
        if (forumReplyDTO.getCourseId() == null) {
            throw new BusinessException(ForumReplyEnum.COURSE_ID_REQUIRED);
        }

        // 验证作者ID
        if (forumReplyDTO.getSysUserId() == null) {
            throw new BusinessException(ForumReplyEnum.AUTHOR_ID_REQUIRED);
        }

        // 验证回复内容
        if (!StringUtils.hasText(forumReplyDTO.getContent())) {
            throw new BusinessException(ForumReplyEnum.REPLY_CONTENT_REQUIRED);
        }

        // 检查回复层级（设置合理的上限防止恶意创建过深的回复）
        if (forumReplyDTO.getParentReplyId() != null) {
            int depth = getReplyDepth(forumReplyDTO.getParentReplyId());
            // 设置一个合理的上限，比如100层，防止恶意创建过深的回复
            if (depth >= 100) {
                throw new BusinessException(ForumReplyEnum.REPLY_TOO_DEEP);
            }
        }

        // 创建回复实体
        ForumReply reply = new ForumReply();
        BeanUtils.copyProperties(forumReplyDTO, reply);

        // 设置ID
        reply.setId(UuidCreator.getTimeOrderedEpoch());

        // 设置默认值
        if (reply.getStatus() == null) {
            reply.setStatus(StatusEnum.NORMAL.getCode());
        }
        if (reply.getLikeCount() == null) {
            reply.setLikeCount(ForumReplyConstants.DEFAULT_LIKE_COUNT);
        }
        if (reply.getReplyCount() == null) {
            reply.setReplyCount(ForumReplyConstants.DEFAULT_REPLY_COUNT);
        }
        if (reply.getIsAccepted() == null) {
            reply.setIsAccepted(ForumReplyConstants.DEFAULT_IS_ACCEPTED);
        }
        if (reply.getIsAnonymous() == null) {
            reply.setIsAnonymous(ForumReplyConstants.DEFAULT_IS_ANONYMOUS);
        }
        if (reply.getDeleted() == null) {
            reply.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        }

        // 设置楼层号
        if (reply.getFloorNumber() == null) {
            reply.setFloorNumber(getNextFloorNumber(forumReplyDTO.getPostId()));
        }

        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        reply.setCreateTime(now);
        reply.setUpdateTime(now);

        // 保存回复
        ForumReply savedReply = forumReplyRepository.save(reply);

        log.info(ForumReplyConstants.LOG_ADD_SUCCESS, savedReply.getId());
        return convertToVO(savedReply);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumReply", key = "#p0.id"),
            @CacheEvict(value = "ForumReply", key = "'post:' + #p0.postId"),
            @CacheEvict(value = "ForumReply", key = "'forum:' + #p0.forumId"),
            @CacheEvict(value = "ForumReply", key = "'course:' + #p0.courseId"),
            @CacheEvict(value = "ForumReply", key = "'tree:' + #p0.id"),
            @CacheEvict(value = "ReplyChildren", allEntries = true)
    })
    public Boolean updateForumReply(ForumReplyDTO forumReplyDTO) {
        if (forumReplyDTO == null || forumReplyDTO.getId() == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_INFO_OR_ID_REQUIRED);
        }

        // 检查回复是否存在
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_ID).is(forumReplyDTO.getId());
        query.addCriteria(criteria);

        ForumReply existingReply = mongoTemplate.findOne(query, ForumReply.class);
        if (existingReply == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_NOT_EXISTS);
        }

        // 更新字段
        if (StringUtils.hasText(forumReplyDTO.getContent())) {
            existingReply.setContent(forumReplyDTO.getContent());
        }
        if (forumReplyDTO.getIsAnonymous() != null) {
            existingReply.setIsAnonymous(forumReplyDTO.getIsAnonymous());
        }
        if (forumReplyDTO.getAttachmentUrls() != null) {
            existingReply.setAttachmentUrls(forumReplyDTO.getAttachmentUrls());
        }
        if (forumReplyDTO.getImageUrls() != null) {
            existingReply.setImageUrls(forumReplyDTO.getImageUrls());
        }
        if (forumReplyDTO.getStatus() != null) {
            existingReply.setStatus(forumReplyDTO.getStatus());
        }

        // 更新时间
        existingReply.setUpdateTime(LocalDateTime.now());

        // 保存更新
        forumReplyRepository.save(existingReply);

        log.info(ForumReplyConstants.LOG_UPDATE_SUCCESS, existingReply.getId());
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumReply", allEntries = true),
            @CacheEvict(value = "ReplyChildren", allEntries = true)
    })
    public Boolean removeForumReplyById(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_ID_REQUIRED);
        }

        // 检查回复是否存在
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_ID).is(id);
        query.addCriteria(criteria);

        ForumReply reply = mongoTemplate.findOne(query, ForumReply.class);
        if (reply == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_NOT_EXISTS);
        }

        // 逻辑删除
        reply.setDeleted(DeletedEnum.DELETED.getCode());
        reply.setUpdateTime(LocalDateTime.now());
        forumReplyRepository.save(reply);

        log.info(ForumReplyConstants.LOG_DELETE_SUCCESS, reply.getId());
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumReply", allEntries = true),
            @CacheEvict(value = "ReplyChildren", allEntries = true)
    })
    public Integer removeForumReplyByIds(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(ForumReplyEnum.REPLY_ID_LIST_REQUIRED);
        }

        int deletedCount = 0;
        for (UUID id : ids) {
            try {
                if (removeForumReplyById(id)) {
                    deletedCount++;
                }
            } catch (Exception e) {
                log.warn(ForumReplyConstants.LOG_DELETE_FAILED, id, e.getMessage());
            }
        }

        log.info(ForumReplyConstants.LOG_BATCH_DELETE_SUCCESS, deletedCount);
        return deletedCount;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumReply", allEntries = true),
            @CacheEvict(value = "ReplyChildren", allEntries = true)
    })
    public Boolean updateReplyStatus(UUID id, Integer status) {
        if (id == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_ID_REQUIRED);
        }
        if (status == null || (status < ForumReplyConstants.STATUS_MIN || status > ForumReplyConstants.STATUS_MAX)) {
            throw new BusinessException(ForumReplyEnum.REPLY_STATUS_INVALID);
        }

        // 检查回复是否存在
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_ID).is(id);
        query.addCriteria(criteria);

        ForumReply reply = mongoTemplate.findOne(query, ForumReply.class);
        if (reply == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_NOT_EXISTS);
        }

        // 更新状态
        reply.setStatus(status);
        reply.setUpdateTime(LocalDateTime.now());
        forumReplyRepository.save(reply);

        log.info(ForumReplyConstants.LOG_UPDATE_STATUS_SUCCESS, reply.getId(), status);
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumReply", allEntries = true),
            @CacheEvict(value = "ReplyChildren", allEntries = true)
    })
    public Boolean likeReply(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_ID_REQUIRED);
        }

        Query query = new Query(Criteria.where(ForumReplyConstants.FIELD_ID).is(id));
        Update update = new Update().inc(ForumReplyConstants.FIELD_LIKE_COUNT, ForumReplyConstants.INCREMENT_VALUE);

        mongoTemplate.updateFirst(query, update, ForumReply.class);

        log.info(ForumReplyConstants.LOG_LIKE_SUCCESS, id);
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumReply", allEntries = true),
            @CacheEvict(value = "ReplyChildren", allEntries = true)
    })
    public Boolean unlikeReply(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_ID_REQUIRED);
        }

        Query query = new Query(Criteria.where(ForumReplyConstants.FIELD_ID).is(id));
        Update update = new Update().inc(ForumReplyConstants.FIELD_LIKE_COUNT, ForumReplyConstants.DECREMENT_VALUE);

        mongoTemplate.updateFirst(query, update, ForumReply.class);

        log.info(ForumReplyConstants.LOG_UNLIKE_SUCCESS, id);
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumReply", allEntries = true),
            @CacheEvict(value = "ReplyChildren", allEntries = true)
    })
    public Boolean acceptReply(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_ID_REQUIRED);
        }

        // 检查回复是否存在
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_ID).is(id);
        query.addCriteria(criteria);

        ForumReply reply = mongoTemplate.findOne(query, ForumReply.class);
        if (reply == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_NOT_EXISTS);
        }

        // 更新采纳状态
        reply.setIsAccepted(1);
        reply.setUpdateTime(LocalDateTime.now());
        forumReplyRepository.save(reply);

        log.info(ForumReplyConstants.LOG_ACCEPT_SUCCESS, reply.getId());
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "ForumReply", allEntries = true),
            @CacheEvict(value = "ReplyChildren", allEntries = true)
    })
    public Boolean unacceptReply(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_ID_REQUIRED);
        }

        // 检查回复是否存在
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_ID).is(id);
        query.addCriteria(criteria);

        ForumReply reply = mongoTemplate.findOne(query, ForumReply.class);
        if (reply == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_NOT_EXISTS);
        }

        // 更新采纳状态
        reply.setIsAccepted(0);
        reply.setUpdateTime(LocalDateTime.now());
        forumReplyRepository.save(reply);

        log.info(ForumReplyConstants.LOG_UNACCEPT_SUCCESS, reply.getId());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ForumReply", key = "'tree:' + #p0", condition = "#p0 != null")
    public List<ForumReplyVO> getReplyTree(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_ID_REQUIRED);
        }

        // 构建查询条件
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_POST_ID).is(id);
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, ForumReplyConstants.FIELD_CREATE_TIME));

        // 获取所有回复
        List<ForumReply> allReplies = mongoTemplate.find(query, ForumReply.class);

        // 转换为VO
        List<ForumReplyVO> replyVOList = convertToVOList(allReplies);

        // 构建树形结构
        return buildReplyTree(replyVOList, null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ReplyChildren", key = "#p0", condition = "#p0 != null")
    public List<ForumReplyVO> getAllRepliesByParentId(UUID parentReplyId) {
        if (parentReplyId == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_ID_REQUIRED);
        }

        // 递归获取所有子回复（平铺的list形式）
        List<ForumReplyVO> allReplies = new ArrayList<>();
        collectAllRepliesByParentId(parentReplyId, allReplies);

        // 按照点赞量（降序）和创建时间（降序）排序
        allReplies.sort((a, b) -> {
            // 首先按点赞量降序排序
            int likeComparison = Long.compare(b.getLikeCount() != null ? b.getLikeCount() : 0L,
                    a.getLikeCount() != null ? a.getLikeCount() : 0L);
            if (likeComparison != 0) {
                return likeComparison;
            }
            // 点赞量相同时，按创建时间降序排序
            if (a.getCreateTime() != null && b.getCreateTime() != null) {
                return b.getCreateTime().compareTo(a.getCreateTime());
            }
            return 0;
        });

        return allReplies;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ForumReply", key = "'statistics:' + #p0", condition = "#p0 != null")
    public ForumReplyVO getReplyStatistics(UUID id) {
        if (id == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_ID).is(id);
        query.addCriteria(criteria);

        ForumReply reply = mongoTemplate.findOne(query, ForumReply.class);
        if (reply == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_NOT_EXISTS);
        }

        return convertToVO(reply);
    }

    /**
     * 获取回复层级深度（支持无限层级）
     *
     * @param parentReplyId 父回复ID
     * @return 层级深度
     */
    private int getReplyDepth(UUID parentReplyId) {
        int depth = 0;
        UUID currentParentId = parentReplyId;
        Set<UUID> visitedIds = new HashSet<>(); // 用于检测循环引用

        while (currentParentId != null) {
            // 检测循环引用，防止无限循环
            if (visitedIds.contains(currentParentId)) {
                log.warn("检测到循环引用，停止深度计算。当前父回复ID: {}", currentParentId);
                break;
            }
            visitedIds.add(currentParentId);

            // 构建查询条件
            Query query = new Query();
            Criteria criteria = new Criteria();
            criteria.and(ForumReplyConstants.FIELD_ID).is(currentParentId);
            query.addCriteria(criteria);

            List<ForumReply> replies = mongoTemplate.find(query, ForumReply.class);
            if (replies.isEmpty()) {
                break;
            }

            ForumReply parentReply = replies.get(0);
            depth++;
            currentParentId = parentReply.getParentReplyId();
        }

        return depth;
    }

    /**
     * 获取下一个楼层号并更新帖子的回复数量
     *
     * @param postId 帖子ID
     * @return 下一个楼层号
     */
    private Integer getNextFloorNumber(UUID postId) {
        // 构建查询条件
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_POST_ID).is(postId);
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, ForumReplyConstants.FIELD_FLOOR_NUMBER));
        query.limit(1);

        List<ForumReply> replies = mongoTemplate.find(query, ForumReply.class);
        int nextFloorNumber = replies.isEmpty() ?
                ForumReplyConstants.DEFAULT_FLOOR_NUMBER :
                replies.get(0).getFloorNumber() + 1;

        // 使用帖子服务更新回复数量
        forumPostService.updateReplyCount(postId, (long) nextFloorNumber);

        return nextFloorNumber;
    }

    /**
     * 构建回复树形结构
     *
     * @param allReplies 所有回复
     * @param parentId   父回复ID
     * @return 树形结构回复列表
     */
    private List<ForumReplyVO> buildReplyTree(List<ForumReplyVO> allReplies, UUID parentId) {
        List<ForumReplyVO> result = new ArrayList<>();

        for (ForumReplyVO reply : allReplies) {
            UUID currentParentId = reply.getParentReplyId();

            // 判断是否为当前层级的回复
            if ((parentId == null && currentParentId == null) ||
                    (parentId != null && parentId.equals(currentParentId))) {

                // 递归查找子回复
                List<ForumReplyVO> children = buildReplyTree(allReplies, reply.getId());
                reply.setChildren(children);

                result.add(reply);
            }
        }

        return result;
    }

    /**
     * 递归收集父回复下的所有子回复（平铺的list形式）
     *
     * @param parentReplyId 父回复ID
     * @param allReplies    收集结果的列表
     */
    private void collectAllRepliesByParentId(UUID parentReplyId, List<ForumReplyVO> allReplies) {
        // 构建查询条件，查找直接子回复
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_PARENT_REPLY_ID).is(parentReplyId);
        query.addCriteria(criteria);
        // 移除排序，统一在 getAllRepliesByParentId 方法中排序

        // 获取直接子回复
        List<ForumReply> directChildren = mongoTemplate.find(query, ForumReply.class);

        for (ForumReply child : directChildren) {
            ForumReplyVO childVO = convertToVO(child);
            allReplies.add(childVO);

            // 递归收集子回复的子回复
            collectAllRepliesByParentId(child.getId(), allReplies);
        }
    }

    /**
     * 批量将PO转换为VO（优化版本，只查询一次用户信息）
     *
     * @param replies 回复PO列表
     * @return 回复VO列表
     */
    private List<ForumReplyVO> convertToVOList(List<ForumReply> replies) {
        if (CollectionUtils.isEmpty(replies)) {
            return new ArrayList<>();
        }

        // 获取所有用户信息
        Map<UUID, SysUserVO> userMap = getUserMap();

        return replies.stream()
                .map(reply -> convertToVOWithUserMap(reply, userMap))
                .toList();
    }

    /**
     * 将PO转换为VO（优化版本，使用用户映射避免N+1查询）
     *
     * @param reply 回复PO
     * @return 回复VO
     */
    private ForumReplyVO convertToVO(ForumReply reply) {
        if (reply == null) {
            return null;
        }

        // 获取用户映射（避免N+1查询）
        Map<UUID, SysUserVO> userMap = getUserMap();

        return convertToVOWithUserMap(reply, userMap);
    }

    /**
     * 使用用户映射将PO转换为VO
     *
     * @param reply   回复PO
     * @param userMap 用户映射
     * @return 回复VO
     */
    private ForumReplyVO convertToVOWithUserMap(ForumReply reply, Map<UUID, SysUserVO> userMap) {
        if (reply == null) {
            return null;
        }

        ForumReplyVO vo = new ForumReplyVO();
        BeanUtils.copyProperties(reply, vo);

        // 填充用户信息
        fillUserInfoWithMap(vo, reply.getSysUserId(), userMap);

        // 填充回复目标用户信息
        fillReplyToUserInfo(vo, reply.getReplyToUserId(), userMap);

        return vo;
    }


    /**
     * 获取用户映射
     *
     * @return 用户ID到用户信息的映射
     */
    private Map<UUID, SysUserVO> getUserMap() {
        try {
            Result<List<SysUserVO>> result = sysUserClient.listAllSysUser();
            if (result != null && result.getData() != null) {
                return result.getData().stream()
                        .collect(Collectors.toMap(SysUserVO::getId, user -> user));
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败: error={}", e.getMessage());
        }
        return new HashMap<>();
    }

    /**
     * 使用用户映射填充用户信息
     *
     * @param vo        回复VO
     * @param sysUserId 用户ID
     * @param userMap   用户映射
     */
    private void fillUserInfoWithMap(ForumReplyVO vo, UUID sysUserId, Map<UUID, SysUserVO> userMap) {
        if (sysUserId == null || userMap.isEmpty()) {
            return;
        }

        SysUserVO user = userMap.get(sysUserId);
        if (user != null) {
            vo.setUserName(user.getNickName() != null ? user.getNickName() : user.getUsername());
            vo.setUserAvatar(user.getAvatar());
        }
    }

    /**
     * 填充回复目标用户信息
     *
     * @param vo            回复VO
     * @param replyToUserId 回复目标用户ID
     * @param userMap       用户映射
     */
    private void fillReplyToUserInfo(ForumReplyVO vo, UUID replyToUserId, Map<UUID, SysUserVO> userMap) {
        if (replyToUserId == null || userMap.isEmpty()) {
            return;
        }

        SysUserVO replyToUser = userMap.get(replyToUserId);
        if (replyToUser != null) {
            vo.setReplyToUserName(replyToUser.getNickName() != null ? replyToUser.getNickName() : replyToUser.getUsername());
        }
    }
}
