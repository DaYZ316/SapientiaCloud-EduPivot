package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.QuestionAnswerDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.QuestionAnswerVO;

import java.util.List;
import java.util.UUID;

public interface IQuestionAnswerService {

    List<QuestionAnswerVO> listQuestionAnswerByQuestionId(UUID questionId);

    QuestionAnswerVO getQuestionAnswerById(UUID id);

    QuestionAnswerVO addQuestionAnswer(QuestionAnswerDTO questionAnswerDTO);

    List<QuestionAnswerVO> addQuestionAnswers(List<QuestionAnswerDTO> questionAnswerDTOList);

    Boolean updateQuestionAnswer(QuestionAnswerDTO questionAnswerDTO);

    Boolean removeQuestionAnswerById(UUID id);

    Boolean removeQuestionAnswersByQuestionId(UUID questionId);

    Integer removeQuestionAnswersByIds(List<UUID> ids);
}

