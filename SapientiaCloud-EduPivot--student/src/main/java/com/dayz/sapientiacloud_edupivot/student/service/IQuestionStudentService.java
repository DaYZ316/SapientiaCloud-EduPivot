package com.dayz.sapientiacloud_edupivot.student.service;

import com.dayz.sapientiacloud_edupivot.student.entity.dto.QuestionStudentSubmitDTO;
import com.dayz.sapientiacloud_edupivot.student.entity.po.QuestionStudent;

import java.util.List;
import java.util.UUID;

public interface IQuestionStudentService {

    QuestionStudent submit(UUID classroomId, QuestionStudentSubmitDTO dto);

    List<QuestionStudent> listMyByClassroom(UUID classroomId);

    List<QuestionStudent> summaryMy();

    List<QuestionStudent> listByClassroom(UUID classroomId);
}