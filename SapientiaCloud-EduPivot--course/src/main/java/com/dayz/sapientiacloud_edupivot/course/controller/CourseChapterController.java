package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseChapterAddDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseChapterDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseChapterQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseChapterVO;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseChapterService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "课程章节管理", description = "用于管理课程章节的API")
@RestController
@RequestMapping("/chapter")
@RequiredArgsConstructor
public class CourseChapterController extends BaseController {

    private final ICourseChapterService courseChapterService;

    @HasPermission(
            summary = "listCourseChapter",
            description = "根据传入的条件分页查询课程章节信息。支持根据章节名称、课程ID等字段进行查询。",
            permission = PermissionConstants.CHAPTER_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listCourseChapter(@ParameterObject CourseChapterQueryDTO courseChapterQueryDTO) {
        startPage();
        PageInfo<CourseChapterVO> pageInfo = courseChapterService.listCourseChapter(courseChapterQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "listCourseChapterByCourseId",
            description = "根据课程ID获取该课程下的所有章节列表。",
            permission = PermissionConstants.CHAPTER_QUERY
    )
    @GetMapping("/course/{courseId}")
    public Result<List<CourseChapterVO>> listCourseChapterByCourseId(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId
    ) {
        List<CourseChapterVO> chapterVOList = courseChapterService.listCourseChapterByCourseId(courseId);
        return Result.success(chapterVOList);
    }

    @HasPermission(
            summary = "listCourseChapterTree",
            description = "获取课程章节的树形结构。",
            permission = PermissionConstants.CHAPTER_QUERY
    )
    @GetMapping("/course/{courseId}/tree")
    public Result<List<CourseChapterVO>> listCourseChapterTree(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId
    ) {
        List<CourseChapterVO> chapterTree = courseChapterService.listCourseChapterTree(courseId);
        return Result.success(chapterTree);
    }

    @HasPermission(
            summary = "getCourseChapterById",
            description = "通过章节的唯一ID获取其详细信息。",
            permission = PermissionConstants.CHAPTER_QUERY
    )
    @GetMapping("/{id}")
    public Result<CourseChapterVO> getCourseChapterById(
            @Parameter(name = "id", description = "章节ID", required = true) @PathVariable("id") UUID id
    ) {
        CourseChapterVO courseChapterVO = courseChapterService.getCourseChapterById(id);
        return Result.success(courseChapterVO);
    }

    @HasPermission(
            summary = "addCourseChapter",
            description = "向课程中添加一个新的章节。",
            permission = PermissionConstants.CHAPTER_ADD
    )
    @PostMapping
    public Result<CourseChapterVO> addCourseChapter(
            @RequestBody @Valid CourseChapterAddDTO courseChapterAddDTO
    ) {
        CourseChapterVO courseChapterVO = courseChapterService.addCourseChapter(courseChapterAddDTO);
        return Result.success(courseChapterVO);
    }

    @HasPermission(
            summary = "updateCourseChapter",
            description = "更新现有章节的信息。",
            permission = PermissionConstants.CHAPTER_EDIT
    )
    @PutMapping
    public Result<Boolean> updateCourseChapter(
            @RequestBody @Valid CourseChapterDTO courseChapterDTO
    ) {
        Boolean result = courseChapterService.updateCourseChapter(courseChapterDTO);
        return Result.success(result);
    }

    @HasPermission(
            summary = "removeCourseChapterById",
            description = "通过章节的唯一ID删除章节。",
            permission = PermissionConstants.CHAPTER_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeCourseChapterById(
            @Parameter(name = "id", description = "章节ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = courseChapterService.removeCourseChapterById(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "removeCourseChapterByIds",
            description = "根据章节ID列表批量删除章节。",
            permission = PermissionConstants.CHAPTER_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeCourseChapterByIds(
            @RequestBody List<UUID> ids
    ) {
        Integer result = courseChapterService.removeCourseChapterByIds(ids);
        return Result.success(result);
    }

    @HasPermission(
            summary = "updateChapterStatus",
            description = "更新章节状态（草稿/发布）。",
            permission = PermissionConstants.CHAPTER_EDIT
    )
    @PutMapping("/{id}/status")
    public Result<Boolean> updateChapterStatus(
            @Parameter(name = "id", description = "章节ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "status", description = "章节状态 (0=草稿, 1=发布)", required = true) @RequestParam("status") Integer status
    ) {
        Boolean result = courseChapterService.updateChapterStatus(id, status);
        return Result.success(result);
    }

    @HasPermission(
            summary = "updateChapterSortOrder",
            description = "更新章节排序权重。",
            permission = PermissionConstants.CHAPTER_EDIT
    )
    @PutMapping("/{id}/sort")
    public Result<Boolean> updateChapterSortOrder(
            @Parameter(name = "id", description = "章节ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "sortOrder", description = "排序权重", required = true) @RequestParam("sortOrder") Integer sortOrder
    ) {
        Boolean result = courseChapterService.updateChapterSortOrder(id, sortOrder);
        return Result.success(result);
    }

    @HasPermission(
            summary = "batchUpdateChapterSortOrder",
            description = "批量更新章节排序权重。",
            permission = PermissionConstants.CHAPTER_EDIT
    )
    @PutMapping("/batch/sort")
    public Result<Boolean> batchUpdateChapterSortOrder(
            @RequestBody List<CourseChapterDTO> chapterSortList
    ) {
        Boolean result = courseChapterService.batchUpdateChapterSortOrder(chapterSortList);
        return Result.success(result);
    }

    @HasPermission(
            summary = "likeChapter",
            description = "点赞章节。",
            permission = PermissionConstants.CHAPTER_QUERY
    )
    @PostMapping("/{id}/like")
    public Result<Boolean> likeChapter(
            @Parameter(name = "id", description = "章节ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = courseChapterService.likeChapter(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "unlikeChapter",
            description = "取消点赞章节。",
            permission = PermissionConstants.CHAPTER_QUERY
    )
    @DeleteMapping("/{id}/like")
    public Result<Boolean> unlikeChapter(
            @Parameter(name = "id", description = "章节ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = courseChapterService.unlikeChapter(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "viewChapter",
            description = "浏览章节（增加浏览次数）。",
            permission = PermissionConstants.CHAPTER_QUERY
    )
    @PostMapping("/{id}/view")
    public Result<Boolean> viewChapter(
            @Parameter(name = "id", description = "章节ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = courseChapterService.viewChapter(id);
        return Result.success(result);
    }

    @HasPermission(
            summary = "getChapterStatistics",
            description = "获取章节统计信息（浏览次数、点赞次数、评论次数等）。",
            permission = PermissionConstants.CHAPTER_QUERY
    )
    @GetMapping("/{id}/statistics")
    public Result<CourseChapterVO> getChapterStatistics(
            @Parameter(name = "id", description = "章节ID", required = true) @PathVariable("id") UUID id
    ) {
        CourseChapterVO statistics = courseChapterService.getChapterStatistics(id);
        return Result.success(statistics);
    }
}
