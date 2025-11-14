package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.QuestionDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.QuestionQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.QuestionVO;
import com.dayz.sapientiacloud_edupivot.course.service.IQuestionService;
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
 * 题目控制器
 *
 * @author SapientiaCloud
 * @since 2024-01-01
 */
@Tag(name = "题目管理", description = "题目相关接口")
@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionController extends BaseController {

    private final IQuestionService questionService;

    @HasPermission(
            summary = "listQuestion",
            description = "根据传入的条件分页查询题目信息。支持根据题目标题、题目类型、难度等级等字段进行查询。",
            permission = PermissionConstants.QUESTION_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listQuestion(@ParameterObject QuestionQueryDTO questionQueryDTO) {
        startPage();
        PageInfo<QuestionVO> pageInfo = questionService.listQuestion(questionQueryDTO);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "listAllQuestion",
            description = "获取所有题目列表。",
            permission = PermissionConstants.QUESTION_QUERY
    )
    @GetMapping("/all")
    public Result<List<QuestionVO>> listAllQuestion() {
        List<QuestionVO> questionVOList = questionService.listAllQuestion();
        return Result.success(questionVOList);
    }

    @HasPermission(
            summary = "getQuestionById",
            description = "通过题目的唯一ID获取其详细信息。",
            permission = PermissionConstants.QUESTION_QUERY
    )
    @GetMapping("/{id}")
    public Result<QuestionVO> getQuestionById(
            @Parameter(name = "id", description = "题目ID", required = true) @PathVariable("id") UUID id
    ) {
        QuestionVO questionVO = questionService.getQuestionById(id);
        return Result.success(questionVO);
    }

    @HasPermission(
            summary = "listQuestionByQuestionBankId",
            description = "根据题库ID获取题目列表。",
            permission = PermissionConstants.QUESTION_QUERY
    )
    @GetMapping("/question-bank/{questionBankId}")
    public Result<List<QuestionVO>> listQuestionByQuestionBankId(
            @Parameter(name = "questionBankId", description = "题库ID", required = true) @PathVariable("questionBankId") UUID questionBankId
    ) {
        List<QuestionVO> questionVOList = questionService.listQuestionByQuestionBankId(questionBankId);
        return Result.success(questionVOList);
    }

    @HasPermission(
            summary = "addQuestion",
            description = "创建新的题目。",
            permission = PermissionConstants.QUESTION_ADD
    )
    @PostMapping("/add")
    public Result<QuestionVO> addQuestion(@Valid @RequestBody QuestionDTO questionDTO) {
        QuestionVO questionVO = questionService.addQuestion(questionDTO);
        return Result.success(questionVO);
    }

    @HasPermission(
            summary = "updateQuestion",
            description = "修改现有题目的信息。",
            permission = PermissionConstants.QUESTION_EDIT
    )
    @PutMapping
    public Result<Boolean> updateQuestion(@Valid @RequestBody QuestionDTO questionDTO) {
        return Result.success(questionService.updateQuestion(questionDTO));
    }

    @HasPermission(
            summary = "removeQuestionById",
            description = "根据题目ID从系统中移除题目。",
            permission = PermissionConstants.QUESTION_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeQuestionById(
            @Parameter(name = "id", description = "题目ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(questionService.removeQuestionById(id));
    }

    @HasPermission(
            summary = "removeQuestionByIds",
            description = "根据题目ID列表批量删除题目。",
            permission = PermissionConstants.QUESTION_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeQuestionByIds(
            @Parameter(name = "ids", description = "题目ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        return Result.success(questionService.removeQuestionByIds(ids));
    }

    @HasPermission(
            summary = "publishQuestion",
            description = "发布题目。",
            permission = PermissionConstants.QUESTION_EDIT
    )
    @PutMapping("/{id}/publish")
    public Result<Boolean> publishQuestion(
            @Parameter(name = "id", description = "题目ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(questionService.publishQuestion(id));
    }

    @HasPermission(
            summary = "unpublishQuestion",
            description = "取消发布题目。",
            permission = PermissionConstants.QUESTION_EDIT
    )
    @PutMapping("/{id}/unpublish")
    public Result<Boolean> unpublishQuestion(
            @Parameter(name = "id", description = "题目ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(questionService.unpublishQuestion(id));
    }

    @HasPermission(
            summary = "viewQuestion",
            description = "增加题目浏览次数。",
            permission = PermissionConstants.QUESTION_VIEW
    )
    @PutMapping("/{id}/view")
    public Result<Boolean> viewQuestion(
            @Parameter(name = "id", description = "题目ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(questionService.viewQuestion(id));
    }
}
