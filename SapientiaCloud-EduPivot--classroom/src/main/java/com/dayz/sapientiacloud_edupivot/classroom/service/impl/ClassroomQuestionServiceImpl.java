package com.dayz.sapientiacloud_edupivot.classroom.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dayz.sapientiacloud_edupivot.classroom.common.enums.DeletedEnum;
import com.dayz.sapientiacloud_edupivot.classroom.common.exception.BusinessException;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.ClassroomQuestionDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.ClassroomQuestionQueryDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.po.ClassroomQuestion;
import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.ClassroomQuestionVO;
import com.dayz.sapientiacloud_edupivot.classroom.enums.ClassroomQuestionEnum;
import com.dayz.sapientiacloud_edupivot.classroom.mapper.ClassroomQuestionMapper;
import com.dayz.sapientiacloud_edupivot.classroom.service.IClassroomQuestionService;
import com.dayz.sapientiacloud_edupivot.classroom.service.ICourseRecordService;
import com.github.f4b6a3.uuid.UuidCreator;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassroomQuestionServiceImpl extends ServiceImpl<ClassroomQuestionMapper, ClassroomQuestion> implements IClassroomQuestionService {

    private final ClassroomQuestionMapper classroomQuestionMapper;
    private final ICourseRecordService courseRecordService;

    @Override
    @Transactional(readOnly = true)
    public PageInfo<ClassroomQuestionVO> listPage(ClassroomQuestionQueryDTO classroomQuestionQueryDTO) {
        return PageHelper
                .startPage(classroomQuestionQueryDTO == null ? 1 : classroomQuestionQueryDTO.getPageNum(), classroomQuestionQueryDTO == null ? 10 : classroomQuestionQueryDTO.getPageSize())
                .doSelectPageInfo(() -> classroomQuestionMapper.listClassroomQuestionVO(classroomQuestionQueryDTO));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ClassroomQuestion", key = "#p0", condition = "#p0 != null")
    public List<ClassroomQuestionVO> listByClassroomId(UUID classroomId) {
        if (classroomId == null) {
            throw new BusinessException(ClassroomQuestionEnum.CLASSROOM_ID_REQUIRED);
        }
        return classroomQuestionMapper.listVOByClassroomId(classroomId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "ClassroomQuestion", allEntries = true)
    public ClassroomQuestionVO add(ClassroomQuestionDTO classroomQuestionDTO) {
        if (classroomQuestionDTO == null) {
            throw new BusinessException(ClassroomQuestionEnum.RECORD_NOT_EXISTS);
        }
        if (classroomQuestionDTO.getClassroomId() == null) {
            throw new BusinessException(ClassroomQuestionEnum.CLASSROOM_ID_REQUIRED);
        }
        if (classroomQuestionDTO.getQuestionId() == null) {
            throw new BusinessException(ClassroomQuestionEnum.QUESTION_ID_REQUIRED);
        }
        if (classroomQuestionDTO.getScore() != null && (classroomQuestionDTO.getScore() < 0 || classroomQuestionDTO.getScore() > 100)) {
            throw new BusinessException(ClassroomQuestionEnum.SCORE_INVALID);
        }
        if (classroomQuestionDTO.getIsRequired() != null && classroomQuestionDTO.getIsRequired() != 0 && classroomQuestionDTO.getIsRequired() != 1) {
            throw new BusinessException(ClassroomQuestionEnum.REQUIRED_FLAG_INVALID);
        }
        if (classroomQuestionDTO.getStartTime() != null && classroomQuestionDTO.getEndTime() != null && classroomQuestionDTO.getEndTime().isBefore(classroomQuestionDTO.getStartTime())) {
            throw new BusinessException(ClassroomQuestionEnum.END_TIME_BEFORE_START);
        }
        LambdaQueryWrapper<ClassroomQuestion> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(ClassroomQuestion::getClassroomId, classroomQuestionDTO.getClassroomId())
                .eq(ClassroomQuestion::getQuestionId, classroomQuestionDTO.getQuestionId())
                .eq(ClassroomQuestion::getDeleted, DeletedEnum.NOT_DELETED.getCode());
        if (this.count(dupWrapper) > 0) {
            throw new BusinessException(ClassroomQuestionEnum.DUPLICATE_QUESTION_IN_CLASSROOM);
        }

        ClassroomQuestion entity = new ClassroomQuestion();
        BeanUtils.copyProperties(classroomQuestionDTO, entity);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = classroomQuestionDTO.getStartTime() != null ? classroomQuestionDTO.getStartTime() : now;
        LocalDateTime endTime = classroomQuestionDTO.getEndTime();
        if (endTime == null) {
            endTime = courseRecordService.getCourseEndTimeById(classroomQuestionDTO.getClassroomId());
        }
        entity.setStartTime(startTime);
        entity.setEndTime(endTime);
        entity.setId(UuidCreator.getTimeOrderedEpoch());
        entity.setDeleted(DeletedEnum.NOT_DELETED.getCode());
        entity.setCreateTime(now);
        entity.setUpdateTime(now);

        this.save(entity);
        return toVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = "ClassroomQuestion", key = "#p0.classroomId", condition = "#p0 != null"),
            @CacheEvict(value = "ClassroomQuestion", allEntries = true)
    })
    public Boolean update(ClassroomQuestionDTO classroomQuestionDTO) {
        if (classroomQuestionDTO == null || classroomQuestionDTO.getId() == null) {
            throw new BusinessException(ClassroomQuestionEnum.RECORD_NOT_EXISTS);
        }
        if (classroomQuestionDTO.getClassroomId() == null) {
            throw new BusinessException(ClassroomQuestionEnum.CLASSROOM_ID_REQUIRED);
        }
        if (classroomQuestionDTO.getQuestionId() == null) {
            throw new BusinessException(ClassroomQuestionEnum.QUESTION_ID_REQUIRED);
        }
        if (classroomQuestionDTO.getScore() != null && (classroomQuestionDTO.getScore() < 0 || classroomQuestionDTO.getScore() > 100)) {
            throw new BusinessException(ClassroomQuestionEnum.SCORE_INVALID);
        }
        if (classroomQuestionDTO.getIsRequired() != null && classroomQuestionDTO.getIsRequired() != 0 && classroomQuestionDTO.getIsRequired() != 1) {
            throw new BusinessException(ClassroomQuestionEnum.REQUIRED_FLAG_INVALID);
        }
        if (classroomQuestionDTO.getStartTime() != null && classroomQuestionDTO.getEndTime() != null && classroomQuestionDTO.getEndTime().isBefore(classroomQuestionDTO.getStartTime())) {
            throw new BusinessException(ClassroomQuestionEnum.END_TIME_BEFORE_START);
        }

        ClassroomQuestion existing = this.getById(classroomQuestionDTO.getId());
        if (existing == null) {
            throw new BusinessException(ClassroomQuestionEnum.RECORD_NOT_EXISTS);
        }

        ClassroomQuestion entity = new ClassroomQuestion();
        BeanUtils.copyProperties(classroomQuestionDTO, entity);
        entity.setUpdateTime(LocalDateTime.now());
        return this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "ClassroomQuestion", allEntries = true)
    public Boolean removeById(UUID id) {
        if (id == null) {
            throw new BusinessException(ClassroomQuestionEnum.RECORD_NOT_EXISTS);
        }
        ClassroomQuestion existing = this.getById(id);
        if (existing == null) {
            throw new BusinessException(ClassroomQuestionEnum.RECORD_NOT_EXISTS);
        }
        return classroomQuestionMapper.softDeleteById(id) > 0;
    }

    private ClassroomQuestionVO toVO(ClassroomQuestion entity) {
        if (entity == null) {
            return null;
        }
        ClassroomQuestionVO vo = new ClassroomQuestionVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}