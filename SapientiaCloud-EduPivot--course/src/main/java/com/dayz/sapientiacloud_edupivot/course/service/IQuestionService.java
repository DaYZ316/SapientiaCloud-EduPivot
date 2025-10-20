package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.QuestionAddDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.QuestionDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.QuestionQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.QuestionVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface IQuestionService {

    PageInfo<QuestionVO> listQuestion(QuestionQueryDTO questionQueryDTO);

    List<QuestionVO> listAllQuestion();

    QuestionVO getQuestionById(UUID id);

    List<QuestionVO> listQuestionByQuestionBankId(UUID questionBankId);

    QuestionVO addQuestion(QuestionAddDTO questionAddDTO);

    Boolean updateQuestion(QuestionDTO questionDTO);

    Boolean removeQuestionById(UUID id);

    Integer removeQuestionByIds(List<UUID> ids);

    Boolean publishQuestion(UUID id);

    Boolean unpublishQuestion(UUID id);

    Boolean viewQuestion(UUID id);
}
