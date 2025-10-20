package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.QuestionAnswerDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.QuestionAnswerVO;
import com.dayz.sapientiacloud_edupivot.course.service.IQuestionAnswerService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 题目答案控制器
 *
 * @author SapientiaCloud
 * @since 2024-01-01
 */
@Tag(name = "题目答案管理", description = "题目答案相关接口")
@RestController
@RequestMapping("/question-answer")
@RequiredArgsConstructor
public class QuestionAnswerController extends BaseController {

    private final IQuestionAnswerService questionAnswerService;

    @HasPermission(
            summary = "listQuestionAnswer",
            description = "分页查询答案列表。",
            permission = PermissionConstants.QUESTION_ANSWER_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listQuestionAnswer(
            @Parameter(name = "pageNum", description = "页码", required = true) @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(name = "pageSize", description = "页大小", required = true) @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        startPage();
        PageInfo<QuestionAnswerVO> pageInfo = questionAnswerService.listQuestionAnswer(pageNum, pageSize);
        return getDataTable(pageInfo.getList());
    }

    @HasPermission(
            summary = "listAllQuestionAnswerByQuestionId",
            description = "根据题目ID获取答案列表。",
            permission = PermissionConstants.QUESTION_ANSWER_QUERY
    )
    @GetMapping("/question/{questionId}")
    public Result<List<QuestionAnswerVO>> listAllQuestionAnswerByQuestionId(
            @Parameter(name = "questionId", description = "题目ID", required = true) @PathVariable("questionId") UUID questionId
    ) {
        List<QuestionAnswerVO> questionAnswerVOList = questionAnswerService.listAllQuestionAnswerByQuestionId(questionId);
        return Result.success(questionAnswerVOList);
    }

    @HasPermission(
            summary = "listAllQuestionAnswerBySysUserId",
            description = "根据用户ID获取答案列表。",
            permission = PermissionConstants.QUESTION_ANSWER_QUERY
    )
    @GetMapping("/user/{sysUserId}")
    public Result<List<QuestionAnswerVO>> listAllQuestionAnswerBySysUserId(
            @Parameter(name = "sysUserId", description = "用户ID", required = true) @PathVariable("sysUserId") UUID sysUserId
    ) {
        List<QuestionAnswerVO> questionAnswerVOList = questionAnswerService.listAllQuestionAnswerBySysUserId(sysUserId);
        return Result.success(questionAnswerVOList);
    }

    @HasPermission(
            summary = "getQuestionAnswerByQuestionIdAndSysUserId",
            description = "根据题目ID和用户ID获取答案。",
            permission = PermissionConstants.QUESTION_ANSWER_QUERY
    )
    @GetMapping("/question/{questionId}/user/{sysUserId}")
    public Result<QuestionAnswerVO> getQuestionAnswerByQuestionIdAndSysUserId(
            @Parameter(name = "questionId", description = "题目ID", required = true) @PathVariable("questionId") UUID questionId,
            @Parameter(name = "sysUserId", description = "用户ID", required = true) @PathVariable("sysUserId") UUID sysUserId
    ) {
        QuestionAnswerVO questionAnswerVO = questionAnswerService.getQuestionAnswerByQuestionIdAndSysUserId(questionId, sysUserId);
        return Result.success(questionAnswerVO);
    }

    @HasPermission(
            summary = "getQuestionAnswerById",
            description = "通过答案的唯一ID获取其详细信息。",
            permission = PermissionConstants.QUESTION_ANSWER_QUERY
    )
    @GetMapping("/{id}")
    public Result<QuestionAnswerVO> getQuestionAnswerById(
            @Parameter(name = "id", description = "答案ID", required = true) @PathVariable("id") UUID id
    ) {
        QuestionAnswerVO questionAnswerVO = questionAnswerService.getQuestionAnswerById(id);
        return Result.success(questionAnswerVO);
    }

    @HasPermission(
            summary = "addQuestionAnswer",
            description = "创建新的答案。",
            permission = PermissionConstants.QUESTION_ANSWER_ADD
    )
    @PostMapping("/add")
    public Result<QuestionAnswerVO> addQuestionAnswer(@Valid @RequestBody QuestionAnswerDTO questionAnswerDTO) {
        QuestionAnswerVO questionAnswerVO = questionAnswerService.addQuestionAnswer(questionAnswerDTO);
        return Result.success(questionAnswerVO);
    }

    @HasPermission(
            summary = "addQuestionAnswers",
            description = "批量创建答案。",
            permission = PermissionConstants.QUESTION_ANSWER_ADD
    )
    @PostMapping("/add-batch")
    public Result<List<QuestionAnswerVO>> addQuestionAnswers(@Valid @RequestBody List<QuestionAnswerDTO> questionAnswerDTOList) {
        List<QuestionAnswerVO> questionAnswerVOList = questionAnswerService.addQuestionAnswers(questionAnswerDTOList);
        return Result.success(questionAnswerVOList);
    }

    @HasPermission(
            summary = "updateQuestionAnswer",
            description = "修改现有答案的信息。",
            permission = PermissionConstants.QUESTION_ANSWER_EDIT
    )
    @PutMapping
    public Result<Boolean> updateQuestionAnswer(@Valid @RequestBody QuestionAnswerDTO questionAnswerDTO) {
        return Result.success(questionAnswerService.updateQuestionAnswer(questionAnswerDTO));
    }

    @HasPermission(
            summary = "removeQuestionAnswerById",
            description = "根据答案ID从系统中移除答案。",
            permission = PermissionConstants.QUESTION_ANSWER_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeQuestionAnswerById(
            @Parameter(name = "id", description = "答案ID", required = true) @PathVariable("id") UUID id
    ) {
        return Result.success(questionAnswerService.removeQuestionAnswerById(id));
    }

    @HasPermission(
            summary = "removeQuestionAnswersByQuestionId",
            description = "根据题目ID删除所有答案。",
            permission = PermissionConstants.QUESTION_ANSWER_DELETE
    )
    @DeleteMapping("/question/{questionId}")
    public Result<Boolean> removeQuestionAnswersByQuestionId(
            @Parameter(name = "questionId", description = "题目ID", required = true) @PathVariable("questionId") UUID questionId
    ) {
        return Result.success(questionAnswerService.removeQuestionAnswersByQuestionId(questionId));
    }

    @HasPermission(
            summary = "removeQuestionAnswersByIds",
            description = "根据答案ID列表批量删除答案。",
            permission = PermissionConstants.QUESTION_ANSWER_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeQuestionAnswersByIds(
            @Parameter(name = "ids", description = "答案ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        return Result.success(questionAnswerService.removeQuestionAnswersByIds(ids));
    }
}
