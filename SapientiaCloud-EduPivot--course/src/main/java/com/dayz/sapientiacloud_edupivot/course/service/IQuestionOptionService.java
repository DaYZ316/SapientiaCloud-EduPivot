package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.QuestionOptionDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.QuestionOptionVO;

import java.util.List;
import java.util.UUID;

public interface IQuestionOptionService {

    List<QuestionOptionVO> listQuestionOptionByQuestionId(UUID questionId);

    QuestionOptionVO getQuestionOptionById(UUID id);

    QuestionOptionVO addQuestionOption(QuestionOptionDTO questionOptionDTO);

    List<QuestionOptionVO> addQuestionOptions(List<QuestionOptionDTO> questionOptionDTOList);

    Boolean updateQuestionOption(QuestionOptionDTO questionOptionDTO);

    Boolean removeQuestionOptionById(UUID id);

    Boolean removeQuestionOptionsByQuestionId(UUID questionId);

    Integer removeQuestionOptionsByIds(List<UUID> ids);
}
