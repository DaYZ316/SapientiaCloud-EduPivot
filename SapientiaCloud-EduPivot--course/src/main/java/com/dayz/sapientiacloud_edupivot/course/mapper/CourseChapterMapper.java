package com.dayz.sapientiacloud_edupivot.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseChapterQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseChapter;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseChapterVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface CourseChapterMapper extends BaseMapper<CourseChapter> {

    List<CourseChapterVO> listCourseChapter(CourseChapterQueryDTO courseChapterQueryDTO);

    List<CourseChapterVO> listAllCourseChapterTree(UUID courseId);

    CourseChapterVO getCourseChapterById(UUID chapterId);

    CourseChapterVO addCourseChapter(CourseChapter courseChapter);

    Boolean updateCourseChapter(CourseChapter courseChapter);

    Boolean removeCourseChapterById(UUID chapterId);

    Integer removeCourseChapterByIds(@Param("chapterIds") List<UUID> chapterIds);
}
