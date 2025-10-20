package com.dayz.sapientiacloud_edupivot.course.service;

import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseChapterAddDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseChapterDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseChapterQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseChapterVO;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.UUID;

public interface ICourseChapterService {

    PageInfo<CourseChapterVO> listCourseChapter(CourseChapterQueryDTO courseChapterQueryDTO);

    List<CourseChapterVO> listAllCourseChapterByCourseId(UUID courseId);

    List<CourseChapterVO> listCourseChapterTree(UUID courseId);

    CourseChapterVO getCourseChapterById(UUID id);

    CourseChapterVO addCourseChapter(CourseChapterAddDTO courseChapterAddDTO);

    Boolean updateCourseChapter(CourseChapterDTO courseChapterDTO);

    Boolean removeCourseChapterById(UUID id);

    Integer removeCourseChapterByIds(List<UUID> ids);

    Boolean updateChapterStatus(UUID id, Integer status);

    Boolean updateChapterSortOrder(UUID id, Integer sortOrder);

    Boolean batchUpdateChapterSortOrder(List<CourseChapterDTO> chapterSortList);

    Boolean likeChapter(UUID id);

    Boolean unlikeChapter(UUID id);

    Boolean viewChapter(UUID id);

    CourseChapterVO getChapterStatistics(UUID id);
}
