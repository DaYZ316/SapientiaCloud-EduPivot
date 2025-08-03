package com.dayz.sapientiacloud_edupivot.teacher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.teacher.entity.dto.TeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.teacher.entity.po.Teacher;
import com.dayz.sapientiacloud_edupivot.teacher.entity.vo.TeacherVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.UUID;

@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {

    List<TeacherVO> listTeacher(TeacherQueryDTO teacherQueryDTO);

    Teacher selectByTeacherCode(String teacherCode);

    Teacher selectBySysUserId(UUID sysUserId);
}