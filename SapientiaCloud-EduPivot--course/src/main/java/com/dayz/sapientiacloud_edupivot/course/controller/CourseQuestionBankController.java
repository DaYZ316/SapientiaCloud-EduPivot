package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseQuestionBankDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseQuestionBankQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseQuestionBankVO;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseQuestionBankService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 课程题库控制器
 *
 * @author SapientiaCloud
 * @since 2024-01-01
 */
@Tag(name = "课程题库管理", description = "课程题库相关接口")
@RestController
@RequestMapping("/course-question-bank")
@RequiredArgsConstructor
public class CourseQuestionBankController extends BaseController {

    private final ICourseQuestionBankService courseQuestionBankService;

    @HasPermission(
            summary = "listCourseQuestionBank",
            description = "根据传入的条件分页查询题库信息。支持根据题库名称、题库类型、难度等级、是否公开等字段进行查询。",
            permission = PermissionConstants.QUESTION_BANK_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listCourseQuestionBank(@ParameterObject CourseQuestionBankQueryDTO courseQuestionBankQueryDTO) {
        startPage();
        PageInfo<CourseQuestionBankVO> pageInfo = courseQuestionBankService.listCourseQuestionBank(courseQuestionBankQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "listAllCourseQuestionBankByCourseId",
            description = "获取所有题库列表。",
            permission = PermissionConstants.QUESTION_BANK_QUERY
    )
    @GetMapping("/course/{courseId}")
    public Result<List<CourseQuestionBankVO>> listAllCourseQuestionBankByCourseId(@PathVariable("courseId") UUID courseId) {
        List<CourseQuestionBankVO> courseQuestionBankVOList = courseQuestionBankService.listAllCourseQuestionBankByCourseId(courseId);
        return Result.success(courseQuestionBankVOList);
    }

    @HasPermission(
            summary = "getCourseQuestionBankById",
            description = "通过题库的唯一ID获取其详细信息。",
            permission = PermissionConstants.QUESTION_BANK_QUERY
    )
    @GetMapping("/{id}")
    public Result<CourseQuestionBankVO> getCourseQuestionBankById(
            @Parameter(name = "id", description = "题库ID", required = true) @PathVariable("id") UUID id
    ) {
        CourseQuestionBankVO courseQuestionBankVO = courseQuestionBankService.getCourseQuestionBankById(id);
        return Result.success(courseQuestionBankVO);
    }

    @HasPermission(
            summary = "addCourseQuestionBank",
            description = "创建新的题库。",
            permission = PermissionConstants.QUESTION_BANK_ADD
    )
    @PostMapping("/add")
    public Result<CourseQuestionBankVO> addCourseQuestionBank(@Valid @RequestBody CourseQuestionBankDTO courseQuestionBankDTO) {
        CourseQuestionBankVO courseQuestionBankVO = courseQuestionBankService.addCourseQuestionBank(courseQuestionBankDTO);
        return Result.success(courseQuestionBankVO);
    }

    @HasPermission(
            summary = "updateCourseQuestionBank",
            description = "修改现有题库的信息。",
            permission = PermissionConstants.QUESTION_BANK_EDIT
    )
    @PutMapping
    public Result<Boolean> updateCourseQuestionBank(@Valid @RequestBody CourseQuestionBankDTO courseQuestionBankDTO) {
        return Result.success(courseQuestionBankService.updateCourseQuestionBank(courseQuestionBankDTO));
    }

    @HasPermission(
            summary = "removeCourseQuestionBankById",
            description = "根据题库ID从系统中移除题库。",
            permission = PermissionConstants.QUESTION_BANK_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeCourseQuestionBankById(
            @Parameter(name = "id", description = "题库ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(courseQuestionBankService.removeCourseQuestionBankById(id));
    }

    @HasPermission(
            summary = "removeCourseQuestionBankByIds",
            description = "根据题库ID列表批量删除题库。",
            permission = PermissionConstants.QUESTION_BANK_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeCourseQuestionBankByIds(
            @Parameter(name = "ids", description = "题库ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        return Result.success(courseQuestionBankService.removeCourseQuestionBankByIds(ids));
    }
}
