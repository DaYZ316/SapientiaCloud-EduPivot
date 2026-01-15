package com.dayz.sapientiacloud_edupivot.course.feigns;

import com.dayz.sapientiacloud_edupivot.course.common.entity.vo.TeacherVO;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.*;
import com.dayz.sapientiacloud_edupivot.course.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "课程内部查询接口", description = "提供给内部微服务通过Feign调用的只读查询端点")
@RestController
@RequestMapping("/course")
@RequiredArgsConstructor
public class CourseFeign {

    private final ICourseService courseService;
    private final ICourseTeacherService courseTeacherService;
    private final ICourseStudentService courseStudentService;
    private final ICourseTaskService courseTaskService;
    private final ICourseChapterService courseChapterService;
    private final ICourseForumService courseForumService;
    private final IForumPostService forumPostService;
    private final IForumReplyService forumReplyService;
    private final ICourseQuestionBankService courseQuestionBankService;
    private final IQuestionService questionService;

    @Operation(summary = "根据课程ID查询课程")
    @GetMapping("/internal/{id}")
    public Result<CourseVO> getCourseById(@PathVariable("id") UUID id) {
        return Result.success(courseService.getCourseById(id));
    }

    @Operation(summary = "查询所有课程")
    @GetMapping("/internal/all")
    public Result<List<CourseVO>> listAllCourse() {
        return Result.success(courseService.listAllCourse());
    }

    @Operation(summary = "根据课程ID查询教师列表")
    @GetMapping("/internal/{courseId}/teachers")
    public Result<List<TeacherVO>> listTeachersByCourseId(@PathVariable("courseId") UUID courseId) {
        return Result.success(courseTeacherService.listAllTeacherByCourseId(courseId));
    }

    @Operation(summary = "根据课程ID查询学生列表")
    @GetMapping("/internal/{courseId}/students")
    public Result<List<CourseStudentVO>> listStudentsByCourseId(@PathVariable("courseId") UUID courseId) {
        return Result.success(courseStudentService.listAllCourseStudentByCourseId(courseId));
    }

    @Operation(summary = "根据任务ID查询任务")
    @GetMapping("/internal/task/{id}")
    public Result<CourseTaskVO> getTaskById(@PathVariable("id") UUID id) {
        return Result.success(courseTaskService.getCourseTaskById(id));
    }

    @Operation(summary = "根据课程ID查询任务列表")
    @GetMapping("/internal/task/course/{courseId}")
    public Result<List<CourseTaskVO>> listTasksByCourseId(@PathVariable("courseId") UUID courseId) {
        return Result.success(courseTaskService.listAllCourseTaskByCourseId(courseId));
    }

    @Operation(summary = "查询所有任务列表")
    @GetMapping("/internal/task/all")
    public Result<List<CourseTaskVO>> listAllCourseTask() {
        return Result.success(courseTaskService.listAllCourseTask());
    }

    @Operation(summary = "根据章节ID查询章节")
    @GetMapping("/internal/chapter/{id}")
    public Result<CourseChapterVO> getChapterById(@PathVariable("id") UUID id) {
        return Result.success(courseChapterService.getCourseChapterById(id));
    }

    @Operation(summary = "根据课程ID查询章节列表")
    @GetMapping("/internal/chapter/course/{courseId}")
    public Result<List<CourseChapterVO>> listChaptersByCourseId(@PathVariable("courseId") UUID courseId) {
        return Result.success(courseChapterService.listAllCourseChapterByCourseId(courseId));
    }

    @Operation(summary = "查询所有章节列表")
    @GetMapping("/internal/chapter/all")
    public Result<List<CourseChapterVO>> listAllCourseChapter() {
        return Result.success(courseChapterService.listAllCourseChapter());
    }

    @Operation(summary = "根据课程ID查询论坛列表")
    @GetMapping("/internal/forum/course/{courseId}")
    public Result<List<CourseForumVO>> listForumsByCourseId(@PathVariable("courseId") UUID courseId) {
        return Result.success(courseForumService.listAllCourseForumByCourseId(courseId));
    }

    @Operation(summary = "根据论坛ID查询论坛")
    @GetMapping("/internal/forum/{id}")
    public Result<CourseForumVO> getForumById(@PathVariable("id") UUID id) {
        return Result.success(courseForumService.getCourseForumById(id));
    }

    @Operation(summary = "根据帖子ID查询帖子")
    @GetMapping("/internal/forum/post/{id}")
    public Result<ForumPostVO> getForumPostById(@PathVariable("id") UUID id) {
        return Result.success(forumPostService.getForumPostById(id));
    }

    @Operation(summary = "根据回复ID查询回复")
    @GetMapping("/internal/forum/reply/{id}")
    public Result<ForumReplyVO> getForumReplyById(@PathVariable("id") UUID id) {
        return Result.success(forumReplyService.getForumReplyById(id));
    }

    @Operation(summary = "根据课程ID查询帖子列表")
    @GetMapping("/internal/forum/post/course/{courseId}")
    public Result<List<ForumPostVO>> listForumPostsByCourseId(@PathVariable("courseId") UUID courseId) {
        return Result.success(forumPostService.listAllForumPostByCourseId(courseId));
    }

    @Operation(summary = "查询所有帖子列表")
    @GetMapping("/internal/forum/post/all")
    public Result<List<ForumPostVO>> listAllForumPost() {
        return Result.success(forumPostService.listAllForumPost());
    }

    @Operation(summary = "根据课程ID查询回复列表")
    @GetMapping("/internal/forum/reply/course/{courseId}")
    public Result<List<ForumReplyVO>> listForumRepliesByCourseId(@PathVariable("courseId") UUID courseId) {
        return Result.success(forumReplyService.listAllForumReplyByCourseId(courseId));
    }

    @Operation(summary = "根据题库ID查询题库")
    @GetMapping("/internal/question-bank/{id}")
    public Result<CourseQuestionBankVO> getQuestionBankById(@PathVariable("id") UUID id) {
        return Result.success(courseQuestionBankService.getCourseQuestionBankById(id));
    }

    @Operation(summary = "获取所有题目列表。")
    @GetMapping("/internal/question/all")
    public Result<List<QuestionVO>> listAllQuestion() {
        List<QuestionVO> questionVOList = questionService.listAllQuestion();
        return Result.success(questionVOList);
    }

    @Operation(summary = "根据课程ID查询题库列表")
    @GetMapping("/internal/question-bank/course/{courseId}")
    public Result<List<CourseQuestionBankVO>> listQuestionBanksByCourseId(@PathVariable("courseId") UUID courseId) {
        return Result.success(courseQuestionBankService.listAllCourseQuestionBankByCourseId(courseId));
    }

    @Operation(summary = "根据题目ID查询题目")
    @GetMapping("/internal/question/{id}")
    public Result<QuestionVO> getQuestionById(@PathVariable("id") UUID id) {
        return Result.success(questionService.getQuestionById(id));
    }

    @Operation(summary = "根据题库ID查询题目列表")
    @GetMapping("/internal/question/bank/{bankId}")
    public Result<List<QuestionVO>> listQuestionsByBankId(@PathVariable("bankId") UUID bankId) {
        return Result.success(questionService.listQuestionByQuestionBankId(bankId));
    }

    @Operation(summary = "获取课程总数")
    @GetMapping("/internal/count")
    public Result<Long> getCourseCount() {
        return Result.success(courseService.getCourseCount());
    }
}


