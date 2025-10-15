package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.QuestionAnswerDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.QuestionAnswerVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface IQuestionAnswerService {

    PageInfo<QuestionAnswerVO> listQuestionAnswer(Integer pageNum, Integer pageSize);

    List<QuestionAnswerVO> listAllQuestionAnswerByQuestionId(UUID questionId);

    List<QuestionAnswerVO> listAllQuestionAnswerBySysUserId(UUID sysUserId);

    QuestionAnswerVO getQuestionAnswerByQuestionIdAndSysUserId(UUID questionId, UUID sysUserId);

    QuestionAnswerVO getQuestionAnswerById(UUID id);

    QuestionAnswerVO addQuestionAnswer(QuestionAnswerDTO questionAnswerDTO);

    List<QuestionAnswerVO> addQuestionAnswers(List<QuestionAnswerDTO> questionAnswerDTOList);

    Boolean updateQuestionAnswer(QuestionAnswerDTO questionAnswerDTO);

    Boolean removeQuestionAnswerById(UUID id);

    Boolean removeQuestionAnswersByQuestionId(UUID questionId);

    Integer removeQuestionAnswersByIds(List<UUID> ids);
}
