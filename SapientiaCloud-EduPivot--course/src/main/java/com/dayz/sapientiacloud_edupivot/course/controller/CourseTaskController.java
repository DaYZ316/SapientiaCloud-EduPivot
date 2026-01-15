package com.dayz.sapientiacloud_edupivot.course.controller;

import com.dayz.sapientiacloud_edupivot.course.common.controller.BaseController;
import com.dayz.sapientiacloud_edupivot.course.common.result.Result;
import com.dayz.sapientiacloud_edupivot.course.common.result.TableDataResult;
import com.dayz.sapientiacloud_edupivot.course.common.security.annotation.HasPermission;
import com.dayz.sapientiacloud_edupivot.course.constant.PermissionConstants;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseTaskDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.dto.CourseTaskQueryDTO;
import com.dayz.sapientiacloud_edupivot.course.entity.vo.CourseTaskVO;
import com.dayz.sapientiacloud_edupivot.course.service.ICourseTaskService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "课程任务管理", description = "用于管理课程任务的API")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class CourseTaskController extends BaseController {

    private final ICourseTaskService courseTaskService;

    @Operation(summary = "listCourseTask", description = "根据传入的条件分页查询课程任务信息。支持根据任务名称、类型、难度、状态等字段进行查询。")
    @HasPermission(
            summary = "listCourseTask",
            description = "根据传入的条件分页查询课程任务信息。支持根据任务名称、类型、难度、状态等字段进行查询。",
            permission = PermissionConstants.TASK_QUERY
    )
    @GetMapping("/list")
    public TableDataResult listCourseTask(@ParameterObject CourseTaskQueryDTO courseTaskQueryDTO) {
        startPage();
        PageInfo<CourseTaskVO> pageInfo = courseTaskService.listCourseTask(courseTaskQueryDTO);
        return getDataTable(pageInfo);
    }

    @Operation(summary = "listAllCourseTaskByCourseId", description = "根据课程ID获取该课程下的所有任务列表。")
    @HasPermission(
            summary = "listAllCourseTaskByCourseId",
            description = "根据课程ID获取该课程下的所有任务列表。",
            permission = PermissionConstants.TASK_QUERY
    )
    @GetMapping("/course/{courseId}")
    public Result<List<CourseTaskVO>> listAllCourseTaskByCourseId(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId
    ) {
        List<CourseTaskVO> taskList = courseTaskService.listAllCourseTaskByCourseId(courseId);
        return Result.success(taskList);
    }

    @Operation(summary = "listAllCourseTaskByUserId", description = "根据用户ID获取该用户创建的所有任务列表。")
    @HasPermission(
            summary = "listAllCourseTaskByUserId",
            description = "根据用户ID获取该用户创建的所有任务列表。",
            permission = PermissionConstants.TASK_QUERY
    )
    @GetMapping("/user/{sysUserId}")
    public Result<List<CourseTaskVO>> listAllCourseTaskByUserId(
            @Parameter(name = "sysUserId", description = "用户ID", required = true) @PathVariable("sysUserId") UUID sysUserId
    ) {
        List<CourseTaskVO> taskList = courseTaskService.listAllCourseTaskByUserId(sysUserId);
        return Result.success(taskList);
    }

    @Operation(summary = "listAllCourseTask", description = "获取所有课程任务列表。")
    @HasPermission(
            summary = "listAllCourseTask",
            description = "获取所有课程任务列表。",
            permission = PermissionConstants.TASK_QUERY
    )
    @GetMapping("/all")
    public Result<List<CourseTaskVO>> listAllCourseTask() {
        List<CourseTaskVO> taskList = courseTaskService.listAllCourseTask();
        return Result.success(taskList);
    }

    @Operation(summary = "listCourseTaskByCourseIdAndStatus", description = "根据课程ID和状态获取任务列表。")
    @HasPermission(
            summary = "listCourseTaskByCourseIdAndStatus",
            description = "根据课程ID和状态获取任务列表。",
            permission = PermissionConstants.TASK_QUERY
    )
    @GetMapping("/course/{courseId}/status/{status}")
    public Result<List<CourseTaskVO>> listCourseTaskByCourseIdAndStatus(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId,
            @Parameter(name = "status", description = "任务状态", required = true) @PathVariable("status") Integer status
    ) {
        List<CourseTaskVO> taskList = courseTaskService.listCourseTaskByCourseIdAndStatus(courseId, status);
        return Result.success(taskList);
    }

    @Operation(summary = "listCourseTaskByCourseIdAndTaskType", description = "根据课程ID和任务类型获取任务列表。")
    @HasPermission(
            summary = "listCourseTaskByCourseIdAndTaskType",
            description = "根据课程ID和任务类型获取任务列表。",
            permission = PermissionConstants.TASK_QUERY
    )
    @GetMapping("/course/{courseId}/type/{taskType}")
    public Result<List<CourseTaskVO>> listCourseTaskByCourseIdAndTaskType(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId,
            @Parameter(name = "taskType", description = "任务类型", required = true) @PathVariable("taskType") Integer taskType
    ) {
        List<CourseTaskVO> taskList = courseTaskService.listCourseTaskByCourseIdAndTaskType(courseId, taskType);
        return Result.success(taskList);
    }

    @Operation(summary = "listCourseTaskByCourseIdAndDifficulty", description = "根据课程ID和难度获取任务列表。")
    @HasPermission(
            summary = "listCourseTaskByCourseIdAndDifficulty",
            description = "根据课程ID和难度获取任务列表。",
            permission = PermissionConstants.TASK_QUERY
    )
    @GetMapping("/course/{courseId}/difficulty/{difficulty}")
    public Result<List<CourseTaskVO>> listCourseTaskByCourseIdAndDifficulty(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId,
            @Parameter(name = "difficulty", description = "难度等级", required = true) @PathVariable("difficulty") Integer difficulty
    ) {
        List<CourseTaskVO> taskList = courseTaskService.listCourseTaskByCourseIdAndDifficulty(courseId, difficulty);
        return Result.success(taskList);
    }

    @Operation(summary = "getCourseTaskById", description = "通过任务的唯一ID获取其详细信息。")
    @HasPermission(
            summary = "getCourseTaskById",
            description = "通过任务的唯一ID获取其详细信息。",
            permission = PermissionConstants.TASK_QUERY
    )
    @GetMapping("/{id}")
    public Result<CourseTaskVO> getCourseTaskById(
            @Parameter(name = "id", description = "任务ID", required = true) @PathVariable("id") UUID id
    ) {
        CourseTaskVO courseTaskVO = courseTaskService.getCourseTaskById(id);
        return Result.success(courseTaskVO);
    }

    @Operation(summary = "addCourseTask", description = "创建新的课程任务。")
    @HasPermission(
            summary = "addCourseTask",
            description = "创建新的课程任务。",
            permission = PermissionConstants.TASK_ADD
    )
    @PostMapping
    public Result<CourseTaskVO> addCourseTask(@Valid @RequestBody CourseTaskDTO courseTaskDTO) {
        CourseTaskVO courseTaskVO = courseTaskService.addCourseTask(courseTaskDTO);
        return Result.success(courseTaskVO);
    }

    @Operation(summary = "updateCourseTask", description = "更新现有任务的信息。")
    @HasPermission(
            summary = "updateCourseTask",
            description = "更新现有任务的信息。",
            permission = PermissionConstants.TASK_EDIT
    )
    @PutMapping
    public Result<Boolean> updateCourseTask(@Valid @RequestBody CourseTaskDTO courseTaskDTO) {
        Boolean result = courseTaskService.updateCourseTask(courseTaskDTO);
        return Result.success(result);
    }

    @Operation(summary = "removeCourseTaskById", description = "根据任务ID删除任务。")
    @HasPermission(
            summary = "removeCourseTaskById",
            description = "根据任务ID删除任务。",
            permission = PermissionConstants.TASK_DELETE
    )
    @DeleteMapping("/{id}")
    public Result<Boolean> removeCourseTaskById(
            @Parameter(name = "id", description = "任务ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = courseTaskService.removeCourseTaskById(id);
        return Result.success(result);
    }

    @Operation(summary = "removeCourseTaskByIds", description = "根据任务ID列表批量删除任务。")
    @HasPermission(
            summary = "removeCourseTaskByIds",
            description = "根据任务ID列表批量删除任务。",
            permission = PermissionConstants.TASK_DELETE
    )
    @DeleteMapping
    public Result<Integer> removeCourseTaskByIds(
            @Parameter(name = "ids", description = "任务ID列表", required = true) @RequestBody List<UUID> ids
    ) {
        Integer result = courseTaskService.removeCourseTaskByIds(ids);
        return Result.success(result);
    }

    @Operation(summary = "updateTaskStatus", description = "更新任务状态。")
    @HasPermission(
            summary = "updateTaskStatus",
            description = "更新任务状态。",
            permission = PermissionConstants.TASK_EDIT
    )
    @PutMapping("/{id}/status/{status}")
    public Result<Boolean> updateTaskStatus(
            @Parameter(name = "id", description = "任务ID", required = true) @PathVariable("id") UUID id,
            @Parameter(name = "status", description = "任务状态", required = true) @PathVariable("status") Integer status
    ) {
        Boolean result = courseTaskService.updateTaskStatus(id, status);
        return Result.success(result);
    }

    @Operation(summary = "publishTask", description = "发布任务。")
    @HasPermission(
            summary = "publishTask",
            description = "发布任务。",
            permission = PermissionConstants.TASK_PUBLISH
    )
    @PutMapping("/{id}/publish")
    public Result<Boolean> publishTask(
            @Parameter(name = "id", description = "任务ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = courseTaskService.publishTask(id);
        return Result.success(result);
    }

    @Operation(summary = "unpublishTask", description = "取消发布任务。")
    @HasPermission(
            summary = "unpublishTask",
            description = "取消发布任务。",
            permission = PermissionConstants.TASK_EDIT
    )
    @PutMapping("/{id}/unpublish")
    public Result<Boolean> unpublishTask(
            @Parameter(name = "id", description = "任务ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = courseTaskService.unpublishTask(id);
        return Result.success(result);
    }

    @Operation(summary = "startTask", description = "开始任务。")
    @HasPermission(
            summary = "startTask",
            description = "开始任务。",
            permission = PermissionConstants.TASK_EDIT
    )
    @PutMapping("/{id}/start")
    public Result<Boolean> startTask(
            @Parameter(name = "id", description = "任务ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = courseTaskService.startTask(id);
        return Result.success(result);
    }

    @Operation(summary = "endTask", description = "结束任务。")
    @HasPermission(
            summary = "endTask",
            description = "结束任务。",
            permission = PermissionConstants.TASK_EDIT
    )
    @PutMapping("/{id}/end")
    public Result<Boolean> endTask(
            @Parameter(name = "id", description = "任务ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = courseTaskService.endTask(id);
        return Result.success(result);
    }

    @Operation(summary = "viewTask", description = "增加任务浏览次数。")
    @HasPermission(
            summary = "viewTask",
            description = "增加任务浏览次数。",
            permission = PermissionConstants.TASK_VIEW
    )
    @PutMapping("/{id}/view")
    public Result<Boolean> viewTask(
            @Parameter(name = "id", description = "任务ID", required = true) @PathVariable("id") UUID id
    ) {
        Boolean result = courseTaskService.viewTask(id);
        return Result.success(result);
    }

    @Operation(summary = "getHotTasks", description = "获取热门任务列表。")
    @HasPermission(
            summary = "getHotTasks",
            description = "获取热门任务列表。",
            permission = PermissionConstants.TASK_QUERY
    )
    @GetMapping("/course/{courseId}/hot")
    public Result<List<CourseTaskVO>> getHotTasks(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId,
            @Parameter(name = "limit", description = "限制数量") @RequestParam(value = "limit", required = false) Integer limit
    ) {
        List<CourseTaskVO> taskList = courseTaskService.getHotTasks(courseId, limit);
        return Result.success(taskList);
    }

    @Operation(summary = "getLatestTasks", description = "获取最新任务列表。")
    @HasPermission(
            summary = "getLatestTasks",
            description = "获取最新任务列表。",
            permission = PermissionConstants.TASK_QUERY
    )
    @GetMapping("/course/{courseId}/latest")
    public Result<List<CourseTaskVO>> getLatestTasks(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId,
            @Parameter(name = "limit", description = "限制数量") @RequestParam(value = "limit", required = false) Integer limit
    ) {
        List<CourseTaskVO> taskList = courseTaskService.getLatestTasks(courseId, limit);
        return Result.success(taskList);
    }

    @Operation(summary = "getTasksByTag", description = "根据标签获取任务列表。")
    @HasPermission(
            summary = "getTasksByTag",
            description = "根据标签获取任务列表。",
            permission = PermissionConstants.TASK_QUERY
    )
    @GetMapping("/tag/{tag}")
    public Result<List<CourseTaskVO>> getTasksByTag(
            @Parameter(name = "tag", description = "标签", required = true) @PathVariable("tag") String tag,
            @Parameter(name = "limit", description = "限制数量") @RequestParam(value = "limit", required = false) Integer limit
    ) {
        List<CourseTaskVO> taskList = courseTaskService.getTasksByTag(tag, limit);
        return Result.success(taskList);
    }

    @Operation(summary = "searchTasksByName", description = "根据任务名称搜索任务。")
    @HasPermission(
            summary = "searchTasksByName",
            description = "根据任务名称搜索任务。",
            permission = PermissionConstants.TASK_QUERY
    )
    @GetMapping("/search")
    public Result<List<CourseTaskVO>> searchTasksByName(
            @Parameter(name = "taskName", description = "任务名称", required = true) @RequestParam("taskName") String taskName,
            @Parameter(name = "limit", description = "限制数量") @RequestParam(value = "limit", required = false) Integer limit
    ) {
        List<CourseTaskVO> taskList = courseTaskService.searchTasksByName(taskName, limit);
        return Result.success(taskList);
    }

    @Operation(summary = "getTaskStatistics", description = "获取任务统计信息。")
    @HasPermission(
            summary = "getTaskStatistics",
            description = "获取任务统计信息。",
            permission = PermissionConstants.TASK_QUERY
    )
    @GetMapping("/course/{courseId}/statistics")
    public Result<Map<String, Object>> getTaskStatistics(
            @Parameter(name = "courseId", description = "课程ID", required = true) @PathVariable("courseId") UUID courseId
    ) {
        Map<String, Object> statistics = courseTaskService.getTaskStatistics(courseId);
        return Result.success(statistics);
    }

    @Operation(summary = "getUserTaskStatistics", description = "获取用户任务统计信息。")
    @HasPermission(
            summary = "getUserTaskStatistics",
            description = "获取用户任务统计信息。",
            permission = PermissionConstants.TASK_QUERY
    )
    @GetMapping("/user/{sysUserId}/statistics")
    public Result<Map<String, Object>> getUserTaskStatistics(
            @Parameter(name = "sysUserId", description = "用户ID", required = true) @PathVariable("sysUserId") UUID sysUserId
    ) {
        Map<String, Object> statistics = courseTaskService.getUserTaskStatistics(sysUserId);
        return Result.success(statistics);
    }
}
