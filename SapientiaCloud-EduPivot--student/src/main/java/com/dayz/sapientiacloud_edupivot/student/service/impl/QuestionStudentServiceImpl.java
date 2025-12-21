package com.dayz.sapientiacloud_edupivot.student.service.impl;

import com.dayz.sapientiacloud_edupivot.student.common.clients.QuestionClient;
import com.dayz.sapientiacloud_edupivot.student.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.student.common.result.Result;
import com.dayz.sapientiacloud_edupivot.student.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentAddDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.po.QuestionStudent;
import com.dayz.sapientiacloud_edupivot.student.entity.vo.QuestionStudentVO;
import com.dayz.sapientiacloud_edupivot.student.entity.vo.StudentVO;
import com.dayz.sapientiacloud_edupivot.student.enums.StudentPracticeEnum;
import com.dayz.sapientiacloud_edupivot.student.repository.QuestionStudentRepository;
import com.dayz.sapientiacloud_edupivot.student.service.IQuestionStudentService;
import com.dayz.sapientiacloud_edupivot.student.service.IStudentService;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "QuestionStudent", key = "'summary:' + 'me'", condition = "true")
    public List<QuestionStudentVO> summaryMy() {
        UUID currentUserId = UserContextUtil.getCurrentUserId();
        StudentVO studentVO = studentService.getStudentByUserId(currentUserId);
        if (studentVO == null || studentVO.getId() == null) {
            throw new BusinessException(StudentPracticeEnum.STUDENT_NOT_FOUND);
        }
        List<QuestionStudent> list = questionStudentRepository.findByStudentIdAndIsDeleted(studentVO.getId(), 0);
        return convertToVOList(list);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "QuestionStudent", key = "#p0", condition = "#p0 != null")
    public List<QuestionStudentVO> listByClassroom(UUID classroomId) {
        if (classroomId == null) {
            throw new BusinessException(StudentPracticeEnum.CLASSROOM_ID_REQUIRED);
        }
        List<QuestionStudent> list = questionStudentRepository.findByClassroomIdAndIsDeleted(classroomId, 0);
        return convertToVOList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionStudentVO> list(QuestionStudentQueryDTO queryDTO) {
        UUID classroomId = queryDTO != null ? queryDTO.getClassroomId() : null;
        UUID studentId = queryDTO != null ? queryDTO.getStudentId() : null;
        UUID questionId = queryDTO != null ? queryDTO.getQuestionId() : null;

        List<QuestionStudent> list;
        if (classroomId != null && studentId != null && questionId != null) {
            QuestionStudent record = questionStudentRepository.findByClassroomIdAndStudentIdAndQuestionIdAndIsDeleted(classroomId, studentId, questionId, 0);
            list = record == null ? List.of() : List.of(record);
        } else if (classroomId != null && studentId != null) {
            list = questionStudentRepository.findByClassroomIdAndStudentIdAndIsDeleted(classroomId, studentId, 0);
        } else if (classroomId != null) {
            list = questionStudentRepository.findByClassroomIdAndIsDeleted(classroomId, 0);
        } else if (studentId != null) {
            list = questionStudentRepository.findByStudentIdAndIsDeleted(studentId, 0);
        } else {
            list = questionStudentRepository.findByIsDeleted(0);
        }

        return convertToVOList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionStudentVO> listAll() {
        List<QuestionStudent> list = questionStudentRepository.findByIsDeleted(0);
        return convertToVOList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionStudentVO getById(UUID id) {
        if (id == null) {
            throw new BusinessException(StudentPracticeEnum.SUBMISSION_NOT_FOUND);
        }
        QuestionStudent questionStudent = questionStudentRepository.findById(id)
                .filter(q -> q.getIsDeleted() != null && q.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(StudentPracticeEnum.SUBMISSION_NOT_FOUND));
        return convertToVO(questionStudent);
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
        entity.setAnswer(dto.getAnswer());
        entity.setIsCorrect(dto.getIsCorrect());
        entity.setScore(dto.getScore());
        entity.setSubmitTime(dto.getSubmitTime());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setIsDeleted(0);

        // 尝试自动批阅：对于填空题、选择题、判断题，通过答案对比直接生成分数
        try {
            // 获取题目信息
            Result<Map<String, Object>> questionResult = questionClient.getQuestionById(dto.getQuestionId());
            if (questionResult != null && questionResult.isSuccess() && questionResult.getData() != null) {
                Map<String, Object> questionData = questionResult.getData();
                Integer questionType = questionData.get("questionType") != null ?
                        Integer.valueOf(questionData.get("questionType").toString()) : null;

                // 题目类型：0=单选题, 1=多选题, 2=判断题, 3=填空题, 4=简答题
                if (questionType != null && questionType <= 3) {
                    // 进行自动批阅
                    AutoReviewResult reviewResult = autoReview(questionData, dto.getAnswer(), questionType);
                    if (reviewResult != null) {
                        entity.setIsCorrect(reviewResult.isCorrect);
                        entity.setScore(reviewResult.score);
                        log.info("自动批阅成功: questionId={}, questionType={}, score={}, isCorrect={}",
                                dto.getQuestionId(), questionType, reviewResult.score, reviewResult.isCorrect);
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
                .filter(q -> q.getIsDeleted() != null && q.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(StudentPracticeEnum.SUBMISSION_NOT_FOUND));
        existing.setClassroomId(dto.getClassroomId());
        existing.setStudentId(dto.getStudentId());
        existing.setQuestionId(dto.getQuestionId());
        existing.setAnswer(dto.getAnswer());
        existing.setIsCorrect(dto.getIsCorrect());
        existing.setScore(dto.getScore());
        existing.setSubmitTime(dto.getSubmitTime());
        existing.setUpdateTime(LocalDateTime.now());
        questionStudentRepository.save(existing);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "QuestionStudent", allEntries = true)
    public Boolean removeById(UUID id) {
        QuestionStudent existing = questionStudentRepository.findById(id)
                .filter(q -> q.getIsDeleted() != null && q.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(StudentPracticeEnum.SUBMISSION_NOT_FOUND));
        existing.setIsDeleted(1);
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
                .filter(q -> q.getIsDeleted() != null && q.getIsDeleted() == 0)
                .collect(Collectors.toList());
        LocalDateTime now = LocalDateTime.now();
        list.forEach(q -> {
            q.setIsDeleted(1);
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
        List<QuestionStudent> list = questionStudentRepository.findByQuestionIdAndStudentIdAndIsDeleted(questionId, studentId, 0);
        return list != null && !list.isEmpty();
    }

    private List<QuestionStudentVO> convertToVOList(List<QuestionStudent> list) {
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private QuestionStudentVO convertToVO(QuestionStudent entity) {
        if (entity == null) {
            return null;
        }
        QuestionStudentVO vo = new QuestionStudentVO();
        BeanUtils.copyProperties(entity, vo);
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

            return switch (questionType) {
                case 0 -> reviewSingleChoice(questionData, studentAnswer, questionScore);
                case 1 -> reviewMultipleChoice(questionData, studentAnswer, questionScore);
                case 2 -> reviewTrueFalse(questionData, studentAnswer, questionScore);
                case 3 -> reviewFillBlank(questionData, studentAnswer, questionScore);
                default -> null;
            };
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
                .filter(opt -> opt.get("isCorrect") != null &&
                        Integer.parseInt(opt.get("isCorrect").toString()) == 1)
                .map(opt -> opt.get("optionLabel") != null ? opt.get("optionLabel").toString().trim() : null)
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
            return new AutoReviewResult(false, 0f);
        }

        boolean isCorrect = correctOptionLabel.equalsIgnoreCase(studentOptionLabel);
        return new AutoReviewResult(isCorrect, isCorrect ? questionScore.floatValue() : 0f);
    }

    /**
     * 批阅多选题
     */
    private AutoReviewResult reviewMultipleChoice(Map<String, Object> questionData,
                                                  QuestionStudent.AnswerPayload studentAnswer,
                                                  BigDecimal questionScore) {
        // 获取正确答案选项标签列表（学生答案格式：{"options": ["A", "C"]}，其中"A"、"C"是选项标签）
        List<Map<String, Object>> options = getOptionsFromQuestion(questionData);
        Set<String> correctOptionLabels = options.stream()
                .filter(opt -> opt.get("isCorrect") != null &&
                        Integer.parseInt(opt.get("isCorrect").toString()) == 1)
                .map(opt -> opt.get("optionLabel") != null ? opt.get("optionLabel").toString().trim() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (correctOptionLabels.isEmpty()) {
            return null;
        }

        // 学生答案（格式：{"options": ["A", "C"]}，其中"A"、"C"是选项标签）
        Set<String> studentOptionLabels = studentAnswer.getOptions() != null ?
                studentAnswer.getOptions().stream()
                        .map(String::trim)
                        .collect(Collectors.toSet()) : new HashSet<>();

        // 多选题：全部正确才得分（比较选项标签，忽略大小写）
        Set<String> correctOptionLabelsLower = correctOptionLabels.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        Set<String> studentOptionLabelsLower = studentOptionLabels.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        boolean isCorrect = correctOptionLabelsLower.equals(studentOptionLabelsLower);
        return new AutoReviewResult(isCorrect, isCorrect ? questionScore.floatValue() : 0f);
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
                .filter(opt -> opt.get("isCorrect") != null &&
                        Integer.parseInt(opt.get("isCorrect").toString()) == 1)
                .map(opt -> opt.get("optionLabel") != null ? opt.get("optionLabel").toString().trim() : null)
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
            return new AutoReviewResult(false, 0f);
        }

        // 比较选项标签，忽略大小写
        boolean isCorrect = correctOptionLabel.equalsIgnoreCase(studentOptionLabel);
        return new AutoReviewResult(isCorrect, isCorrect ? questionScore.floatValue() : 0f);
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
            return new AutoReviewResult(false, 0f);
        }

        // 逐空对比
        int correctCount = 0;
        BigDecimal totalScore = BigDecimal.ZERO;

        for (int i = 0; i < answers.size(); i++) {
            Map<String, Object> answer = answers.get(i);
            String correctAnswer = answer.get("answerContent") != null ?
                    answer.get("answerContent").toString().trim() : null;
            BigDecimal blankScore = answer.get("score") != null ?
                    new BigDecimal(answer.get("score").toString()) :
                    questionScore.divide(BigDecimal.valueOf(answers.size()), 2, java.math.RoundingMode.HALF_UP);

            String studentBlank = i < studentBlanks.size() ?
                    studentBlanks.get(i).trim() : "";

            if (correctAnswer != null && correctAnswer.equalsIgnoreCase(studentBlank)) {
                correctCount++;
                totalScore = totalScore.add(blankScore);
            }
        }

        boolean isCorrect = correctCount == answers.size();
        return new AutoReviewResult(isCorrect, totalScore.floatValue());
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
        boolean isCorrect;
        Float score;

        AutoReviewResult(boolean isCorrect, Float score) {
            this.isCorrect = isCorrect;
            this.score = score;
        }
    }
}