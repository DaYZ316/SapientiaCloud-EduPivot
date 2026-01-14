package com.dayz.sapientiacloud_edupivot.student.service.impl;

import com.dayz.sapientiacloud_edupivot.student.common.clients.QuestionClient;
import com.dayz.sapientiacloud_edupivot.student.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.student.common.result.Result;
import com.dayz.sapientiacloud_edupivot.student.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.student.constant.QuestionStudentConstants;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentAddDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.po.QuestionStudent;
import com.dayz.sapientiacloud_edupivot.student.entity.vo.PracticeStatisticsVO;
import com.dayz.sapientiacloud_edupivot.student.entity.vo.QuestionStudentVO;
import com.dayz.sapientiacloud_edupivot.student.entity.vo.StudentVO;
import com.dayz.sapientiacloud_edupivot.student.enums.AnswerStatusEnum;
import com.dayz.sapientiacloud_edupivot.student.enums.StudentPracticeEnum;
import com.dayz.sapientiacloud_edupivot.student.repository.QuestionStudentRepository;
import com.dayz.sapientiacloud_edupivot.student.service.IQuestionStudentService;
import com.dayz.sapientiacloud_edupivot.student.service.IStudentService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionStudentServiceImpl implements IQuestionStudentService {

    private final QuestionStudentRepository questionStudentRepository;
    private final IStudentService studentService;
    private final QuestionClient questionClient;
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "QuestionStudent", key = "'summary:' + 'me'", condition = "true")
    public List<QuestionStudentVO> summaryMy() {
        UUID currentUserId = UserContextUtil.getCurrentUserId();
        StudentVO studentVO = studentService.getStudentByUserId(currentUserId);
        if (studentVO == null || studentVO.getId() == null) {
            throw new BusinessException(StudentPracticeEnum.STUDENT_NOT_FOUND);
        }
        List<QuestionStudent> list = questionStudentRepository.findByStudentIdAndIsDeleted(studentVO.getId(), QuestionStudentConstants.STATUS_NOT_DELETED);
        return convertToVOList(list);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "QuestionStudent", key = "#p0", condition = "#p0 != null")
    public List<QuestionStudentVO> listByPractice(UUID practiceId) {
        if (practiceId == null) {
            throw new BusinessException(StudentPracticeEnum.PRACTICE_ID_REQUIRED);
        }
        List<QuestionStudent> list = questionStudentRepository.findByPracticeIdAndIsDeleted(practiceId, QuestionStudentConstants.STATUS_NOT_DELETED);
        return convertToVOList(list);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "QuestionStudent", key = "#p0", condition = "#p0 != null")
    public List<QuestionStudentVO> listByCourse(UUID courseId) {
        if (courseId == null) {
            throw new BusinessException(StudentPracticeEnum.COURSE_ID_REQUIRED);
        }
        List<QuestionStudent> list = questionStudentRepository.findByCourseIdAndIsDeleted(courseId, QuestionStudentConstants.STATUS_NOT_DELETED);
        return convertToVOList(list);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "QuestionStudent", key = "#p0", condition = "#p0 != null")
    public List<QuestionStudentVO> listByClassroom(UUID classroomId) {
        if (classroomId == null) {
            throw new BusinessException(StudentPracticeEnum.CLASSROOM_ID_REQUIRED);
        }
        List<QuestionStudent> list = questionStudentRepository.findByClassroomIdAndIsDeleted(classroomId, QuestionStudentConstants.STATUS_NOT_DELETED);
        return convertToVOList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionStudentVO> list(QuestionStudentQueryDTO queryDTO) {
        UUID classroomId = queryDTO != null ? queryDTO.getClassroomId() : null;
        UUID studentId = queryDTO != null ? queryDTO.getStudentId() : null;
        UUID questionId = queryDTO != null ? queryDTO.getQuestionId() : null;
        UUID practiceId = queryDTO != null ? queryDTO.getPracticeId() : null;
        UUID courseId = queryDTO != null ? queryDTO.getCourseId() : null;

        List<QuestionStudent> list;
        if (practiceId != null && studentId != null) {
            list = questionStudentRepository.findByPracticeIdAndStudentIdAndIsDeleted(practiceId, studentId, QuestionStudentConstants.STATUS_NOT_DELETED);
        } else if (practiceId != null) {
            list = questionStudentRepository.findByPracticeIdAndIsDeleted(practiceId, QuestionStudentConstants.STATUS_NOT_DELETED);
        } else if (courseId != null) {
            list = questionStudentRepository.findByCourseIdAndIsDeleted(courseId, QuestionStudentConstants.STATUS_NOT_DELETED);
        } else if (classroomId != null && studentId != null && questionId != null) {
            QuestionStudent record = questionStudentRepository.findByClassroomIdAndStudentIdAndQuestionIdAndIsDeleted(classroomId, studentId, questionId, QuestionStudentConstants.STATUS_NOT_DELETED);
            list = record == null ? List.of() : List.of(record);
        } else if (classroomId != null && studentId != null) {
            list = questionStudentRepository.findByClassroomIdAndStudentIdAndIsDeleted(classroomId, studentId, QuestionStudentConstants.STATUS_NOT_DELETED);
        } else if (classroomId != null) {
            list = questionStudentRepository.findByClassroomIdAndIsDeleted(classroomId, QuestionStudentConstants.STATUS_NOT_DELETED);
        } else if (studentId != null) {
            list = questionStudentRepository.findByStudentIdAndIsDeleted(studentId, QuestionStudentConstants.STATUS_NOT_DELETED);
        } else {
            list = questionStudentRepository.findByIsDeleted(QuestionStudentConstants.STATUS_NOT_DELETED);
        }

        return convertToVOList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public PageInfo<QuestionStudentVO> listPaged(QuestionStudentQueryDTO queryDTO) {
        if (queryDTO == null) {
            throw new BusinessException(StudentPracticeEnum.QUERY_DTO_REQUIRED);
        }

        Query query = new Query();
        Criteria criteria = Criteria.where(QuestionStudentConstants.FIELD_IS_DELETED).is(QuestionStudentConstants.STATUS_NOT_DELETED);

        if (queryDTO.getClassroomId() != null) {
            criteria.and(QuestionStudentConstants.FIELD_CLASSROOM_ID).is(queryDTO.getClassroomId());
        }

        if (queryDTO.getStudentId() != null) {
            criteria.and(QuestionStudentConstants.FIELD_STUDENT_ID).is(queryDTO.getStudentId());
        }

        if (queryDTO.getQuestionId() != null) {
            criteria.and(QuestionStudentConstants.FIELD_QUESTION_ID).is(queryDTO.getQuestionId());
        }

        if (queryDTO.getPracticeId() != null) {
            criteria.and(QuestionStudentConstants.FIELD_PRACTICE_ID).is(queryDTO.getPracticeId());
        }

        if (queryDTO.getCourseId() != null) {
            criteria.and(QuestionStudentConstants.FIELD_COURSE_ID).is(queryDTO.getCourseId());
        }

        if (queryDTO.getIsCorrect() != null) {
            criteria.and(QuestionStudentConstants.FIELD_IS_CORRECT).is(queryDTO.getIsCorrect());
        }

        query.addCriteria(criteria);

        // 添加排序，按创建时间倒序
        query.with(Sort.by(Sort.Order.desc(QuestionStudentConstants.FIELD_CREATE_TIME)));

        // 创建分页对象
        Pageable pageable = PageRequest.of(
                queryDTO.getPageNum() - 1,
                queryDTO.getPageSize()
        );
        query.with(pageable);

        // 执行分页查询
        long total = mongoTemplate.count(new Query().addCriteria(criteria), QuestionStudent.class);
        List<QuestionStudent> questionStudents = mongoTemplate.find(query, QuestionStudent.class);

        // 转换为VO
        List<QuestionStudentVO> voList = convertToVOList(questionStudents);

        // 构造PageInfo
        PageInfo<QuestionStudentVO> pageInfo = new PageInfo<>();
        pageInfo.setList(voList);
        pageInfo.setTotal(total);
        pageInfo.setPageNum(queryDTO.getPageNum());
        pageInfo.setPageSize(queryDTO.getPageSize());
        pageInfo.setPages((int) Math.ceil((double) total / queryDTO.getPageSize()));

        return pageInfo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionStudentVO> listAll() {
        List<QuestionStudent> list = questionStudentRepository.findByIsDeleted(QuestionStudentConstants.STATUS_NOT_DELETED);
        return convertToVOList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionStudentVO getById(UUID id) {
        if (id == null) {
            throw new BusinessException(StudentPracticeEnum.SUBMISSION_NOT_FOUND);
        }
        QuestionStudent questionStudent = questionStudentRepository.findById(id)
                .filter(q -> q.getIsDeleted() != null && q.getIsDeleted() == QuestionStudentConstants.STATUS_NOT_DELETED)
                .orElseThrow(() -> new BusinessException(StudentPracticeEnum.SUBMISSION_NOT_FOUND));

        // 获取学生信息并设置学生姓名
        Map<UUID, StudentVO> studentMap = new HashMap<>();
        if (questionStudent.getStudentId() != null) {
            try {
                StudentVO studentVO = studentService.getStudentById(questionStudent.getStudentId());
                if (studentVO != null) {
                    studentMap.put(questionStudent.getStudentId(), studentVO);
                }
            } catch (Exception e) {
                log.warn("获取学生信息失败: studentId={}, error={}", questionStudent.getStudentId(), e.getMessage());
            }
        }

        return convertToVO(questionStudent, studentMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "QuestionStudent", allEntries = true)
    public Boolean add(QuestionStudentAddDTO dto) {
        if (dto == null) {
            throw new BusinessException(StudentPracticeEnum.SUBMISSION_NOT_FOUND);
        }
        LocalDateTime now = LocalDateTime.now();
        QuestionStudent entity = new QuestionStudent();
        entity.setId(UuidCreator.getTimeOrderedEpoch());
        entity.setClassroomId(dto.getClassroomId());
        entity.setStudentId(dto.getStudentId());
        entity.setQuestionId(dto.getQuestionId());
        entity.setPracticeId(dto.getPracticeId());
        entity.setCourseId(dto.getCourseId());
        entity.setAnswer(dto.getAnswer());
        entity.setIsCorrect(dto.getIsCorrect());
        entity.setScore(dto.getScore());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setIsDeleted(QuestionStudentConstants.STATUS_NOT_DELETED);

        // 尝试自动批阅：对于填空题、选择题、判断题，通过答案对比直接生成分数
        try {
            // 获取题目信息
            Result<Map<String, Object>> questionResult = questionClient.getQuestionById(dto.getQuestionId());
            if (questionResult != null && questionResult.isSuccess() && questionResult.getData() != null) {
                Map<String, Object> questionData = questionResult.getData();
                Integer questionType = questionData.get("questionType") != null ?
                        Integer.valueOf(questionData.get("questionType").toString()) : null;

                // 题目类型：0=单选题, 1=多选题, 2=判断题, 3=填空题, 4=简答题
                if (questionType != null) {
                    if (questionType <= 3) {
                        // 进行自动批阅
                        AutoReviewResult reviewResult = autoReview(questionData, dto.getAnswer(), questionType);
                        if (reviewResult != null) {
                            entity.setIsCorrect(reviewResult.isCorrect);
                            entity.setScore(reviewResult.score);
                            log.info("自动批阅成功: questionId={}, questionType={}, score={}, isCorrect={}",
                                    dto.getQuestionId(), questionType, reviewResult.score, reviewResult.isCorrect);
                        }
                    } else if (questionType == QuestionStudentConstants.QUESTION_TYPE_ESSAY) {
                        // 简答题设置为待批阅状态
                        entity.setIsCorrect(QuestionStudentConstants.ANSWER_PENDING_REVIEW);
                        entity.setScore(null);
                        log.info("简答题设置为待批阅: questionId={}, questionType={}", dto.getQuestionId(), questionType);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("自动批阅失败: questionId={}, error={}", dto.getQuestionId(), e.getMessage());
        }

        // 如果 DTO 中已经提供了分数和正确性，优先使用
        if (dto.getScore() != null) {
            entity.setScore(dto.getScore());
        }
        if (dto.getIsCorrect() != null) {
            entity.setIsCorrect(dto.getIsCorrect());
        }

        questionStudentRepository.save(entity);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "QuestionStudent", allEntries = true)
    public Boolean update(QuestionStudentDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new BusinessException(StudentPracticeEnum.SUBMISSION_NOT_FOUND);
        }
        QuestionStudent existing = questionStudentRepository.findById(dto.getId())
                .filter(q -> q.getIsDeleted() != null && q.getIsDeleted() == QuestionStudentConstants.STATUS_NOT_DELETED)
                .orElseThrow(() -> new BusinessException(StudentPracticeEnum.SUBMISSION_NOT_FOUND));
        existing.setClassroomId(dto.getClassroomId());
        existing.setStudentId(dto.getStudentId());
        existing.setQuestionId(dto.getQuestionId());
        existing.setPracticeId(dto.getPracticeId());
        existing.setCourseId(dto.getCourseId());
        existing.setAnswer(dto.getAnswer());
        existing.setIsCorrect(dto.getIsCorrect());
        existing.setScore(dto.getScore());
        existing.setUpdateTime(LocalDateTime.now());
        questionStudentRepository.save(existing);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "QuestionStudent", allEntries = true)
    public Boolean removeById(UUID id) {
        QuestionStudent existing = questionStudentRepository.findById(id)
                .filter(q -> q.getIsDeleted() != null && q.getIsDeleted() == QuestionStudentConstants.STATUS_NOT_DELETED)
                .orElseThrow(() -> new BusinessException(StudentPracticeEnum.SUBMISSION_NOT_FOUND));
        existing.setIsDeleted(QuestionStudentConstants.STATUS_DELETED);
        existing.setUpdateTime(LocalDateTime.now());
        questionStudentRepository.save(existing);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "QuestionStudent", allEntries = true)
    public Integer removeByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        List<QuestionStudent> list = questionStudentRepository.findAllById(ids).stream()
                .filter(q -> q.getIsDeleted() != null && q.getIsDeleted() == QuestionStudentConstants.STATUS_NOT_DELETED)
                .collect(Collectors.toList());
        LocalDateTime now = LocalDateTime.now();
        list.forEach(q -> {
            q.setIsDeleted(QuestionStudentConstants.STATUS_DELETED);
            q.setUpdateTime(now);
        });
        questionStudentRepository.saveAll(list);
        return list.size();
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean existsByQuestionIdAndStudentId(UUID questionId, UUID studentId) {
        if (questionId == null || studentId == null) {
            return false;
        }
        List<QuestionStudent> list = questionStudentRepository.findByQuestionIdAndStudentIdAndIsDeleted(questionId, studentId, QuestionStudentConstants.STATUS_NOT_DELETED);
        return list != null && !list.isEmpty();
    }

    private List<QuestionStudentVO> convertToVOList(List<QuestionStudent> list) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量获取学生信息，避免N+1查询
        Set<UUID> studentIds = list.stream()
                .map(QuestionStudent::getStudentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, StudentVO> studentMap = new HashMap<>();
        for (UUID studentId : studentIds) {
            try {
                StudentVO studentVO = studentService.getStudentById(studentId);
                if (studentVO != null) {
                    studentMap.put(studentId, studentVO);
                }
            } catch (Exception e) {
                log.warn("获取学生信息失败: studentId={}, error={}", studentId, e.getMessage());
            }
        }

        return list.stream().map(entity -> convertToVO(entity, studentMap)).collect(Collectors.toList());
    }

    private QuestionStudentVO convertToVO(QuestionStudent entity, Map<UUID, StudentVO> studentMap) {
        if (entity == null) {
            return null;
        }
        QuestionStudentVO vo = new QuestionStudentVO();
        BeanUtils.copyProperties(entity, vo);

        // 设置学生真实姓名
        if (entity.getStudentId() != null && studentMap != null) {
            StudentVO studentVO = studentMap.get(entity.getStudentId());
            if (studentVO != null && studentVO.getRealName() != null) {
                vo.setStudentRealName(studentVO.getRealName());
            }
        }

        return vo;
    }

    /**
     * 自动批阅方法
     *
     * @param questionData  题目数据
     * @param studentAnswer 学生答案
     * @param questionType  题目类型 (0=单选题, 1=多选题, 2=判断题, 3=填空题)
     * @return 批阅结果
     */
    private AutoReviewResult autoReview(Map<String, Object> questionData,
                                        QuestionStudent.AnswerPayload studentAnswer,
                                        Integer questionType) {
        if (studentAnswer == null || questionData == null) {
            return null;
        }

        try {
            BigDecimal questionScore = questionData.get("score") != null ?
                    new BigDecimal(questionData.get("score").toString()) : BigDecimal.ZERO;

            if (QuestionStudentConstants.QUESTION_TYPE_SINGLE_CHOICE.equals(questionType)) {
                return reviewSingleChoice(questionData, studentAnswer, questionScore);
            } else if (QuestionStudentConstants.QUESTION_TYPE_MULTIPLE_CHOICE.equals(questionType)) {
                return reviewMultipleChoice(questionData, studentAnswer, questionScore);
            } else if (QuestionStudentConstants.QUESTION_TYPE_TRUE_FALSE.equals(questionType)) {
                return reviewTrueFalse(questionData, studentAnswer, questionScore);
            } else if (QuestionStudentConstants.QUESTION_TYPE_FILL_BLANK.equals(questionType)) {
                return reviewFillBlank(questionData, studentAnswer, questionScore);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("自动批阅异常: questionType={}, error={}", questionType, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 批阅单选题
     */
    private AutoReviewResult reviewSingleChoice(Map<String, Object> questionData,
                                                QuestionStudent.AnswerPayload studentAnswer,
                                                BigDecimal questionScore) {
        // 获取正确答案选项标签（学生答案格式：{"options": ["D"]}，其中"D"是选项标签）
        List<Map<String, Object>> options = getOptionsFromQuestion(questionData);
        String correctOptionLabel = options.stream()
                .filter(opt -> opt.get(QuestionStudentConstants.OPTION_IS_CORRECT) != null &&
                        Integer.parseInt(opt.get(QuestionStudentConstants.OPTION_IS_CORRECT).toString()) == QuestionStudentConstants.ANSWER_CORRECT)
                .map(opt -> opt.get(QuestionStudentConstants.OPTION_LABEL) != null ? opt.get(QuestionStudentConstants.OPTION_LABEL).toString().trim() : null)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (correctOptionLabel == null) {
            return null;
        }

        // 学生答案（格式：{"options": ["D"]}，其中"D"是选项标签）
        String studentOptionLabel = studentAnswer.getOptions() != null && !studentAnswer.getOptions().isEmpty() ?
                studentAnswer.getOptions().get(0).trim() : null;

        if (studentOptionLabel == null) {
            return new AutoReviewResult(AnswerStatusEnum.INCORRECT.getCode(), 0f);
        }

        boolean isCorrect = correctOptionLabel.equalsIgnoreCase(studentOptionLabel);
        Float score = isCorrect ? questionScore.floatValue() : 0f;
        return new AutoReviewResult(getAnswerStatusByScore(score, questionScore.floatValue()), score);
    }

    /**
     * 批阅多选题
     */
    private AutoReviewResult reviewMultipleChoice(Map<String, Object> questionData,
                                                  QuestionStudent.AnswerPayload studentAnswer,
                                                  BigDecimal questionScore) {
        // 获取所有选项
        List<Map<String, Object>> options = getOptionsFromQuestion(questionData);
        if (options.isEmpty()) {
            return null;
        }

        // 分离正确选项和错误选项
        Map<String, Map<String, Object>> correctOptions = new HashMap<>();
        Map<String, Map<String, Object>> incorrectOptions = new HashMap<>();

        for (Map<String, Object> option : options) {
            String optionLabel = option.get(QuestionStudentConstants.OPTION_LABEL) != null ?
                    option.get(QuestionStudentConstants.OPTION_LABEL).toString().trim() : null;
            if (optionLabel == null) {
                continue;
            }

            Integer isCorrect = option.get(QuestionStudentConstants.OPTION_IS_CORRECT) != null ?
                    Integer.parseInt(option.get(QuestionStudentConstants.OPTION_IS_CORRECT).toString()) : QuestionStudentConstants.ANSWER_INCORRECT;

            if (isCorrect == QuestionStudentConstants.ANSWER_CORRECT) {
                correctOptions.put(optionLabel.toLowerCase(), option);
            } else {
                incorrectOptions.put(optionLabel.toLowerCase(), option);
            }
        }

        if (correctOptions.isEmpty()) {
            return null;
        }

        // 学生答案（格式：{"options": ["A", "C"]}，其中"A"、"C"是选项标签）
        Set<String> studentOptionLabels = studentAnswer.getOptions() != null ?
                studentAnswer.getOptions().stream()
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .collect(Collectors.toSet()) : new HashSet<>();

        // 检查是否选择了错误选项 - 如果是，全错
        boolean hasIncorrectSelection = studentOptionLabels.stream()
                .anyMatch(label -> incorrectOptions.containsKey(label));

        if (hasIncorrectSelection) {
            return new AutoReviewResult(AnswerStatusEnum.INCORRECT.getCode(), 0f);
        }

        // 计算选中的正确选项的分数
        BigDecimal totalScore = BigDecimal.ZERO;
        int correctSelectedCount = 0;

        for (String studentLabel : studentOptionLabels) {
            Map<String, Object> correctOption = correctOptions.get(studentLabel);
            if (correctOption != null) {
                correctSelectedCount++;
                // 获取选项分数，如果没有则平均分配
                BigDecimal optionScore = correctOption.get("score") != null ?
                        new BigDecimal(correctOption.get("score").toString()) :
                        questionScore.divide(BigDecimal.valueOf(correctOptions.size()), 2, java.math.RoundingMode.HALF_UP);
                totalScore = totalScore.add(optionScore);
            }
        }

        // 判断答案状态
        int answerStatus;
        if (correctSelectedCount == correctOptions.size()) {
            // 全选正确
            answerStatus = AnswerStatusEnum.CORRECT.getCode();
            totalScore = questionScore;
        } else if (correctSelectedCount > 0) {
            // 部分正确
            answerStatus = AnswerStatusEnum.PARTIALLY_CORRECT.getCode();
        } else {
            // 没有选对任何正确选项
            answerStatus = AnswerStatusEnum.INCORRECT.getCode();
            totalScore = BigDecimal.ZERO;
        }

        return new AutoReviewResult(answerStatus, totalScore.floatValue());
    }

    /**
     * 批阅判断题
     */
    private AutoReviewResult reviewTrueFalse(Map<String, Object> questionData,
                                             QuestionStudent.AnswerPayload studentAnswer,
                                             BigDecimal questionScore) {
        // 获取正确答案选项标签（学生答案格式：{"options": ["A"]}，其中"A"是选项标签）
        List<Map<String, Object>> options = getOptionsFromQuestion(questionData);
        String correctOptionLabel = options.stream()
                .filter(opt -> opt.get(QuestionStudentConstants.OPTION_IS_CORRECT) != null &&
                        Integer.parseInt(opt.get(QuestionStudentConstants.OPTION_IS_CORRECT).toString()) == QuestionStudentConstants.ANSWER_CORRECT)
                .map(opt -> opt.get(QuestionStudentConstants.OPTION_LABEL) != null ? opt.get(QuestionStudentConstants.OPTION_LABEL).toString().trim() : null)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (correctOptionLabel == null) {
            return null;
        }

        // 学生答案（格式：{"options": ["A"]}，其中"A"是选项标签）
        String studentOptionLabel = null;
        if (studentAnswer.getOptions() != null && !studentAnswer.getOptions().isEmpty()) {
            studentOptionLabel = studentAnswer.getOptions().get(0).trim();
        } else if (studentAnswer.getContent() != null) {
            studentOptionLabel = studentAnswer.getContent().trim();
        }

        if (studentOptionLabel == null) {
            return new AutoReviewResult(AnswerStatusEnum.INCORRECT.getCode(), 0f);
        }

        // 比较选项标签，忽略大小写
        boolean isCorrect = correctOptionLabel.equalsIgnoreCase(studentOptionLabel);
        Float score = isCorrect ? questionScore.floatValue() : 0f;
        return new AutoReviewResult(getAnswerStatusByScore(score, questionScore.floatValue()), score);
    }

    /**
     * 批阅填空题
     */
    private AutoReviewResult reviewFillBlank(Map<String, Object> questionData,
                                             QuestionStudent.AnswerPayload studentAnswer,
                                             BigDecimal questionScore) {
        // 获取标准答案列表
        List<Map<String, Object>> answers = getAnswersFromQuestion(questionData);
        if (answers.isEmpty()) {
            return null;
        }

        // 学生答案
        List<String> studentBlanks = studentAnswer.getBlanks() != null ?
                studentAnswer.getBlanks() : new ArrayList<>();

        if (studentBlanks.size() != answers.size()) {
            // 答案数量不匹配，返回0分
            return new AutoReviewResult(AnswerStatusEnum.INCORRECT.getCode(), 0f);
        }

        // 逐空对比
        BigDecimal totalScore = BigDecimal.ZERO;

        for (int i = 0; i < answers.size(); i++) {
            Map<String, Object> answer = answers.get(i);
            String correctAnswer = answer.get(QuestionStudentConstants.ANSWER_CONTENT) != null ?
                    answer.get(QuestionStudentConstants.ANSWER_CONTENT).toString().trim() : null;
            BigDecimal blankScore = answer.get(QuestionStudentConstants.FIELD_SCORE) != null ?
                    new BigDecimal(answer.get(QuestionStudentConstants.FIELD_SCORE).toString()) :
                    questionScore.divide(BigDecimal.valueOf(answers.size()), 2, java.math.RoundingMode.HALF_UP);

            String studentBlank = i < studentBlanks.size() ?
                    studentBlanks.get(i).trim() : "";

            if (correctAnswer != null && correctAnswer.equalsIgnoreCase(studentBlank)) {
                totalScore = totalScore.add(blankScore);
            }
        }

        // 根据得分判断答案状态
        int answerStatus;
        if (totalScore.compareTo(questionScore) == 0) {
            answerStatus = AnswerStatusEnum.CORRECT.getCode();
        } else if (totalScore.compareTo(BigDecimal.ZERO) == 0) {
            answerStatus = AnswerStatusEnum.INCORRECT.getCode();
        } else {
            answerStatus = AnswerStatusEnum.PARTIALLY_CORRECT.getCode();
        }
        return new AutoReviewResult(answerStatus, totalScore.floatValue());
    }

    /**
     * 根据得分判断答案状态
     * @param score 实际得分
     * @param fullScore 满分
     * @return 答案状态码
     */
    private int getAnswerStatusByScore(Float score, Float fullScore) {
        if (score == null || fullScore == null || fullScore == 0) {
            return AnswerStatusEnum.INCORRECT.getCode();
        }

        if (score.equals(fullScore)) {
            return AnswerStatusEnum.CORRECT.getCode();
        } else if (score == 0f) {
            return AnswerStatusEnum.INCORRECT.getCode();
        } else {
            return AnswerStatusEnum.PARTIALLY_CORRECT.getCode();
        }
    }

    /**
     * 从题目数据中获取选项列表
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getOptionsFromQuestion(Map<String, Object> questionData) {
        Object optionsObj = questionData.get("options");
        if (optionsObj instanceof List) {
            return (List<Map<String, Object>>) optionsObj;
        }
        return new ArrayList<>();
    }

    /**
     * 从题目数据中获取答案列表
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getAnswersFromQuestion(Map<String, Object> questionData) {
        Object answersObj = questionData.get("answers");
        if (answersObj instanceof List) {
            return (List<Map<String, Object>>) answersObj;
        }
        return new ArrayList<>();
    }

    /**
     * 自动批阅结果
     */
    private static class AutoReviewResult {
        int isCorrect;
        Float score;

        AutoReviewResult(int isCorrect, Float score) {
            this.isCorrect = isCorrect;
            this.score = score;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PracticeStatisticsVO getPracticeStatistics(UUID practiceId) {
        if (practiceId == null) {
            throw new BusinessException(StudentPracticeEnum.PRACTICE_ID_REQUIRED);
        }

        // 查询指定练习ID的所有作答记录
        Query query = new Query();
        query.addCriteria(Criteria.where(QuestionStudentConstants.FIELD_PRACTICE_ID).is(practiceId));
        query.addCriteria(Criteria.where(QuestionStudentConstants.FIELD_IS_DELETED).is(QuestionStudentConstants.STATUS_NOT_DELETED));

        List<QuestionStudent> questionStudents = mongoTemplate.find(query, QuestionStudent.class);

        // 统计各种状态的数量
        long totalQuestions = questionStudents.size();
        long correctCount = questionStudents.stream().filter(qs -> qs.getIsCorrect() != null && qs.getIsCorrect() == QuestionStudentConstants.ANSWER_CORRECT).count();
        long incorrectCount = questionStudents.stream().filter(qs -> qs.getIsCorrect() != null && qs.getIsCorrect() == QuestionStudentConstants.ANSWER_INCORRECT).count();
        long partialCount = questionStudents.stream().filter(qs -> qs.getIsCorrect() != null && qs.getIsCorrect() == QuestionStudentConstants.ANSWER_PARTIALLY_CORRECT).count();
        long pendingReviewCount = questionStudents.stream().filter(qs -> qs.getIsCorrect() != null && qs.getIsCorrect() == QuestionStudentConstants.ANSWER_PENDING_REVIEW).count();

        // 计算题目平均分
        double averageScore = questionStudents.stream()
                .filter(qs -> qs.getScore() != null)
                .mapToDouble(QuestionStudent::getScore)
                .average()
                .orElse(0.0);

        PracticeStatisticsVO statistics = new PracticeStatisticsVO();
        statistics.setPracticeId(practiceId.toString());
        statistics.setTotalQuestions(totalQuestions);
        statistics.setCorrectCount(correctCount);
        statistics.setIncorrectCount(incorrectCount);
        statistics.setPartialCount(partialCount);
        statistics.setPendingReviewCount(pendingReviewCount);
        statistics.setAverageScore(averageScore);

        return statistics;
    }
}