package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.QuestionOptionDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.QuestionOptionVO;
import com.dayz.sapientiacloud_edupivot.course.service.IQuestionOptionService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 题目选项控制器
 *
 * @author SapientiaCloud
 * @since 2024-01-01
 */
@Tag(name = "题目选项管理", description = "题目选项相关接口")
@RestController
@RequestMapping("/question-option")
@RequiredArgsConstructor
public class QuestionOptionController extends BaseController {

    private final IQuestionOptionService questionOptionService;

    @HasPermission(
            summary = "listQuestionOptionByQuestionId",
            description = "根据题目ID获取选项列表。",
            permission = PermissionConstants.QUESTION_OPTION_QUERY
    )
    @GetMapping("/question/{questionId}")
    public Result<List<QuestionOptionVO>> listQuestionOptionByQuestionId(
            @Parameter(name = "questionId", description = "题目ID", required = true) @PathVariable("questionId") UUID questionId
    ) {
        List<QuestionOptionVO> questionOptionVOList = questionOptionService.listQuestionOptionByQuestionId(questionId);
        return Result.success(questionOptionVOList);
    }

    @HasPermission(
            summary = "getQuestionOptionById",
            description = "通过选项的唯一ID获取其详细信息。",
            permission = PermissionConstants.QUESTION_OPTION_QUERY
    )
    @GetMapping("/{id}")
    public Result<QuestionOptionVO> getQuestionOptionById(
            @Parameter(name = "id", description = "选项ID", required = true) @PathVariable("id") UUID id
    ) {
        QuestionOptionVO questionOptionVO = questionOptionService.getQuestionOptionById(id);
        return Result.success(questionOptionVO);
    }

    @HasPermission(
            summary = "addQuestionOption",
            description = "创建新的选项。",
            permission = PermissionConstants.QUESTION_OPTION_ADD
    )
    @PostMapping("/add")
    public Result<QuestionOptionVO> addQuestionOption(@Valid @RequestBody QuestionOptionDTO questionOptionDTO) {
        QuestionOptionVO questionOptionVO = questionOptionService.addQuestionOption(questionOptionDTO);
        return Result.success(questionOptionVO);
    }

    @HasPermission(
            summary = "addQuestionOptions",
            description = "批量创建选项。",
            permission = PermissionConstants.QUESTION_OPTION_ADD
    )
    @PostMapping("/add-batch")
    public Result<List<QuestionOptionVO>> addQuestionOptions(@Valid @RequestBody List<QuestionOptionDTO> questionOptionDTOList) {
        List<QuestionOptionVO> questionOptionVOList = questionOptionService.addQuestionOptions(questionOptionDTOList);
        return Result.success(questionOptionVOList);
    }

    @HasPermission(
            summary = "updateQuestionOption",
            description = "修改现有选项的信息。",
            permission = PermissionConstants.QUESTION_OPTION_EDIT
    )
    @PutMapping
    public Result<Boolean> updateQuestionOption(@Valid @RequestBody QuestionOptionDTO questionOptionDTO) {
        return Result.success(questionOptionService.updateQuestionOption(questionOptionDTO));
    }

    @HasPermission(
            summary = "removeQuestionOptionById",
            description = "根据选项ID从系统中移除选项。",
            permission = PermissionConstants.QUESTION_OPTION_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeQuestionOptionById(
            @Parameter(name = "id", description = "选项ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(questionOptionService.removeQuestionOptionById(id));
    }

    @HasPermission(
            summary = "removeQuestionOptionsByQuestionId",
            description = "根据题目ID删除所有选项。",
            permission = PermissionConstants.QUESTION_OPTION_DELETE
    )
    @DeleteMapping("/question/{questionId}")
    public Result<Boolean> removeQuestionOptionsByQuestionId(
            @Parameter(name = "questionId", description = "题目ID", required = true) @PathVariable("questionId") UUID questionId
    ) {
        return Result.success(questionOptionService.removeQuestionOptionsByQuestionId(questionId));
    }

    @HasPermission(
            summary = "removeQuestionOptionsByIds",
            description = "根据选项ID列表批量删除选项。",
            permission = PermissionConstants.QUESTION_OPTION_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeQuestionOptionsByIds(
            @Parameter(name = "ids", description = "选项ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        return Result.success(questionOptionService.removeQuestionOptionsByIds(ids));
    }
}
