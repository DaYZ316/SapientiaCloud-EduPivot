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
import org.springframework.beans.BeanUtils;
import lombok.RequiredArgsConstructor;
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
    public PageInfo<ClassroomQuestionVO> listPage(ClassroomQuestionQueryDTO dto) {
        return PageHelper
                .startPage(dto == null ? 1 : dto.getPageNum(), dto == null ? 10 : dto.getPageSize())
                .doSelectPageInfo(() -> classroomQuestionMapper.listClassroomQuestionVO(dto));
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
    public ClassroomQuestionVO add(ClassroomQuestionDTO dto) {
        if (dto == null) {
            throw new BusinessException(ClassroomQuestionEnum.RECORD_NOT_EXISTS);
        }
        if (dto.getClassroomId() == null) {
            throw new BusinessException(ClassroomQuestionEnum.CLASSROOM_ID_REQUIRED);
        }
        if (dto.getQuestionId() == null) {
            throw new BusinessException(ClassroomQuestionEnum.QUESTION_ID_REQUIRED);
        }
        if (dto.getScore() != null && (dto.getScore() < 0 || dto.getScore() > 100)) {
            throw new BusinessException(ClassroomQuestionEnum.SCORE_INVALID);
        }
        if (dto.getIsRequired() != null && dto.getIsRequired() != 0 && dto.getIsRequired() != 1) {
            throw new BusinessException(ClassroomQuestionEnum.REQUIRED_FLAG_INVALID);
        }
        if (dto.getStartTime() != null && dto.getEndTime() != null && dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException(ClassroomQuestionEnum.END_TIME_BEFORE_START);
        }
        LambdaQueryWrapper<ClassroomQuestion> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(ClassroomQuestion::getClassroomId, dto.getClassroomId())
                .eq(ClassroomQuestion::getQuestionId, dto.getQuestionId())
                .eq(ClassroomQuestion::getDeleted, DeletedEnum.NOT_DELETED.getCode());
        if (this.count(dupWrapper) > 0) {
            throw new BusinessException(ClassroomQuestionEnum.DUPLICATE_QUESTION_IN_CLASSROOM);
        }

        ClassroomQuestion entity = new ClassroomQuestion();
        BeanUtils.copyProperties(dto, entity);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = dto.getStartTime() != null ? dto.getStartTime() : now;
        LocalDateTime endTime = dto.getEndTime();
        if (endTime == null) {
            endTime = courseRecordService.getCourseEndTimeById(dto.getClassroomId());
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
    public Boolean update(ClassroomQuestionDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new BusinessException(ClassroomQuestionEnum.RECORD_NOT_EXISTS);
        }
        if (dto.getClassroomId() == null) {
            throw new BusinessException(ClassroomQuestionEnum.CLASSROOM_ID_REQUIRED);
        }
        if (dto.getQuestionId() == null) {
            throw new BusinessException(ClassroomQuestionEnum.QUESTION_ID_REQUIRED);
        }
        if (dto.getScore() != null && (dto.getScore() < 0 || dto.getScore() > 100)) {
            throw new BusinessException(ClassroomQuestionEnum.SCORE_INVALID);
        }
        if (dto.getIsRequired() != null && dto.getIsRequired() != 0 && dto.getIsRequired() != 1) {
            throw new BusinessException(ClassroomQuestionEnum.REQUIRED_FLAG_INVALID);
        }
        if (dto.getStartTime() != null && dto.getEndTime() != null && dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new BusinessException(ClassroomQuestionEnum.END_TIME_BEFORE_START);
        }

        ClassroomQuestion existing = this.getById(dto.getId());
        if (existing == null) {
            throw new BusinessException(ClassroomQuestionEnum.RECORD_NOT_EXISTS);
        }

        ClassroomQuestion entity = new ClassroomQuestion();
        BeanUtils.copyProperties(dto, entity);
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