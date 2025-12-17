package com.dayz.sapientiacloud_edupivot.student.service;

import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentAddDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentQueryDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.vo.QuestionStudentVO;

import java.util.List;
import java.util.UUID;

public interface IQuestionStudentService {

    List<QuestionStudentVO> summaryMy();

    List<QuestionStudentVO> listByClassroom(UUID classroomId);

    List<QuestionStudentVO> list(QuestionStudentQueryDTO queryDTO);

    List<QuestionStudentVO> listAll();

    QuestionStudentVO getById(UUID id);

    Boolean add(QuestionStudentAddDTO dto);

    Boolean update(QuestionStudentDTO dto);

    Boolean removeById(UUID id);

    Integer removeByIds(List<UUID> ids);

    Boolean existsByQuestionIdAndStudentId(UUID questionId, UUID studentId);
}