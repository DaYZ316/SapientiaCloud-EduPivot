package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseChapterDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseChapterQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseChapterVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ICourseChapterService {

    PageInfo<CourseChapterVO> listCourseChapter(CourseChapterQueryDTO courseChapterQueryDTO);

    List<CourseChapterVO> listAllCourseChapterTree(UUID courseId);

    CourseChapterVO getCourseChapterById(UUID chapterId);

    CourseChapterVO addCourseChapter(CourseChapterDTO courseChapterDTO);

    Boolean updateCourseChapter(CourseChapterDTO courseChapterDTO);

    Boolean removeCourseChapterById(UUID chapterId);

    Integer removeCourseChapterByIds(List<UUID> chapterIds);
}
