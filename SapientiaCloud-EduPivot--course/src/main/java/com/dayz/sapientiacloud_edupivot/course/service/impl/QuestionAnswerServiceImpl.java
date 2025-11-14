package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.constant.QuestionAnswerConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.QuestionAnswerDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.QuestionAnswer;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.QuestionAnswerVO;
import com.dayz.sapientiacloud_edupivot.course.enums.QuestionAnswerEnum;
import com.dayz.sapientiacloud_edupivot.course.repository.QuestionAnswerRepository;
import com.dayz.sapientiacloud_edupivot.course.service.IQuestionAnswerService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionAnswerServiceImpl implements IQuestionAnswerService {

    private final QuestionAnswerRepository questionAnswerRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "QuestionAnswer", key = "'question:' + #p0", condition = "#p0 != null")
    public List<QuestionAnswerVO> listQuestionAnswerByQuestionId(UUID questionId) {
        if (questionId == null) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionAnswerConstants.FIELD_QUESTION_ID).is(questionId);
        criteria.and(QuestionAnswerConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, QuestionAnswerConstants.FIELD_SORT_ORDER));

        List<QuestionAnswer> answers = mongoTemplate.find(query, QuestionAnswer.class);
        return convertToVOList(answers);
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

        if (questionAnswerDTO.getScore() != null) {
            validateScore(questionAnswerDTO.getScore());
        }

        Integer sortOrder = questionAnswerDTO.getSortOrder();
        if (sortOrder != null) {
            ensureSortOrderUnique(questionAnswerDTO.getQuestionId(), sortOrder, null);
        }

        QuestionAnswer answer = new QuestionAnswer();
        BeanUtils.copyProperties(questionAnswerDTO, answer);

        answer.setId(UuidCreator.getTimeOrderedEpoch());

        if (answer.getScore() == null) {
            answer.setScore(QuestionAnswerConstants.DEFAULT_SCORE);
        }

        if (answer.getSortOrder() == null) {
            answer.setSortOrder(calculateNextSortOrder(questionAnswerDTO.getQuestionId()));
        }

        LocalDateTime now = LocalDateTime.now();
        answer.setCreateTime(now);
        answer.setUpdateTime(now);
        answer.setDeleted(DeletedEnum.NOT_DELETED.getCode());

        QuestionAnswer savedAnswer = questionAnswerRepository.save(answer);

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

        return result;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "QuestionAnswer", key = "#p0.id"),
            @CacheEvict(value = "QuestionAnswer", key = "'question:' + #p0.questionId"),
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
        if (StringUtils.hasText(questionAnswerDTO.getExplanation())) {
            existingAnswer.setExplanation(questionAnswerDTO.getExplanation());
        }
        if (questionAnswerDTO.getScore() != null) {
            validateScore(questionAnswerDTO.getScore());
            existingAnswer.setScore(questionAnswerDTO.getScore());
        }
        if (questionAnswerDTO.getSortOrder() != null &&
                !Objects.equals(questionAnswerDTO.getSortOrder(), existingAnswer.getSortOrder())) {
            ensureSortOrderUnique(existingAnswer.getQuestionId(), questionAnswerDTO.getSortOrder(), existingAnswer.getId());
            existingAnswer.setSortOrder(questionAnswerDTO.getSortOrder());
        }

        existingAnswer.setUpdateTime(LocalDateTime.now());

        questionAnswerRepository.save(existingAnswer);

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

        LocalDateTime now = LocalDateTime.now();
        for (QuestionAnswer answer : answers) {
            answer.setDeleted(DeletedEnum.DELETED.getCode());
            answer.setUpdateTime(now);
        }

        questionAnswerRepository.saveAll(answers);

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

        Query query = new Query(Criteria.where(QuestionAnswerConstants.FIELD_ID).in(ids)
                .and(QuestionAnswerConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode()));
        Update update = new Update()
                .set(QuestionAnswerConstants.FIELD_IS_DELETED, DeletedEnum.DELETED.getCode())
                .set(QuestionAnswerConstants.FIELD_UPDATE_TIME, LocalDateTime.now());

        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, QuestionAnswer.class);
        return (int) updateResult.getModifiedCount();
    }

    private void validateScore(BigDecimal score) {
        if (score == null) {
            return;
        }
        if (score.compareTo(QuestionAnswerConstants.MIN_SCORE) < 0 ||
                score.compareTo(QuestionAnswerConstants.MAX_SCORE) > 0) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ANSWER_SCORE_INVALID);
        }
    }

    private void ensureSortOrderUnique(UUID questionId, Integer sortOrder, UUID excludeId) {
        if (questionId == null || sortOrder == null) {
            return;
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionAnswerConstants.FIELD_QUESTION_ID).is(questionId);
        criteria.and(QuestionAnswerConstants.FIELD_SORT_ORDER).is(sortOrder);
        criteria.and(QuestionAnswerConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        if (excludeId != null) {
            criteria.and(QuestionAnswerConstants.FIELD_ID).ne(excludeId);
        }
        query.addCriteria(criteria);

        boolean exists = mongoTemplate.exists(query, QuestionAnswer.class);
        if (exists) {
            throw new BusinessException(QuestionAnswerEnum.QUESTION_ANSWER_ALREADY_EXISTS);
        }
    }

    private Integer calculateNextSortOrder(UUID questionId) {
        if (questionId == null) {
            return QuestionAnswerConstants.DEFAULT_SORT_ORDER;
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionAnswerConstants.FIELD_QUESTION_ID).is(questionId);
        criteria.and(QuestionAnswerConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, QuestionAnswerConstants.FIELD_SORT_ORDER));
        query.limit(1);

        QuestionAnswer lastAnswer = mongoTemplate.findOne(query, QuestionAnswer.class);
        if (lastAnswer == null || lastAnswer.getSortOrder() == null) {
            return QuestionAnswerConstants.DEFAULT_SORT_ORDER;
        }

        return lastAnswer.getSortOrder() + 1;
    }

    private List<QuestionAnswerVO> convertToVOList(List<QuestionAnswer> answers) {
        if (CollectionUtils.isEmpty(answers)) {
            return new ArrayList<>();
        }

        return answers.stream()
                .map(this::convertToVO)
                .toList();
    }

    private QuestionAnswerVO convertToVO(QuestionAnswer answer) {
        if (answer == null) {
            return null;
        }

        QuestionAnswerVO vo = new QuestionAnswerVO();
        BeanUtils.copyProperties(answer, vo);
        return vo;
    }
}

