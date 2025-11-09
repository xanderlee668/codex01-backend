package com.codex.backend.web;

import com.codex.backend.domain.task.Task;
import com.codex.backend.domain.user.User;
import com.codex.backend.security.UserDetailsServiceImpl.AuthenticatedUser;
import com.codex.backend.service.TaskFilter;
import com.codex.backend.service.TaskMapper;
import com.codex.backend.service.TaskService;
import com.codex.backend.service.TaskSummary;
import com.codex.backend.web.dto.TaskRequest;
import com.codex.backend.web.dto.TaskResponse;
import com.codex.backend.web.dto.TaskSummaryResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

/**
 * 任务相关 API 控制器，负责接收 iOS 客户端的请求并调用服务层完成业务处理。
 * 所有字段均使用 snake_case，与前端的 `APIClient` 请求保持一致。
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TaskController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    /**
     * 分页/过滤查询任务列表，支持按照完成状态、日期区间、关键字、分类、优先级与标签过滤。
     */
    @GetMapping
    public List<TaskResponse> listTasks(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(value = "completed", required = false) Boolean completed,
            @RequestParam(value = "due_from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate dueFrom,
            @RequestParam(value = "due_to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueTo,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "tag", required = false) String tag) {
        User user = principal.getUser();
        TaskFilter filter = TaskFilter.from(completed, dueFrom, dueTo, keyword, category, priority, tag);
        return taskService.listTasks(user, filter).stream().map(taskMapper::toResponse).collect(Collectors.toList());
    }

    /**
     * 获取任务统计信息（总数、完成数、逾期、今日、专注时长等）。
     */
    @GetMapping("/summary")
    public TaskSummaryResponse getSummary(@AuthenticationPrincipal AuthenticatedUser principal) {
        User user = principal.getUser();
        TaskSummary summary = taskService.getSummary(user);
        return new TaskSummaryResponse(
                summary.total(),
                summary.completed(),
                summary.overdue(),
                summary.dueToday(),
                summary.focusMinutes(),
                summary.completionRate());
    }

    /**
     * 查看单个任务详情。
     */
    @GetMapping("/{id}")
    public TaskResponse getTask(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable Long id) {
        User user = principal.getUser();
        Task task = taskService.getTask(user, id);
        return taskMapper.toResponse(task);
    }

    /**
     * 创建新任务。
     */
    @PostMapping
    public TaskResponse createTask(
            @AuthenticationPrincipal AuthenticatedUser principal, @Valid @RequestBody TaskRequest request) {
        User user = principal.getUser();
        Task task = taskService.createTask(user, request);
        return taskMapper.toResponse(task);
    }

    /**
     * 更新已有任务，前端使用 PUT 以保持幂等。
     */
    @PutMapping("/{id}")
    public TaskResponse updateTask(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        User user = principal.getUser();
        Task task = taskService.updateTask(user, id, request);
        return taskMapper.toResponse(task);
    }

    /**
     * 删除任务。
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable Long id) {
        User user = principal.getUser();
        taskService.deleteTask(user, id);
    }
}
