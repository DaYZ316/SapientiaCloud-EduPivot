package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
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

@Tag(name = "课程章节管理", description = "用于管理课程章节信息的API")
@RestController
@RequestMapping("/course-chapter")
@RequiredArgsConstructor
public class CourseChapterController extends BaseController {

    private final ICourseChapterService courseChapterService;


    @HasPermission(
            summary = "listCourseChapter",
            description = "根据父章节ID获取子章节列表。",
            permission = PermissionConstants.CHAPTER_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listCourseChapter(@ParameterObject CourseChapterQueryDTO courseChapterQueryDTO) {
        PageInfo<CourseChapterVO> pageInfo = courseChapterService.listCourseChapter(courseChapterQueryDTO);
        return TableDataResult.build(pageInfo.getList(), pageInfo.getTotal());
    }

    @HasPermission(
            summary = "listAllCourseChapterTree",
            description = "查询课程中的所有章节，并以树状结构返回",
            permission = PermissionConstants.CHAPTER_QUERY
    )
    @PostMapping("/tree/course/{courseId}")
    public Result<List<CourseChapterVO>> listAllCourseChapterTree(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId) {
        List<CourseChapterVO> treeList = courseChapterService.listAllCourseChapterTree(courseId);
        return Result.success(treeList);
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
            description = "添加新的课程章节。",
            permission = PermissionConstants.CHAPTER_ADD
    )
    @PostMapping("/add")
    public Result<CourseChapterVO> addCourseChapter(@Valid @RequestBody CourseChapterDTO courseChapterDTO) {
        CourseChapterVO courseChapterVO = courseChapterService.addCourseChapter(courseChapterDTO);
        return Result.success(courseChapterVO);
    }

    @HasPermission(
            summary = "updateCourseChapter",
            description = "修改现有课程章节的信息。",
            permission = PermissionConstants.CHAPTER_EDIT
    )
    @PutMapping
    public Result<Boolean> updateCourseChapter(@Valid @RequestBody CourseChapterDTO courseChapterDTO) {
        return Result.success(courseChapterService.updateCourseChapter(courseChapterDTO));
    }

    @HasPermission(
            summary = "removeCourseChapterById",
            description = "根据章节ID从系统中移除章节。",
            permission = PermissionConstants.CHAPTER_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeCourseChapterById(
            @Parameter(name = "id", description = "章节ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(courseChapterService.removeCourseChapterById(id));
    }

    @HasPermission(
            summary = "removeCourseChapterByIds",
            description = "根据章节ID列表批量删除章节。",
            permission = PermissionConstants.CHAPTER_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeCourseChapterByIds(
            @Parameter(name = "ids", description = "章节ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        return Result.success(courseChapterService.removeCourseChapterByIds(ids));
    }
}
