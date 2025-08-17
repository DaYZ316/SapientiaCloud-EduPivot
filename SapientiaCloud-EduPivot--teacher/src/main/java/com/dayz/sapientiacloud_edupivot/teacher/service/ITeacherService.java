package com.dayz.sapientiacloud_edupivot.teacher.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dayz.sapientiacloud_edupivot.teacher.entity.dto.TeacherAddDTO;
import com.dayz.sapientiacloud_edupivot.teacher.entity.dto.TeacherDTO;
import com.dayz.sapientiacloud_edupivot.teacher.entity.dto.TeacherQueryDTO;
import com.dayz.sapientiacloud_edupivot.teacher.entity.po.Teacher;
import com.dayz.sapientiacloud_edupivot.teacher.entity.vo.TeacherVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ITeacherService extends IService<Teacher> {

    PageInfo<TeacherVO> listTeacherPage(TeacherQueryDTO teacherQueryDTO);

    List<TeacherVO> listAllTeacher();

    TeacherVO getTeacherById(UUID id);

    Teacher getTeacherByCode(String teacherCode);

    Teacher getTeacherBySysUserId(UUID sysUserId);

    Boolean addTeacher(TeacherAddDTO teacherAddDTO);

    Boolean updateTeacher(TeacherDTO teacherDTO);

    Boolean removeTeacherById(UUID id);

    Integer removeTeacherByIds(List<UUID> ids);

    Boolean checkTeacherCodeExists(String teacherCode, UUID excludeId);
}