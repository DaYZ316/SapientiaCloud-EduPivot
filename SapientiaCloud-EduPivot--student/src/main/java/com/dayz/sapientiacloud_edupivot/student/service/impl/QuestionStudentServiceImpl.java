package com.dayz.sapientiacloud_edupivot.student.service.impl;

import com.dayz.sapientiacloud_edupivot.student.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.student.common.security.utils.UserContextUtil;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentAddDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentDTO;
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
import java.util.stream.Collectors;

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

    @Override
    @Transactional(readOnly = true)
    public List<QuestionStudent> list(UUID classroomId, UUID studentId) {
        if (classroomId != null && studentId != null) {
            return questionStudentRepository.findByClassroomIdAndStudentIdAndIsDeleted(classroomId, studentId, 0);
        }
        if (classroomId != null) {
            return questionStudentRepository.findByClassroomIdAndIsDeleted(classroomId, 0);
        }
        if (studentId != null) {
            return questionStudentRepository.findByStudentIdAndIsDeleted(studentId, 0);
        }
        return questionStudentRepository.findByIsDeleted(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionStudent> listAll() {
        return questionStudentRepository.findByIsDeleted(0);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionStudent getById(UUID id) {
        if (id == null) {
            throw new BusinessException(StudentPracticeEnum.SUBMISSION_NOT_FOUND);
        }
        return questionStudentRepository.findById(id)
                .filter(q -> q.getIsDeleted() != null && q.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(StudentPracticeEnum.SUBMISSION_NOT_FOUND));
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
        entity.setStatus(dto.getStatus());
        entity.setIsCorrect(dto.getIsCorrect());
        entity.setScore(dto.getScore());
        entity.setSubmitTime(dto.getSubmitTime());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        entity.setIsDeleted(0);
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
        existing.setStatus(dto.getStatus());
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
}