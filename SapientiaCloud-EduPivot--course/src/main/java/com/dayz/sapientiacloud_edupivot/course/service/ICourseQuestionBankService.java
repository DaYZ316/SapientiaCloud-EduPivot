package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseQuestionBankDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseQuestionBankQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseQuestionBankVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ICourseQuestionBankService {

    PageInfo<CourseQuestionBankVO> listCourseQuestionBank(CourseQuestionBankQueryDTO courseQuestionBankQueryDTO);

    List<CourseQuestionBankVO> listAllCourseQuestionBankByCourseId(UUID courseId);

    CourseQuestionBankVO getCourseQuestionBankById(UUID id);

    CourseQuestionBankVO addCourseQuestionBank(CourseQuestionBankDTO courseQuestionBankDTO);

    Boolean updateCourseQuestionBank(CourseQuestionBankDTO courseQuestionBankDTO);

    Boolean removeCourseQuestionBankById(UUID id);

    Integer removeCourseQuestionBankByIds(List<UUID> ids);
}
