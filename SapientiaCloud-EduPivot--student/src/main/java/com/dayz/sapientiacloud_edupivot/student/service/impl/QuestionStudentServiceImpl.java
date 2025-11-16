package com.dayz.sapientiacloud_edupivot.student.service.impl;

import com.dayz.sapientiacloud_edupivot.student.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.student.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentSubmitDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.po.QuestionStudent;
import com.dayz.sapientiacloud_edupivot.student.entity.vo.StudentVO;
import com.dayz.sapientiacloud_edupivot.student.enums.PracticeSubmissionStatusEnum;
import com.dayz.sapientiacloud_edupivot.student.enums.StudentPracticeEnum;
import com.dayz.sapientiacloud_edupivot.student.repository.QuestionStudentRepository;
import com.dayz.sapientiacloud_edupivot.student.service.IQuestionStudentService;
import com.dayz.sapientiacloud_edupivot.student.service.IStudentService;
import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionStudentServiceImpl implements IQuestionStudentService {

    private final QuestionStudentRepository questionStudentRepository;
    private final IStudentService studentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "QuestionStudent", allEntries = true)
    public QuestionStudent submit(UUID classroomId, QuestionStudentSubmitDTO dto) {
        if (classroomId == null) {
            throw new BusinessException(StudentPracticeEnum.CLASSROOM_ID_REQUIRED);
        }
        if (dto == null || dto.getQuestionId() == null) {
            throw new BusinessException(StudentPracticeEnum.QUESTION_ID_REQUIRED);
        }
        if (dto.getAnswer() == null) {
            throw new BusinessException(StudentPracticeEnum.ANSWER_REQUIRED);
        }

        UUID currentUserId = UserContextUtil.getCurrentUserId();
        StudentVO studentVO = studentService.getStudentByUserId(currentUserId);
        if (studentVO == null || studentVO.getId() == null) {
            throw new BusinessException(StudentPracticeEnum.STUDENT_NOT_FOUND);
        }

        UUID studentId = studentVO.getId();

        QuestionStudent existing = questionStudentRepository.findByClassroomIdAndStudentIdAndQuestionIdAndIsDeleted(
                classroomId, studentId, dto.getQuestionId(), 0
        );

        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            QuestionStudent entity = new QuestionStudent();
            entity.setId(UuidCreator.getTimeOrderedEpoch());
            entity.setClassroomId(classroomId);
            entity.setStudentId(studentId);
            entity.setQuestionId(dto.getQuestionId());
            entity.setAnswer(dto.getAnswer());
            entity.setStatus(PracticeSubmissionStatusEnum.SUBMITTED.getCode());
            entity.setIsCorrect(null);
            entity.setScore(null);
            entity.setSubmitTime(now);
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            entity.setIsDeleted(0);
            return questionStudentRepository.save(entity);
        } else {
            existing.setAnswer(dto.getAnswer());
            existing.setStatus(PracticeSubmissionStatusEnum.SUBMITTED.getCode());
            existing.setSubmitTime(now);
            existing.setUpdateTime(now);
            return questionStudentRepository.save(existing);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "QuestionStudent", key = "#p0 + ':' + 'me'", condition = "#p0 != null")
    public List<QuestionStudent> listMyByClassroom(UUID classroomId) {
        if (classroomId == null) {
            throw new BusinessException(StudentPracticeEnum.CLASSROOM_ID_REQUIRED);
        }
        UUID currentUserId = UserContextUtil.getCurrentUserId();
        StudentVO studentVO = studentService.getStudentByUserId(currentUserId);
        if (studentVO == null || studentVO.getId() == null) {
            throw new BusinessException(StudentPracticeEnum.STUDENT_NOT_FOUND);
        }
        return questionStudentRepository.findByClassroomIdAndStudentIdAndIsDeleted(classroomId, studentVO.getId(), 0);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "QuestionStudent", key = "'summary:' + 'me'", condition = "true")
    public List<QuestionStudent> summaryMy() {
        UUID currentUserId = UserContextUtil.getCurrentUserId();
        StudentVO studentVO = studentService.getStudentByUserId(currentUserId);
        if (studentVO == null || studentVO.getId() == null) {
            throw new BusinessException(StudentPracticeEnum.STUDENT_NOT_FOUND);
        }
        return questionStudentRepository.findByStudentIdAndIsDeleted(studentVO.getId(), 0);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "QuestionStudent", key = "#p0", condition = "#p0 != null")
    public List<QuestionStudent> listByClassroom(UUID classroomId) {
        if (classroomId == null) {
            throw new BusinessException(StudentPracticeEnum.CLASSROOM_ID_REQUIRED);
        }
        return questionStudentRepository.findByClassroomIdAndIsDeleted(classroomId, 0);
    }
}