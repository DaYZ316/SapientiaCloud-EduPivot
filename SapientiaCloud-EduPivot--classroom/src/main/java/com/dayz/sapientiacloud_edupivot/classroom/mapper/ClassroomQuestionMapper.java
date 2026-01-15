package com.dayz.sapientiacloud_edupivot.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.classroom.entity.dto.ClassroomQuestionQueryDTO;
import com.dayz.sapientiacloud_edupivot.classroom.entity.po.ClassroomQuestion;
import com.dayz.sapientiacloud_edupivot.classroom.entity.vo.ClassroomQuestionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface ClassroomQuestionMapper extends BaseMapper<ClassroomQuestion> {

    int softDeleteById(@Param("id") UUID id);

    List<ClassroomQuestionVO> listClassroomQuestionVO(ClassroomQuestionQueryDTO dto);

    List<ClassroomQuestionVO> listVOByClassroomId(@Param("classroomId") UUID classroomId);

}