package com.dayz.sapientiacloud_edupivot.course.repository;

import com.dayz.sapientiacloud_edupivot.course.entity.po.CourseChapter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseChapterRepository extends MongoRepository<CourseChapter, UUID> {

    List<CourseChapter> findByCourseIdOrderBySortOrderAsc(UUID courseId);

    List<CourseChapter> findByCourseIdAndParentChapterIdOrderBySortOrderAsc(UUID courseId, UUID parentChapterId);

    List<CourseChapter> findByCourseIdAndParentChapterIdIsNullOrderBySortOrderAsc(UUID courseId);

    List<CourseChapter> findByCourseIdAndStatusOrderBySortOrderAsc(UUID courseId, Integer status);

    @Query("{'course_id': ?0, 'chapter_name': {$regex: ?1, $options: 'i'}}")
    List<CourseChapter> findByCourseIdAndChapterNameContainingIgnoreCase(UUID courseId, String chapterName);


    boolean existsByCourseIdAndChapterName(UUID courseId, String chapterName);


    long countByCourseId(UUID courseId);

    long countByCourseIdAndStatus(UUID courseId, Integer status);

    long countByParentChapterId(UUID parentChapterId);

    void deleteByCourseId(UUID courseId);

    void deleteByCourseIdAndStatus(UUID courseId, Integer status);
}
