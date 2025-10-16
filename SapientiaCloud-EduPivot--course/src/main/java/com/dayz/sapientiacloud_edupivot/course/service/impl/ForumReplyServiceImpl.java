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
import com.mongodb.client.result.UpdateResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

        Query query = new Query();
        Criteria criteria = new Criteria();

        if (forumReplyQueryDTO.getPostId() != null) {
            criteria.and(ForumReplyConstants.FIELD_POST_ID).is(forumReplyQueryDTO.getPostId());
        }

        if (forumReplyQueryDTO.getForumId() != null) {
            criteria.and(ForumReplyConstants.FIELD_FORUM_ID).is(forumReplyQueryDTO.getForumId());
        }

        if (forumReplyQueryDTO.getCourseId() != null) {
            criteria.and(ForumReplyConstants.FIELD_COURSE_ID).is(forumReplyQueryDTO.getCourseId());
        }

        if (forumReplyQueryDTO.getSysUserId() != null) {
            criteria.and(ForumReplyConstants.FIELD_SYS_USER_ID).is(forumReplyQueryDTO.getSysUserId());
        }

        if (forumReplyQueryDTO.getParentReplyId() != null) {
            criteria.and(ForumReplyConstants.FIELD_PARENT_REPLY_ID).is(forumReplyQueryDTO.getParentReplyId());
        } else {
            criteria.and(ForumReplyConstants.FIELD_PARENT_REPLY_ID).isNull();
        }

        if (forumReplyQueryDTO.getStatus() != null) {
            criteria.and(ForumReplyConstants.FIELD_STATUS).is(forumReplyQueryDTO.getStatus());
        }

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, ForumReplyConstants.FIELD_LIKE_COUNT, ForumReplyConstants.FIELD_FLOOR_NUMBER));

        Query countQuery = new Query();
        countQuery.addCriteria(criteria);

        Pageable pageable = PageRequest.of(
                forumReplyQueryDTO.getPageNum() - ForumReplyConstants.PAGE_NUM_OFFSET,
                forumReplyQueryDTO.getPageSize()
        );
        query.with(pageable);

        List<ForumReply> replies = mongoTemplate.find(query, ForumReply.class);
        long total = mongoTemplate.count(countQuery, ForumReply.class);

        List<ForumReplyVO> replyVOList = convertToVOList(replies);

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

        if (forumReplyDTO.getPostId() == null) {
            throw new BusinessException(ForumReplyEnum.POST_ID_REQUIRED);
        }

        if (forumReplyDTO.getForumId() == null) {
            throw new BusinessException(ForumReplyEnum.FORUM_ID_REQUIRED);
        }

        if (forumReplyDTO.getCourseId() == null) {
            throw new BusinessException(ForumReplyEnum.COURSE_ID_REQUIRED);
        }

        if (forumReplyDTO.getSysUserId() == null) {
            throw new BusinessException(ForumReplyEnum.AUTHOR_ID_REQUIRED);
        }

        if (!StringUtils.hasText(forumReplyDTO.getContent())) {
            throw new BusinessException(ForumReplyEnum.REPLY_CONTENT_REQUIRED);
        }

        if (forumReplyDTO.getParentReplyId() != null) {
            int depth = getReplyDepth(forumReplyDTO.getParentReplyId());
            if (depth >= 100) {
                throw new BusinessException(ForumReplyEnum.REPLY_TOO_DEEP);
            }
        }

        ForumReply reply = new ForumReply();
        BeanUtils.copyProperties(forumReplyDTO, reply);

        reply.setId(UuidCreator.getTimeOrderedEpoch());

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

        if (reply.getFloorNumber() == null) {
            reply.setFloorNumber(getNextFloorNumber(forumReplyDTO.getPostId()));
        }

        LocalDateTime now = LocalDateTime.now();
        reply.setCreateTime(now);
        reply.setUpdateTime(now);

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

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_ID).is(forumReplyDTO.getId());
        query.addCriteria(criteria);

        ForumReply existingReply = mongoTemplate.findOne(query, ForumReply.class);
        if (existingReply == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_NOT_EXISTS);
        }

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

        existingReply.setUpdateTime(LocalDateTime.now());

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

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_ID).is(id);
        query.addCriteria(criteria);

        ForumReply reply = mongoTemplate.findOne(query, ForumReply.class);
        if (reply == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_NOT_EXISTS);
        }

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

        // 使用批量操作避免N+1问题
        Query query = new Query(Criteria.where(ForumReplyConstants.FIELD_ID).in(ids));
        Update update = new Update()
                .set(ForumReplyConstants.FIELD_IS_DELETED, DeletedEnum.DELETED.getCode())
                .set(ForumReplyConstants.FIELD_UPDATE_TIME, LocalDateTime.now());

        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, ForumReply.class);
        int deletedCount = (int) updateResult.getModifiedCount();

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

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_ID).is(id);
        query.addCriteria(criteria);

        ForumReply reply = mongoTemplate.findOne(query, ForumReply.class);
        if (reply == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_NOT_EXISTS);
        }

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

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_ID).is(id);
        query.addCriteria(criteria);

        ForumReply reply = mongoTemplate.findOne(query, ForumReply.class);
        if (reply == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_NOT_EXISTS);
        }

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

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_ID).is(id);
        query.addCriteria(criteria);

        ForumReply reply = mongoTemplate.findOne(query, ForumReply.class);
        if (reply == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_NOT_EXISTS);
        }

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

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_POST_ID).is(id);
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, ForumReplyConstants.FIELD_CREATE_TIME));

        List<ForumReply> allReplies = mongoTemplate.find(query, ForumReply.class);

        List<ForumReplyVO> replyVOList = convertToVOList(allReplies);

        return buildReplyTree(replyVOList, null);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ReplyChildren", key = "#p0", condition = "#p0 != null")
    public List<ForumReplyVO> getAllRepliesByParentId(UUID parentReplyId) {
        if (parentReplyId == null) {
            throw new BusinessException(ForumReplyEnum.REPLY_ID_REQUIRED);
        }

        List<ForumReplyVO> allReplies = new ArrayList<>();
        collectAllRepliesByParentId(parentReplyId, allReplies);

        allReplies.sort((a, b) -> {
            int likeComparison = Long.compare(b.getLikeCount() != null ? b.getLikeCount() : 0L,
                    a.getLikeCount() != null ? a.getLikeCount() : 0L);
            if (likeComparison != 0) {
                return likeComparison;
            }
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
        Set<UUID> visitedIds = new HashSet<>();

        while (currentParentId != null) {
            if (visitedIds.contains(currentParentId)) {
                log.warn("检测到循环引用，停止深度计算。当前父回复ID: {}", currentParentId);
                break;
            }
            visitedIds.add(currentParentId);

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

            if ((parentId == null && currentParentId == null) ||
                    (parentId != null && parentId.equals(currentParentId))) {

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
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(ForumReplyConstants.FIELD_PARENT_REPLY_ID).is(parentReplyId);
        query.addCriteria(criteria);

        List<ForumReply> directChildren = mongoTemplate.find(query, ForumReply.class);

        for (ForumReply child : directChildren) {
            ForumReplyVO childVO = convertToVO(child);
            allReplies.add(childVO);

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

        fillUserInfoWithMap(vo, reply.getSysUserId(), userMap);

        fillReplyToUserInfo(vo, reply.getReplyToUserId(), userMap);

        return vo;
    }


    /**
     * 获取用户映射
     *
     * @return 用户ID到用户信息的映射
     */
    private Map<UUID, SysUserVO> getUserMap() {
        Result<List<SysUserVO>> result = sysUserClient.listAllSysUser();
        if (result != null && result.getData() != null) {
            return result.getData().stream()
                    .collect(Collectors.toMap(SysUserVO::getId, user -> user));
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
