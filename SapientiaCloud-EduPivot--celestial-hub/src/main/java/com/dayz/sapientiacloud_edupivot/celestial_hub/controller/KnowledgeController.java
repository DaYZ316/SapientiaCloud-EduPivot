package com.dayz.sapientiacloud_edupivot.celestial_hub.controller;

import com.dayz.sapientiacloud_edupivot.celestial_hub.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.result.Result;
import com.dayz.sapientiacloud_edupivot.celestial_hub.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.celestial_hub.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.KnowledgeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.dto.VectorizeRequestDTO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.entity.vo.KnowledgeSearchVO;
import com.dayz.sapientiacloud_edupivot.celestial_hub.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "知识管理", description = "知识向量化和检索相关接口")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class KnowledgeController extends BaseController {

    private final KnowledgeService knowledgeService;

    @HasPermission(
            summary = "searchKnowledge",
            description = "基于向量相似度检索知识内容",
            permission = PermissionConstants.CELESTIAL_QUERY
    )
    @PostMapping("/search")
    public Result<KnowledgeSearchVO> searchKnowledge(@Valid @RequestBody KnowledgeRequestDTO query) {
        KnowledgeSearchVO result = knowledgeService.searchKnowledge(query);
        return Result.success(result);
    }

    @HasPermission(
            summary = "vectorizeContent",
            description = "将课程内容向量化并存储到向量库",
            permission = PermissionConstants.CELESTIAL_ADD
    )
    @PostMapping("/vectorize")
    public Result<Boolean> vectorizeContent(@Valid @RequestBody VectorizeRequestDTO request) {
        knowledgeService.vectorizeCourseContent(request);
        return Result.success(true);
    }

    @HasPermission(
            summary = "deleteCourseVectors",
            description = "删除指定课程的所有向量数据",
            permission = PermissionConstants.CELESTIAL_DELETE
    )
    @DeleteMapping("/course/{courseId}")
    public Result<Boolean> deleteCourseVectors(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable UUID courseId) {
        knowledgeService.deleteCourseVectors(courseId);
        return Result.success(true);
    }

    @HasPermission(
            summary = "deleteChapterVectors",
            description = "删除指定章节的所有向量数据",
            permission = PermissionConstants.CELESTIAL_DELETE
    )
    @DeleteMapping("/chapter/{chapterId}")
    public Result<Boolean> deleteChapterVectors(
            @Parameter(name = "chapterId", description = "章节ID", required = true) @PathVariable UUID chapterId) {
        knowledgeService.deleteChapterVectors(chapterId);
        return Result.success(true);
    }

    @HasPermission(
            summary = "getCourseVectorCount",
            description = "获取课程的向量化统计信息",
            permission = PermissionConstants.CELESTIAL_QUERY
    )
    @GetMapping("/course/{courseId}/count")
    public Result<Long> getCourseVectorCount(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable UUID courseId) {
        Long count = knowledgeService.getCourseVectorCount(courseId);
        return Result.success(count);
    }
}

