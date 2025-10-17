package com.dayz.sapientiacloud_edupivot.course.service.impl;

import com.dayz.sapientiacloud_edupivot.course.common.clients.SysUserClient;
import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.SysUserVO;
import com.dayz.sapientiacloud_edupivot.course.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.course.common.enums.StatusEnum;
import com.dayz.sapientiacloud_edupivot.course.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.constant.QuestionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.*;
import com.dayz.sapientiacloud_edupivot.course.entity.po.Question;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.QuestionAnswerVO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.QuestionVO;
import com.dayz.sapientiacloud_edupivot.course.enums.QuestionEnum;
import com.dayz.sapientiacloud_edupivot.course.repository.QuestionRepository;
import com.dayz.sapientiacloud_edupivot.course.service.IQuestionAnswerService;
import com.dayz.sapientiacloud_edupivot.course.service.IQuestionOptionService;
import com.dayz.sapientiacloud_edupivot.course.service.IQuestionService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageInfo;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
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
public class QuestionServiceImpl implements IQuestionService {

    private final QuestionRepository questionRepository;
    private final MongoTemplate mongoTemplate;
    private final SysUserClient sysUserClient;
    private final IQuestionOptionService questionOptionService;
    private final IQuestionAnswerService questionAnswerService;

    @Override
    @Transactional(readOnly = true)
    public PageInfo<QuestionVO> listQuestion(QuestionQueryDTO questionQueryDTO) {
        if (questionQueryDTO == null) {
            throw new BusinessException(QuestionEnum.QUESTION_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();

        if (questionQueryDTO.getQuestionBankId() != null) {
            criteria.and(QuestionConstants.FIELD_QUESTION_BANK_ID).is(questionQueryDTO.getQuestionBankId());
        }

        if (StringUtils.hasText(questionQueryDTO.getQuestionTitle())) {
            criteria.and(QuestionConstants.FIELD_QUESTION_TITLE).regex(questionQueryDTO.getQuestionTitle(), QuestionConstants.REGEX_CASE_INSENSITIVE);
        }

        if (questionQueryDTO.getQuestionType() != null) {
            criteria.and(QuestionConstants.FIELD_QUESTION_TYPE).is(questionQueryDTO.getQuestionType());
        }

        if (questionQueryDTO.getDifficulty() != null) {
            criteria.and(QuestionConstants.FIELD_DIFFICULTY).is(questionQueryDTO.getDifficulty());
        }

        if (questionQueryDTO.getStatus() != null) {
            criteria.and(QuestionConstants.FIELD_STATUS).is(questionQueryDTO.getStatus());
        }

        if (!CollectionUtils.isEmpty(questionQueryDTO.getTags())) {
            criteria.and(QuestionConstants.FIELD_TAGS).in(questionQueryDTO.getTags());
        }

        if (StringUtils.hasText(questionQueryDTO.getCreateTimeStart())) {
            criteria.and(QuestionConstants.FIELD_CREATE_TIME).gte(questionQueryDTO.getCreateTimeStart());
        }

        if (StringUtils.hasText(questionQueryDTO.getCreateTimeEnd())) {
            criteria.and(QuestionConstants.FIELD_CREATE_TIME).lte(questionQueryDTO.getCreateTimeEnd());
        }

        criteria.and(QuestionConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());

        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, QuestionConstants.FIELD_CREATE_TIME));

        Query countQuery = new Query();
        countQuery.addCriteria(criteria);

        Pageable pageable = PageRequest.of(
                questionQueryDTO.getPageNum() - QuestionConstants.PAGE_NUM_OFFSET,
                questionQueryDTO.getPageSize()
        );
        query.with(pageable);

        List<Question> questions = mongoTemplate.find(query, Question.class);
        long total = mongoTemplate.count(countQuery, Question.class);

        List<QuestionVO> questionVOList = convertToVOList(questions);

        PageInfo<QuestionVO> pageInfo = new PageInfo<>(questionVOList);
        pageInfo.setTotal(total);
        pageInfo.setPageNum(questionQueryDTO.getPageNum());
        pageInfo.setPageSize(questionQueryDTO.getPageSize());
        pageInfo.setPages((int) Math.ceil((double) total / questionQueryDTO.getPageSize()));

        return pageInfo;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "Question", key = "'all'", condition = "true")
    public List<QuestionVO> listAllQuestion() {
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.DESC, QuestionConstants.FIELD_CREATE_TIME));

        List<Question> questions = mongoTemplate.find(query, Question.class);
        return convertToVOList(questions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "Question", key = "#p0", condition = "#p0 != null")
    public QuestionVO getQuestionById(UUID id) {
        if (id == null) {
            throw new BusinessException(QuestionEnum.QUESTION_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionConstants.FIELD_ID).is(id);
        criteria.and(QuestionConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        Question question = mongoTemplate.findOne(query, Question.class);
        if (question == null) {
            throw new BusinessException(QuestionEnum.QUESTION_NOT_EXISTS);
        }

        return convertToVO(question);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "Question", key = "'bank:' + #p0", condition = "#p0 != null")
    public List<QuestionVO> listQuestionByQuestionBankId(UUID questionBankId) {
        if (questionBankId == null) {
            throw new BusinessException(QuestionEnum.QUESTION_BANK_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionConstants.FIELD_QUESTION_BANK_ID).is(questionBankId);
        criteria.and(QuestionConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);
        query.with(Sort.by(Sort.Direction.ASC, QuestionConstants.FIELD_CREATE_TIME));

        List<Question> questions = mongoTemplate.find(query, Question.class);
        return convertToVOList(questions);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "Question", allEntries = true)
    })
    public QuestionVO addQuestion(QuestionAddDTO questionAddDTO) {
        if (questionAddDTO == null) {
            throw new BusinessException(QuestionEnum.QUESTION_REQUIRED);
        }


        if (questionAddDTO.getQuestionType() < QuestionConstants.QUESTION_TYPE_MIN ||
                questionAddDTO.getQuestionType() > QuestionConstants.QUESTION_TYPE_MAX) {
            throw new BusinessException(QuestionEnum.QUESTION_TYPE_INVALID);
        }

        if (questionAddDTO.getDifficulty() < QuestionConstants.DIFFICULTY_MIN ||
                questionAddDTO.getDifficulty() > QuestionConstants.DIFFICULTY_MAX) {
            throw new BusinessException(QuestionEnum.QUESTION_DIFFICULTY_INVALID);
        }

        // 验证选项和答案的业务逻辑
        validateQuestionOptionsAndAnswer(questionAddDTO);

        // 创建Question实体
        Question question = new Question();
        BeanUtils.copyProperties(questionAddDTO, question);

        // 设置ID
        question.setId(UuidCreator.getTimeOrderedEpoch());

        // 设置默认值
        if (question.getStatus() == null) {
            question.setStatus(StatusEnum.NORMAL.getCode());
        }
        if (question.getViewCount() == null) {
            question.setViewCount(0L);
        }
        if (question.getAllowPartialCredit() == null) {
            question.setAllowPartialCredit(0);
        }
        if (question.getEstimatedTime() == null) {
            question.setEstimatedTime(QuestionConstants.DEFAULT_ESTIMATED_TIME);
        }

        // 设置时间戳
        LocalDateTime now = LocalDateTime.now();
        question.setCreateTime(now);
        question.setUpdateTime(now);
        question.setDeleted(DeletedEnum.NOT_DELETED.getCode());

        // 保存题目
        Question savedQuestion = questionRepository.save(question);

        // 保存选项列表（选择题、判断题需要）
        if (!CollectionUtils.isEmpty(questionAddDTO.getOptions())) {
            List<QuestionOptionDTO> optionDTOs = questionAddDTO.getOptions();
            // 为每个选项设置题目ID
            optionDTOs.forEach(option -> option.setQuestionId(savedQuestion.getId()));

            questionOptionService.addQuestionOptions(optionDTOs);
        }

        // 保存正确答案
        if (questionAddDTO.getQuestionAnswerDTO() != null) {
            QuestionAnswerDTO answerDTO = questionAddDTO.getQuestionAnswerDTO();
            // 设置题目ID和用户ID
            answerDTO.setQuestionId(savedQuestion.getId());

            questionAnswerService.addQuestionAnswer(answerDTO);
        }

        return convertToVO(savedQuestion);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "Question", key = "#p0.id"),
            @CacheEvict(value = "Question", allEntries = true)
    })
    public Boolean updateQuestion(QuestionDTO questionDTO) {
        if (questionDTO == null || questionDTO.getId() == null) {
            throw new BusinessException(QuestionEnum.QUESTION_INFO_OR_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionConstants.FIELD_ID).is(questionDTO.getId());
        criteria.and(QuestionConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        Question existingQuestion = mongoTemplate.findOne(query, Question.class);
        if (existingQuestion == null) {
            throw new BusinessException(QuestionEnum.QUESTION_NOT_EXISTS);
        }

        if (StringUtils.hasText(questionDTO.getQuestionTitle())) {
            existingQuestion.setQuestionTitle(questionDTO.getQuestionTitle());
        }
        if (StringUtils.hasText(questionDTO.getQuestionContent())) {
            existingQuestion.setQuestionContent(questionDTO.getQuestionContent());
        }
        if (questionDTO.getQuestionType() != null) {
            existingQuestion.setQuestionType(questionDTO.getQuestionType());
        }
        if (questionDTO.getDifficulty() != null) {
            existingQuestion.setDifficulty(questionDTO.getDifficulty());
        }
        if (questionDTO.getScore() != null) {
            existingQuestion.setScore(questionDTO.getScore());
        }
        if (questionDTO.getEstimatedTime() != null) {
            existingQuestion.setEstimatedTime(questionDTO.getEstimatedTime());
        }
        if (questionDTO.getTags() != null) {
            existingQuestion.setTags(questionDTO.getTags());
        }
        if (questionDTO.getImageUrls() != null) {
            existingQuestion.setImageUrls(questionDTO.getImageUrls());
        }
        if (questionDTO.getAllowPartialCredit() != null) {
            existingQuestion.setAllowPartialCredit(questionDTO.getAllowPartialCredit());
        }
        if (questionDTO.getStatus() != null) {
            existingQuestion.setStatus(questionDTO.getStatus());
        }

        existingQuestion.setUpdateTime(LocalDateTime.now());

        questionRepository.save(existingQuestion);

        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "Question", allEntries = true)
    })
    public Boolean removeQuestionById(UUID id) {
        if (id == null) {
            throw new BusinessException(QuestionEnum.QUESTION_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionConstants.FIELD_ID).is(id);
        criteria.and(QuestionConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        Question question = mongoTemplate.findOne(query, Question.class);
        if (question == null) {
            throw new BusinessException(QuestionEnum.QUESTION_NOT_EXISTS);
        }

        question.setDeleted(DeletedEnum.DELETED.getCode());
        question.setUpdateTime(LocalDateTime.now());
        questionRepository.save(question);

        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "Question", allEntries = true)
    })
    public Integer removeQuestionByIds(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BusinessException(QuestionEnum.QUESTION_IDS_REQUIRED);
        }

        // 使用批量操作避免N+1问题
        Query query = new Query(Criteria.where(QuestionConstants.FIELD_ID).in(ids)
                .and(QuestionConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode()));
        Update update = new Update()
                .set(QuestionConstants.FIELD_IS_DELETED, DeletedEnum.DELETED.getCode())
                .set(QuestionConstants.FIELD_UPDATE_TIME, LocalDateTime.now());

        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, Question.class);
        int deletedCount = (int) updateResult.getModifiedCount();

        return deletedCount;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "Question", key = "#p0"),
            @CacheEvict(value = "Question", allEntries = true)
    })
    public Boolean publishQuestion(UUID id) {
        if (id == null) {
            throw new BusinessException(QuestionEnum.QUESTION_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionConstants.FIELD_ID).is(id);
        criteria.and(QuestionConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        Question question = mongoTemplate.findOne(query, Question.class);
        if (question == null) {
            throw new BusinessException(QuestionEnum.QUESTION_NOT_EXISTS);
        }

        question.setStatus(StatusEnum.NORMAL.getCode());
        question.setUpdateTime(LocalDateTime.now());
        questionRepository.save(question);

        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "Question", key = "#p0"),
            @CacheEvict(value = "Question", allEntries = true)
    })
    public Boolean unpublishQuestion(UUID id) {
        if (id == null) {
            throw new BusinessException(QuestionEnum.QUESTION_ID_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and(QuestionConstants.FIELD_ID).is(id);
        criteria.and(QuestionConstants.FIELD_IS_DELETED).is(DeletedEnum.NOT_DELETED.getCode());
        query.addCriteria(criteria);

        Question question = mongoTemplate.findOne(query, Question.class);
        if (question == null) {
            throw new BusinessException(QuestionEnum.QUESTION_NOT_EXISTS);
        }

        question.setStatus(2);
        question.setUpdateTime(LocalDateTime.now());
        questionRepository.save(question);

        return true;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "Question", key = "#p0"),
            @CacheEvict(value = "Question", allEntries = true)
    })
    public Boolean viewQuestion(UUID id) {
        if (id == null) {
            throw new BusinessException(QuestionEnum.QUESTION_ID_REQUIRED);
        }

        Query query = new Query(Criteria.where(QuestionConstants.FIELD_ID).is(id));
        Update update = new Update().inc(QuestionConstants.FIELD_VIEW_COUNT, QuestionConstants.INCREMENT_VALUE);

        mongoTemplate.updateFirst(query, update, Question.class);

        return true;
    }

    private List<QuestionVO> convertToVOList(List<Question> questions) {
        if (CollectionUtils.isEmpty(questions)) {
            return new ArrayList<>();
        }

        Map<UUID, SysUserVO> userMap = getUserMap();

        return questions.stream()
                .map(question -> convertToVOWithUserMap(question, userMap))
                .toList();
    }

    private QuestionVO convertToVO(Question question) {
        if (question == null) {
            return null;
        }

        Map<UUID, SysUserVO> userMap = getUserMap();

        return convertToVOWithUserMap(question, userMap);
    }

    private QuestionVO convertToVOWithUserMap(Question question, Map<UUID, SysUserVO> userMap) {
        if (question == null) {
            return null;
        }

        QuestionVO vo = new QuestionVO();
        BeanUtils.copyProperties(question, vo);

        fillUserInfoWithMap(vo, question.getSysUserId(), userMap);
        fillAnswerInfo(vo, question.getId());

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

    private void fillUserInfoWithMap(QuestionVO vo, UUID sysUserId, Map<UUID, SysUserVO> userMap) {
        if (sysUserId == null || userMap.isEmpty()) {
            return;
        }

        SysUserVO user = userMap.get(sysUserId);
        if (user != null) {
            vo.setSysUserName(user.getNickName() != null ? user.getNickName() : user.getUsername());
        }
    }

    private void fillAnswerInfo(QuestionVO vo, UUID questionId) {
        if (questionId == null) {
            return;
        }

        // 获取题目的所有答案，通常取第一个作为标准答案
        List<QuestionAnswerVO> answers = questionAnswerService.listAllQuestionAnswerByQuestionId(questionId);
        if (!CollectionUtils.isEmpty(answers)) {
            // 取第一个答案作为标准答案
            vo.setAnswer(answers.get(0));
        }
    }

    /**
     * 验证题目选项和答案的业务逻辑
     *
     * @param questionAddDTO 题目新增DTO
     */
    private void validateQuestionOptionsAndAnswer(QuestionAddDTO questionAddDTO) {
        Integer questionType = questionAddDTO.getQuestionType();

        // 选择题（单选题、多选题、判断题）需要选项
        if (questionType == 0 || questionType == 1 || questionType == 2) {
            if (CollectionUtils.isEmpty(questionAddDTO.getOptions())) {
                throw new BusinessException(QuestionEnum.QUESTION_OPTION_SAVE_FAILED);
            }

            // 验证选项数量
            if (questionType == 0 && questionAddDTO.getOptions().size() < 2) {
                throw new BusinessException(QuestionEnum.QUESTION_OPTION_SAVE_FAILED);
            }
            if (questionType == 1 && questionAddDTO.getOptions().size() < 2) {
                throw new BusinessException(QuestionEnum.QUESTION_OPTION_SAVE_FAILED);
            }
            if (questionType == 2 && questionAddDTO.getOptions().size() != 2) {
                throw new BusinessException(QuestionEnum.QUESTION_OPTION_SAVE_FAILED);
            }

            // 验证是否有正确答案
            boolean hasCorrectOption = questionAddDTO.getOptions().stream()
                    .anyMatch(option -> option.getIsCorrect() != null && option.getIsCorrect() == 1);
            if (!hasCorrectOption) {
                throw new BusinessException(QuestionEnum.QUESTION_OPTION_SAVE_FAILED);
            }
        }

        // 所有题目都需要正确答案
        if (questionAddDTO.getQuestionAnswerDTO() == null) {
            throw new BusinessException(QuestionEnum.QUESTION_ANSWER_SAVE_FAILED);
        }

        // 填空题和简答题需要答案内容
        if (questionType == 3 || questionType == 4) {
            QuestionAnswerDTO answerDTO = questionAddDTO.getQuestionAnswerDTO();
            if (!StringUtils.hasText(answerDTO.getAnswerContent()) && !StringUtils.hasText(answerDTO.getAnswerText())) {
                throw new BusinessException(QuestionEnum.QUESTION_ANSWER_SAVE_FAILED);
            }
        }
    }
}