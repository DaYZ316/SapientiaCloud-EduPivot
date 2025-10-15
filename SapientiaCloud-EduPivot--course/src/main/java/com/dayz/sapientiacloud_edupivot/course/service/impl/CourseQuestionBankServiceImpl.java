package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.dayz.sapientiacloud_edupivot.course.common.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.SysUserVO;
import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.constant.CourseQuestionBankConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseQuestionBankDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseQuestionBankQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseQuestionBank;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseQuestionBankVO;
import com.dayz.sapientiacloud_edupivot.course.enums.CourseQuestionBankEnum;
import com.dayz.sapientiacloud_edupivot.course.repository.CourseQuestionBankRepository;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseQuestionBankService;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseQuestionBankServiceImpl implements ICourseQuestionBankService {

    private final CourseQuestionBankRepository courseQuestionBankRepository;
    private final MongoTemplate mongoTemplate;
    private final SysUserClient sysUserClient;

    @Override
    @Transactional(readOnly = true)
    public PageInfo<CourseQuestionBankVO> listCourseQuestionBank(CourseQuestionBankQueryDTO courseQuestionBankQueryDTO) {
        if (courseQuestionBankQueryDTO == null) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_QUESTION_BANK_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();

        if (courseQuestionBankQueryDTO.getCourseId() != null) {
            criteria.and(CourseQuestionBankConstants.FIELD_COURSE_ID).is(courseQuestionBankQueryDTO.getCourseId());
        }

        if (StringUtils.hasText(courseQuestionBankQueryDTO.getBankName())) {
            criteria.and(CourseQuestionBankConstants.FIELD_BANK_NAME).regex(courseQuestionBankQueryDTO.getBankName(), CourseQuestionBankConstants.REGEX_CASE_INSENSITIVE);
        }

        if (courseQuestionBankQueryDTO.getBankType() != null) {
            criteria.and(CourseQuestionBankConstants.FIELD_BANK_TYPE).is(courseQuestionBankQueryDTO.getBankType());
        }

        if (courseQuestionBankQueryDTO.getDifficulty() != null) {
            criteria.and(CourseQuestionBankConstants.FIELD_DIFFICULTY).is(courseQuestionBankQueryDTO.getDifficulty());
        }

        if (courseQuestionBankQueryDTO.getIsPublic() != null) {
            criteria.and(CourseQuestionBankConstants.FIELD_IS_PUBLIC).is(courseQuestionBankQueryDTO.getIsPublic());
        }

        if (!CollectionUtils.isEmpty(courseQuestionBankQueryDTO.getTags())) {
            criteria.and(CourseQuestionBankConstants.FIELD_TAGS).in(courseQuestionBankQueryDTO.getTags());
        }

        if (StringUtils.hasText(courseQuestionBankQueryDTO.getCreateTimeStart())) {
            criteria.and(CourseQuestionBankConstants.FIELD_CREATE_TIME).gte(courseQuestionBankQueryDTO.getCreateTimeStart());
        }

        if (StringUtils.hasText(courseQuestionBankQueryDTO.getCreateTimeEnd())) {
            criteria.and(CourseQuestionBankConstants.FIELD_CREATE_TIME).lte(courseQuestionBankQueryDTO.getCreateTimeEnd());
        }

        criteria.and(CourseQuestionBankConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, CourseQuestionBankConstants.FIELD_CREATE_TIME));

        Query countQuery = new Query();
        countQuery.addCriteria(criteria);

        Pageable pageable = PageRequest.of(
                courseQuestionBankQueryDTO.getPageNum() - CourseQuestionBankConstants.PAGE_NUM_OFFSET,
                courseQuestionBankQueryDTO.getPageSize()
        );
        query.with(pageable);

        List<CourseQuestionBank> questionBanks = mongoTemplate.find(query, CourseQuestionBank.class);
        long total = mongoTemplate.count(countQuery, CourseQuestionBank.class);

        List<CourseQuestionBankVO> questionBankVOList = convertToVOList(questionBanks);

        PageInfo<CourseQuestionBankVO> pageInfo = new PageInfo<>(questionBankVOList);
        pageInfo.setTotal(total);
        pageInfo.setPageNum(courseQuestionBankQueryDTO.getPageNum());
        pageInfo.setPageSize(courseQuestionBankQueryDTO.getPageSize());
        pageInfo.setPages((int) Math.ceil((double) total / courseQuestionBankQueryDTO.getPageSize()));

        return pageInfo;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseQuestionBank", key = "'course:' + #p0", condition = "#p0 != null")
    public List<CourseQuestionBankVO> listAllCourseQuestionBankByCourseId(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseQuestionBankConstants.FIELD_COURSE_ID).is(courseId);
        criteria.and(CourseQuestionBankConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, CourseQuestionBankConstants.FIELD_CREATE_TIME));

        List<CourseQuestionBank> questionBanks = mongoTemplate.find(query, CourseQuestionBank.class);
        return convertToVOList(questionBanks);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "CourseQuestionBank", key = "#p0", condition = "#p0 != null")
    public CourseQuestionBankVO getCourseQuestionBankById(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_QUESTION_BANK_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseQuestionBankConstants.FIELD_ID).is(id);
        criteria.and(CourseQuestionBankConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        CourseQuestionBank questionBank = mongoTemplate.findOne(query, CourseQuestionBank.class);
        if (questionBank == null) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_QUESTION_BANK_NOT_EXISTS);
        }

        return convertToVO(questionBank);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseQuestionBank", allEntries = true)
    })
    public CourseQuestionBankVO addCourseQuestionBank(CourseQuestionBankDTO courseQuestionBankDTO) {
        if (courseQuestionBankDTO == null) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_QUESTION_BANK_REQUIRED);
        }

        if (courseQuestionBankDTO.getCourseId() == null) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_ID_REQUIRED);
        }

        if (!StringUtils.hasText(courseQuestionBankDTO.getBankName())) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_QUESTION_BANK_NAME_REQUIRED);
        }

        if (courseQuestionBankDTO.getBankType() == null) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_QUESTION_BANK_TYPE_REQUIRED);
        }

        if (courseQuestionBankDTO.getDifficulty() == null) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_QUESTION_BANK_DIFFICULTY_REQUIRED);
        }

        if (courseQuestionBankDTO.getIsPublic() == null) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_QUESTION_BANK_IS_PUBLIC_REQUIRED);
        }

        if (courseQuestionBankDTO.getBankType() < CourseQuestionBankConstants.BANK_TYPE_MIN || 
            courseQuestionBankDTO.getBankType() > CourseQuestionBankConstants.BANK_TYPE_MAX) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_QUESTION_BANK_TYPE_REQUIRED);
        }

        if (courseQuestionBankDTO.getDifficulty() < CourseQuestionBankConstants.DIFFICULTY_MIN || 
            courseQuestionBankDTO.getDifficulty() > CourseQuestionBankConstants.DIFFICULTY_MAX) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_QUESTION_BANK_DIFFICULTY_REQUIRED);
        }

        if (courseQuestionBankDTO.getIsPublic() < CourseQuestionBankConstants.IS_PUBLIC_MIN || 
            courseQuestionBankDTO.getIsPublic() > CourseQuestionBankConstants.IS_PUBLIC_MAX) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_QUESTION_BANK_IS_PUBLIC_REQUIRED);
        }

        CourseQuestionBank questionBank = new CourseQuestionBank();
        BeanUtils.copyProperties(courseQuestionBankDTO, questionBank);

        questionBank.setId(UuidCreator.getTimeOrderedEpoch());

        LocalDateTime now = LocalDateTime.now();
        questionBank.setCreateTime(now);
        questionBank.setUpdateTime(now);
        questionBank.setDeleted(DeletedEnum.NOT_DELETED.getCode());

        CourseQuestionBank savedQuestionBank = courseQuestionBankRepository.save(questionBank);

        log.info(CourseQuestionBankConstants.LOG_ADD_SUCCESS, savedQuestionBank.getId());
        return convertToVO(savedQuestionBank);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseQuestionBank", key = "#p0.id"),
            @CacheEvict(value = "CourseQuestionBank", allEntries = true)
    })
    public Boolean updateCourseQuestionBank(CourseQuestionBankDTO courseQuestionBankDTO) {
        if (courseQuestionBankDTO == null || courseQuestionBankDTO.getId() == null) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_QUESTION_BANK_INFO_OR_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseQuestionBankConstants.FIELD_ID).is(courseQuestionBankDTO.getId());
        criteria.and(CourseQuestionBankConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        CourseQuestionBank existingQuestionBank = mongoTemplate.findOne(query, CourseQuestionBank.class);
        if (existingQuestionBank == null) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_QUESTION_BANK_NOT_EXISTS);
        }

        if (StringUtils.hasText(courseQuestionBankDTO.getBankName())) {
            existingQuestionBank.setBankName(courseQuestionBankDTO.getBankName());
        }
        if (StringUtils.hasText(courseQuestionBankDTO.getDescription())) {
            existingQuestionBank.setDescription(courseQuestionBankDTO.getDescription());
        }
        if (courseQuestionBankDTO.getBankType() != null) {
            existingQuestionBank.setBankType(courseQuestionBankDTO.getBankType());
        }
        if (courseQuestionBankDTO.getTags() != null) {
            existingQuestionBank.setTags(courseQuestionBankDTO.getTags());
        }
        if (courseQuestionBankDTO.getDifficulty() != null) {
            existingQuestionBank.setDifficulty(courseQuestionBankDTO.getDifficulty());
        }
        if (courseQuestionBankDTO.getIsPublic() != null) {
            existingQuestionBank.setIsPublic(courseQuestionBankDTO.getIsPublic());
        }

        existingQuestionBank.setUpdateTime(LocalDateTime.now());

        courseQuestionBankRepository.save(existingQuestionBank);

        log.info(CourseQuestionBankConstants.LOG_UPDATE_SUCCESS, existingQuestionBank.getId());
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseQuestionBank", allEntries = true)
    })
    public Boolean removeCourseQuestionBankById(UUID id) {
        if (id == null) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_QUESTION_BANK_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(CourseQuestionBankConstants.FIELD_ID).is(id);
        criteria.and(CourseQuestionBankConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        CourseQuestionBank questionBank = mongoTemplate.findOne(query, CourseQuestionBank.class);
        if (questionBank == null) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_QUESTION_BANK_NOT_EXISTS);
        }

        questionBank.setDeleted(DeletedEnum.DELETED.getCode());
        questionBank.setUpdateTime(LocalDateTime.now());
        courseQuestionBankRepository.save(questionBank);

        log.info(CourseQuestionBankConstants.LOG_DELETE_SUCCESS, questionBank.getId());
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "CourseQuestionBank", allEntries = true)
    })
    public Integer removeCourseQuestionBankByIds(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(CourseQuestionBankEnum.COURSE_QUESTION_BANK_IDS_REQUIRED);
        }

        int deletedCount = 0;
        for (UUID id : ids) {
            try {
                if (removeCourseQuestionBankById(id)) {
                    deletedCount++;
                }
            } catch (Exception e) {
                log.warn(CourseQuestionBankConstants.LOG_DELETE_FAILED, id, e.getMessage());
            }
        }

        log.info(CourseQuestionBankConstants.LOG_BATCH_DELETE_SUCCESS, deletedCount);
        return deletedCount;
    }

    private List<CourseQuestionBankVO> convertToVOList(List<CourseQuestionBank> questionBanks) {
        if (CollectionUtils.isEmpty(questionBanks)) {
            return new ArrayList<>();
        }

        Map<UUID, SysUserVO> userMap = getUserMap();

        return questionBanks.stream()
                .map(questionBank -> convertToVOWithUserMap(questionBank, userMap))
                .toList();
    }

    private CourseQuestionBankVO convertToVO(CourseQuestionBank questionBank) {
        if (questionBank == null) {
            return null;
        }

        Map<UUID, SysUserVO> userMap = getUserMap();

        return convertToVOWithUserMap(questionBank, userMap);
    }

    private CourseQuestionBankVO convertToVOWithUserMap(CourseQuestionBank questionBank, Map<UUID, SysUserVO> userMap) {
        if (questionBank == null) {
            return null;
        }

        CourseQuestionBankVO vo = new CourseQuestionBankVO();
        BeanUtils.copyProperties(questionBank, vo);

        fillUserInfoWithMap(vo, questionBank.getSysUserId(), userMap);

        return vo;
    }

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

    private void fillUserInfoWithMap(CourseQuestionBankVO vo, UUID sysUserId, Map<UUID, SysUserVO> userMap) {
        if (sysUserId == null || userMap.isEmpty()) {
            return;
        }

        SysUserVO user = userMap.get(sysUserId);
        if (user != null) {
            vo.setSysUserName(user.getNickName() != null ? user.getNickName() : user.getUsername());
            vo.setSysUserAvatar(user.getAvatar());
        }
    }
}
