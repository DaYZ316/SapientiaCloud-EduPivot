package com.dayz.sapientiacloud_edupivot.classroom.service;

import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.ClassroomQuestionDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.ClassroomQuestionQueryDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.ClassroomQuestionVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface IClassroomQuestionService {

    PageInfo<ClassroomQuestionVO> listPage(ClassroomQuestionQueryDTO dto);

    List<ClassroomQuestionVO> listByClassroomId(UUID classroomId);

    ClassroomQuestionVO add(ClassroomQuestionDTO dto);

    Boolean update(ClassroomQuestionDTO dto);

    Boolean removeById(UUID id);
}