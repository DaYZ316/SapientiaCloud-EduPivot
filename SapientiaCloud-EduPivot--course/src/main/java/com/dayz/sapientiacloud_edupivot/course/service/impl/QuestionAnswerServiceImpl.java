package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.dayz.sapientiacloud_edupivot.course.common.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.SysUserVO;
import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.constant.QuestionAnswerConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.QuestionAnswerDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.QuestionAnswer;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.QuestionAnswerVO;
import com.dayz.sapientiacloud_edupivot.course.enums.QuestionAnswerEnum;
import com.dayz.sapientiacloud_edupivot.course.repository.QuestionAnswerRepository;
import com.dayz.sapientiacloud_edupivot.course.service.IQuestionAnswerService;
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
@Slf4j
@RequiredArgsConstructor
public class QuestionAnswerServiceImpl implements IQuestionAnswerService {

    private final QuestionAnswerRepository questionAnswerRepository;
    private final MongoTemplate mongoTemplate;
    private final SysUserClient sysUserClient;

    @Override
    @Transactional(readOnly = true)
    public PageInfo<QuestionAnswerVO> listQuestionAnswer(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageSize == null) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ANSWER_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionAnswerConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, QuestionAnswerConstants.FIELD_CREATE_TIME));

        Query countQuery = new Query();
        countQuery.addCriteria(criteria);

        Pageable pageable = PageRequest.of(
                pageNum - QuestionAnswerConstants.PAGE_NUM_OFFSET,
                pageSize
        );
        query.with(pageable);

        List<QuestionAnswer> answers = mongoTemplate.find(query, QuestionAnswer.class);
        long total = mongoTemplate.count(countQuery, QuestionAnswer.class);

        List<QuestionAnswerVO> answerVOList = convertToVOList(answers);

        PageInfo<QuestionAnswerVO> pageInfo = new PageInfo<>(answerVOList);
        pageInfo.setTotal(total);
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setPages((int) Math.ceil((double) total / pageSize));

        return pageInfo;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "QuestionAnswer", key = "'question:' + #p0", condition = "#p0 != null")
    public List<QuestionAnswerVO> listAllQuestionAnswerByQuestionId(UUID questionId) {
        if (questionId == null) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionAnswerConstants.FIELD_QUESTION_ID).is(questionId);
        criteria.and(QuestionAnswerConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, QuestionAnswerConstants.FIELD_CREATE_TIME));

        List<QuestionAnswer> answers = mongoTemplate.find(query, QuestionAnswer.class);
        return convertToVOList(answers);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "QuestionAnswer", key = "'user:' + #p0", condition = "#p0 != null")
    public List<QuestionAnswerVO> listAllQuestionAnswerBySysUserId(UUID sysUserId) {
        if (sysUserId == null) {
            throw new BusinessException(QuestionAnswerEnum.SYS_USER_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionAnswerConstants.FIELD_SYS_USER_ID).is(sysUserId);
        criteria.and(QuestionAnswerConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, QuestionAnswerConstants.FIELD_CREATE_TIME));

        List<QuestionAnswer> answers = mongoTemplate.find(query, QuestionAnswer.class);
        return convertToVOList(answers);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "QuestionAnswer", key = "'question:' + #p0 + ':user:' + #p1", condition = "#p0 != null && #p1 != null")
    public QuestionAnswerVO getQuestionAnswerByQuestionIdAndSysUserId(UUID questionId, UUID sysUserId) {
        if (questionId == null) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ID_REQUIRED);
        }
        if (sysUserId == null) {
            throw new BusinessException(QuestionAnswerEnum.SYS_USER_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionAnswerConstants.FIELD_QUESTION_ID).is(questionId);
        criteria.and(QuestionAnswerConstants.FIELD_SYS_USER_ID).is(sysUserId);
        criteria.and(QuestionAnswerConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        QuestionAnswer answer = mongoTemplate.findOne(query, QuestionAnswer.class);
        if (answer == null) {
            return null;
        }

        return convertToVO(answer);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "QuestionAnswer", key = "#p0", condition = "#p0 != null")
    public QuestionAnswerVO getQuestionAnswerById(UUID id) {
        if (id == null) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ANSWER_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionAnswerConstants.FIELD_ID).is(id);
        criteria.and(QuestionAnswerConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        QuestionAnswer answer = mongoTemplate.findOne(query, QuestionAnswer.class);
        if (answer == null) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ANSWER_NOT_EXISTS);
        }

        return convertToVO(answer);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "QuestionAnswer", key = "'question:' + #p0.questionId"),
            @CacheEvict(value = "QuestionAnswer", key = "'user:' + #p0.sysUserId"),
            @CacheEvict(value = "QuestionAnswer", allEntries = true)
    })
    public QuestionAnswerVO addQuestionAnswer(QuestionAnswerDTO questionAnswerDTO) {
        if (questionAnswerDTO == null) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ANSWER_REQUIRED);
        }

        if (questionAnswerDTO.getQuestionId() == null) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ID_REQUIRED);
        }

        if (!StringUtils.hasText(questionAnswerDTO.getAnswerContent())) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ANSWER_CONTENT_REQUIRED);
        }

        if (questionAnswerDTO.getIsCorrect() == null) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ANSWER_IS_CORRECT_REQUIRED);
        }

        if (questionAnswerDTO.getScore() == null) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ANSWER_SCORE_REQUIRED);
        }

        if (questionAnswerDTO.getIsCorrect() < QuestionAnswerConstants.IS_CORRECT_MIN ||
                questionAnswerDTO.getIsCorrect() > QuestionAnswerConstants.IS_CORRECT_MAX) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ANSWER_IS_CORRECT_REQUIRED);
        }

        QuestionAnswer answer = new QuestionAnswer();
        BeanUtils.copyProperties(questionAnswerDTO, answer);

        answer.setId(UuidCreator.getTimeOrderedEpoch());

        LocalDateTime now = LocalDateTime.now();
        answer.setCreateTime(now);
        answer.setUpdateTime(now);
        answer.setDeleted(DeletedEnum.NOT_DELETED.getCode());

        QuestionAnswer savedAnswer = questionAnswerRepository.save(answer);

        log.info(QuestionAnswerConstants.LOG_ADD_SUCCESS, savedAnswer.getId());
        return convertToVO(savedAnswer);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "QuestionAnswer", allEntries = true)
    })
    public List<QuestionAnswerVO> addQuestionAnswers(List<QuestionAnswerDTO> questionAnswerDTOList) {
        if (CollectionUtils.isEmpty(questionAnswerDTOList)) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ANSWER_REQUIRED);
        }

        List<QuestionAnswerVO> result = new ArrayList<>();
        for (QuestionAnswerDTO dto : questionAnswerDTOList) {
            QuestionAnswerVO vo = addQuestionAnswer(dto);
            result.add(vo);
        }

        log.info(QuestionAnswerConstants.LOG_BATCH_ADD_SUCCESS, result.size());
        return result;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "QuestionAnswer", key = "#p0.id"),
            @CacheEvict(value = "QuestionAnswer", key = "'question:' + #p0.questionId"),
            @CacheEvict(value = "QuestionAnswer", key = "'user:' + #p0.sysUserId"),
            @CacheEvict(value = "QuestionAnswer", allEntries = true)
    })
    public Boolean updateQuestionAnswer(QuestionAnswerDTO questionAnswerDTO) {
        if (questionAnswerDTO == null || questionAnswerDTO.getId() == null) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ANSWER_INFO_OR_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionAnswerConstants.FIELD_ID).is(questionAnswerDTO.getId());
        criteria.and(QuestionAnswerConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        QuestionAnswer existingAnswer = mongoTemplate.findOne(query, QuestionAnswer.class);
        if (existingAnswer == null) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ANSWER_NOT_EXISTS);
        }

        if (StringUtils.hasText(questionAnswerDTO.getAnswerContent())) {
            existingAnswer.setAnswerContent(questionAnswerDTO.getAnswerContent());
        }
        if (StringUtils.hasText(questionAnswerDTO.getAnswerText())) {
            existingAnswer.setAnswerText(questionAnswerDTO.getAnswerText());
        }
        if (questionAnswerDTO.getIsCorrect() != null) {
            existingAnswer.setIsCorrect(questionAnswerDTO.getIsCorrect());
        }
        if (questionAnswerDTO.getScore() != null) {
            existingAnswer.setScore(questionAnswerDTO.getScore());
        }

        existingAnswer.setUpdateTime(LocalDateTime.now());

        questionAnswerRepository.save(existingAnswer);

        log.info(QuestionAnswerConstants.LOG_UPDATE_SUCCESS, existingAnswer.getId());
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "QuestionAnswer", allEntries = true)
    })
    public Boolean removeQuestionAnswerById(UUID id) {
        if (id == null) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ANSWER_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionAnswerConstants.FIELD_ID).is(id);
        criteria.and(QuestionAnswerConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        QuestionAnswer answer = mongoTemplate.findOne(query, QuestionAnswer.class);
        if (answer == null) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ANSWER_NOT_EXISTS);
        }

        answer.setDeleted(DeletedEnum.DELETED.getCode());
        answer.setUpdateTime(LocalDateTime.now());
        questionAnswerRepository.save(answer);

        log.info(QuestionAnswerConstants.LOG_DELETE_SUCCESS, answer.getId());
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "QuestionAnswer", key = "'question:' + #p0"),
            @CacheEvict(value = "QuestionAnswer", allEntries = true)
    })
    public Boolean removeQuestionAnswersByQuestionId(UUID questionId) {
        if (questionId == null) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionAnswerConstants.FIELD_QUESTION_ID).is(questionId);
        criteria.and(QuestionAnswerConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        List<QuestionAnswer> answers = mongoTemplate.find(query, QuestionAnswer.class);
        if (CollectionUtils.isEmpty(answers)) {
            return true;
        }

        for (QuestionAnswer answer : answers) {
            answer.setDeleted(DeletedEnum.DELETED.getCode());
            answer.setUpdateTime(LocalDateTime.now());
        }

        questionAnswerRepository.saveAll(answers);

        log.info(QuestionAnswerConstants.LOG_BATCH_DELETE_SUCCESS, answers.size());
        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "QuestionAnswer", allEntries = true)
    })
    public Integer removeQuestionAnswersByIds(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ANSWER_IDS_REQUIRED);
        }

        // 使用批量操作避免N+1问题
        Query query = new Query(Criteria.where(QuestionAnswerConstants.FIELD_ID).in(ids)
                .and(QuestionAnswerConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode()));
        Update update = new Update()
                .set(QuestionAnswerConstants.FIELD_IS_DELETED, DeletedEnum.DELETED.getCode())
                .set(QuestionAnswerConstants.FIELD_UPDATE_TIME, LocalDateTime.now());

        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, QuestionAnswer.class);
        int deletedCount = (int) updateResult.getModifiedCount();

        log.info(QuestionAnswerConstants.LOG_BATCH_DELETE_SUCCESS, deletedCount);
        return deletedCount;
    }

    private List<QuestionAnswerVO> convertToVOList(List<QuestionAnswer> answers) {
        if (CollectionUtils.isEmpty(answers)) {
            return new ArrayList<>();
        }

        Map<UUID, SysUserVO> userMap = getUserMap();

        return answers.stream()
                .map(answer -> convertToVOWithUserMap(answer, userMap))
                .toList();
    }

    private QuestionAnswerVO convertToVO(QuestionAnswer answer) {
        if (answer == null) {
            return null;
        }

        Map<UUID, SysUserVO> userMap = getUserMap();

        return convertToVOWithUserMap(answer, userMap);
    }

    private QuestionAnswerVO convertToVOWithUserMap(QuestionAnswer answer, Map<UUID, SysUserVO> userMap) {
        if (answer == null) {
            return null;
        }

        QuestionAnswerVO vo = new QuestionAnswerVO();
        BeanUtils.copyProperties(answer, vo);

        fillUserInfoWithMap(vo, answer.getSysUserId(), userMap);
        fillIsCorrectName(vo, answer.getIsCorrect());

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

    private void fillUserInfoWithMap(QuestionAnswerVO vo, UUID sysUserId, Map<UUID, SysUserVO> userMap) {
        if (sysUserId == null || userMap.isEmpty()) {
            return;
        }

        SysUserVO user = userMap.get(sysUserId);
        if (user != null) {
            vo.setSysUserName(user.getNickName() != null ? user.getNickName() : user.getUsername());
        }
    }

    private void fillIsCorrectName(QuestionAnswerVO vo, Integer isCorrect) {
        if (isCorrect == null) {
            return;
        }

        switch (isCorrect) {
            case QuestionAnswerConstants.IS_CORRECT_WRONG:
                vo.setIsCorrectName(QuestionAnswerConstants.IS_CORRECT_NAME_WRONG);
                break;
            case QuestionAnswerConstants.IS_CORRECT_CORRECT:
                vo.setIsCorrectName(QuestionAnswerConstants.IS_CORRECT_NAME_CORRECT);
                break;
            case QuestionAnswerConstants.IS_CORRECT_PARTIAL:
                vo.setIsCorrectName(QuestionAnswerConstants.IS_CORRECT_NAME_PARTIAL);
                break;
            default:
                vo.setIsCorrectName("未知");
                break;
        }
    }
}