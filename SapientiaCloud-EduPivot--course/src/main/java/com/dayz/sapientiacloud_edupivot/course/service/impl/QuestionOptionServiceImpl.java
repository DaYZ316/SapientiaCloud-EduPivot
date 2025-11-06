package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.constant.QuestionOptionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.QuestionOptionDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.QuestionOption;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.QuestionOptionVO;
import com.dayz.sapientiacloud_edupivot.course.enums.QuestionOptionEnum;
import com.dayz.sapientiacloud_edupivot.course.repository.QuestionOptionRepository;
import com.dayz.sapientiacloud_edupivot.course.service.IQuestionOptionService;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionOptionServiceImpl implements IQuestionOptionService {

    private final QuestionOptionRepository questionOptionRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "QuestionOption", key = "'question:' + #p0", condition = "#p0 != null")
    public List<QuestionOptionVO> listQuestionOptionByQuestionId(UUID questionId) {
        if (questionId == null) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionOptionConstants.FIELD_QUESTION_ID).is(questionId);
        criteria.and(QuestionOptionConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, QuestionOptionConstants.FIELD_OPTION_LABEL));

        List<QuestionOption> options = mongoTemplate.find(query, QuestionOption.class);
        return convertToVOList(options);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "QuestionOption", key = "#p0", condition = "#p0 != null")
    public QuestionOptionVO getQuestionOptionById(UUID id) {
        if (id == null) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_OPTION_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionOptionConstants.FIELD_ID).is(id);
        criteria.and(QuestionOptionConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        QuestionOption option = mongoTemplate.findOne(query, QuestionOption.class);
        if (option == null) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_OPTION_NOT_EXISTS);
        }

        return convertToVO(option);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "QuestionOption", key = "'question:' + #p0.questionId"),
            @CacheEvict(value = "QuestionOption", allEntries = true)
    })
    public QuestionOptionVO addQuestionOption(QuestionOptionDTO questionOptionDTO) {
        if (questionOptionDTO == null) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_OPTION_REQUIRED);
        }

        if (questionOptionDTO.getQuestionId() == null) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_ID_REQUIRED);
        }

        if (!StringUtils.hasText(questionOptionDTO.getOptionContent())) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_OPTION_CONTENT_REQUIRED);
        }

        if (!StringUtils.hasText(questionOptionDTO.getOptionLabel())) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_OPTION_LABEL_REQUIRED);
        }

        if (questionOptionDTO.getIsCorrect() == null) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_OPTION_IS_CORRECT_REQUIRED);
        }

        if (questionOptionDTO.getIsCorrect() < QuestionOptionConstants.IS_CORRECT_MIN ||
                questionOptionDTO.getIsCorrect() > QuestionOptionConstants.IS_CORRECT_MAX) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_OPTION_IS_CORRECT_REQUIRED);
        }

        Query existingQuery = new Query();
        Criteria existingCriteria = new Criteria();
        existingCriteria.and(QuestionOptionConstants.FIELD_QUESTION_ID).is(questionOptionDTO.getQuestionId());
        existingCriteria.and(QuestionOptionConstants.FIELD_OPTION_LABEL).is(questionOptionDTO.getOptionLabel());
        existingCriteria.and(QuestionOptionConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        existingQuery.addCriteria(existingCriteria);

        QuestionOption existingOption = mongoTemplate.findOne(existingQuery, QuestionOption.class);
        if (existingOption != null) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_OPTION_LABEL_DUPLICATE);
        }

        QuestionOption option = new QuestionOption();
        BeanUtils.copyProperties(questionOptionDTO, option);

        option.setId(UuidCreator.getTimeOrderedEpoch());

        if (option.getScore() == null) {
            option.setScore(option.getIsCorrect() == QuestionOptionConstants.IS_CORRECT_CORRECT ?
                    BigDecimal.valueOf(100) : BigDecimal.ZERO);
        }

        LocalDateTime now = LocalDateTime.now();
        option.setCreateTime(now);
        option.setUpdateTime(now);
        option.setDeleted(DeletedEnum.NOT_DELETED.getCode());

        QuestionOption savedOption = questionOptionRepository.save(option);

        return convertToVO(savedOption);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "QuestionOption", allEntries = true)
    })
    public List<QuestionOptionVO> addQuestionOptions(List<QuestionOptionDTO> questionOptionDTOList) {
        if (CollectionUtils.isEmpty(questionOptionDTOList)) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_OPTION_REQUIRED);
        }

        List<QuestionOptionVO> result = new ArrayList<>();
        for (QuestionOptionDTO dto : questionOptionDTOList) {
            QuestionOptionVO vo = addQuestionOption(dto);
            result.add(vo);
        }

        return result;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "QuestionOption", key = "#p0.id"),
            @CacheEvict(value = "QuestionOption", key = "'question:' + #p0.questionId"),
            @CacheEvict(value = "QuestionOption", allEntries = true)
    })
    public Boolean updateQuestionOption(QuestionOptionDTO questionOptionDTO) {
        if (questionOptionDTO == null || questionOptionDTO.getId() == null) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_OPTION_INFO_OR_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionOptionConstants.FIELD_ID).is(questionOptionDTO.getId());
        criteria.and(QuestionOptionConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        QuestionOption existingOption = mongoTemplate.findOne(query, QuestionOption.class);
        if (existingOption == null) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_OPTION_NOT_EXISTS);
        }

        if (StringUtils.hasText(questionOptionDTO.getOptionContent())) {
            existingOption.setOptionContent(questionOptionDTO.getOptionContent());
        }
        if (StringUtils.hasText(questionOptionDTO.getOptionLabel())) {
            existingOption.setOptionLabel(questionOptionDTO.getOptionLabel());
        }
        if (questionOptionDTO.getIsCorrect() != null) {
            existingOption.setIsCorrect(questionOptionDTO.getIsCorrect());
        }
        if (questionOptionDTO.getScore() != null) {
            existingOption.setScore(questionOptionDTO.getScore());
        }
        if (questionOptionDTO.getImageUrls() != null) {
            existingOption.setImageUrls(questionOptionDTO.getImageUrls());
        }
        if (StringUtils.hasText(questionOptionDTO.getExplanation())) {
            existingOption.setExplanation(questionOptionDTO.getExplanation());
        }

        existingOption.setUpdateTime(LocalDateTime.now());

        questionOptionRepository.save(existingOption);

        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "QuestionOption", allEntries = true)
    })
    public Boolean removeQuestionOptionById(UUID id) {
        if (id == null) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_OPTION_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionOptionConstants.FIELD_ID).is(id);
        criteria.and(QuestionOptionConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        QuestionOption option = mongoTemplate.findOne(query, QuestionOption.class);
        if (option == null) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_OPTION_NOT_EXISTS);
        }

        option.setDeleted(DeletedEnum.DELETED.getCode());
        option.setUpdateTime(LocalDateTime.now());
        questionOptionRepository.save(option);

        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "QuestionOption", key = "'question:' + #p0"),
            @CacheEvict(value = "QuestionOption", allEntries = true)
    })
    public Boolean removeQuestionOptionsByQuestionId(UUID questionId) {
        if (questionId == null) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionOptionConstants.FIELD_QUESTION_ID).is(questionId);
        criteria.and(QuestionOptionConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        List<QuestionOption> options = mongoTemplate.find(query, QuestionOption.class);
        if (CollectionUtils.isEmpty(options)) {
            return true;
        }

        for (QuestionOption option : options) {
            option.setDeleted(DeletedEnum.DELETED.getCode());
            option.setUpdateTime(LocalDateTime.now());
        }

        questionOptionRepository.saveAll(options);

        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "QuestionOption", allEntries = true)
    })
    public Integer removeQuestionOptionsByIds(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(QuestionOptionEnum.QUESTION_OPTION_IDS_REQUIRED);
        }

        // 使用批量操作避免N+1问题
        Query query = new Query(Criteria.where(QuestionOptionConstants.FIELD_ID).in(ids)
                .and(QuestionOptionConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode()));
        Update update = new Update()
                .set(QuestionOptionConstants.FIELD_IS_DELETED, DeletedEnum.DELETED.getCode())
                .set(QuestionOptionConstants.FIELD_UPDATE_TIME, LocalDateTime.now());

        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, QuestionOption.class);
        int deletedCount = (int) updateResult.getModifiedCount();

        return deletedCount;
    }

    private List<QuestionOptionVO> convertToVOList(List<QuestionOption> options) {
        if (CollectionUtils.isEmpty(options)) {
            return new ArrayList<>();
        }

        return options.stream()
                .map(this::convertToVO)
                .toList();
    }

    private QuestionOptionVO convertToVO(QuestionOption option) {
        if (option == null) {
            return null;
        }

        QuestionOptionVO vo = new QuestionOptionVO();
        BeanUtils.copyProperties(option, vo);

        return vo;
    }
}
