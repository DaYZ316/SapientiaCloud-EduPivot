package com.dayz.sapientiacloud_edupivot.celestial_hub.common.clients;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.config.FeignConfig;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.entity.vo.*;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(contextId = "CourseClient", value = "SapientiaCloud-EduPivot--course", path = "/course", configuration = FeignConfig.class)
public interface CourseClient {

    @GetMapping("/internal/{id}")
    Result<CourseVO> getCourseById(@PathVariable("id") UUID id);

    @GetMapping("/internal/all")
    Result<List<CourseVO>> listAllCourse();

    @GetMapping("/internal/{courseId}/teachers")
    Result<List<TeacherVO>> listTeachersByCourseId(@PathVariable("courseId") UUID courseId);

    @GetMapping("/internal/{courseId}/students")
    Result<List<CourseStudentVO>> listStudentsByCourseId(@PathVariable("courseId") UUID courseId);

    @GetMapping("/internal/task/{id}")
    Result<CourseTaskVO> getTaskById(@PathVariable("id") UUID id);

    @GetMapping("/internal/task/course/{courseId}")
    Result<List<CourseTaskVO>> listTasksByCourseId(@PathVariable("courseId") UUID courseId);

    @GetMapping("/internal/task/all")
    Result<List<CourseTaskVO>> listAllCourseTask();

    @GetMapping("/internal/chapter/{id}")
    Result<CourseChapterVO> getChapterById(@PathVariable("id") UUID id);

    @GetMapping("/internal/chapter/course/{courseId}")
    Result<List<CourseChapterVO>> listChaptersByCourseId(@PathVariable("courseId") UUID courseId);

    @GetMapping("/internal/chapter/all")
    Result<List<CourseChapterVO>> listAllCourseChapter();

    @GetMapping("/internal/forum/course/{courseId}")
    Result<List<CourseForumVO>> listForumsByCourseId(@PathVariable("courseId") UUID courseId);

    @GetMapping("/internal/forum/{id}")
    Result<CourseForumVO> getForumById(@PathVariable("id") UUID id);

    @GetMapping("/internal/forum/post/{id}")
    Result<ForumPostVO> getForumPostById(@PathVariable("id") UUID id);

    @GetMapping("/internal/forum/post/course/{courseId}")
    Result<List<ForumPostVO>> listForumPostsByCourseId(@PathVariable("courseId") UUID courseId);

    @GetMapping("/internal/forum/post/all")
    Result<List<ForumPostVO>> listAllForumPost();

    @GetMapping("/internal/forum/reply/{id}")
    Result<ForumReplyVO> getForumReplyById(@PathVariable("id") UUID id);

    @GetMapping("/internal/forum/reply/course/{courseId}")
    Result<List<ForumReplyVO>> listForumRepliesByCourseId(@PathVariable("courseId") UUID courseId);

    @GetMapping("/internal/question-bank/{id}")
    Result<CourseQuestionBankVO> getQuestionBankById(@PathVariable("id") UUID id);

    @GetMapping("/internal/question-bank/course/{courseId}")
    Result<List<CourseQuestionBankVO>> listQuestionBanksByCourseId(@PathVariable("courseId") UUID courseId);

    @GetMapping("/internal/question/{id}")
    Result<QuestionVO> getQuestionById(@PathVariable("id") UUID id);

    @GetMapping("/internal/question/all")
    Result<List<QuestionVO>> listAllQuestion();

    @GetMapping("/internal/question/bank/{bankId}")
    Result<List<QuestionVO>> listQuestionsByBankId(@PathVariable("bankId") UUID bankId);
}


