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

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    public TaskController(TaskService taskService, TaskMapper taskMapper) {
        this.taskService = taskService;
        this.taskMapper = taskMapper;
    }

    @GetMapping
    public List<TaskResponse> listTasks(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam(value = "completed", required = false) Boolean completed,
            @RequestParam(value = "due_from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate dueFrom,
            @RequestParam(value = "due_to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueTo,
            @RequestParam(value = "keyword", required = false) String keyword) {
        User user = principal.getUser();
        TaskFilter filter = TaskFilter.from(completed, dueFrom, dueTo, keyword);
        return taskService.listTasks(user, filter).stream().map(taskMapper::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/summary")
    public TaskSummaryResponse getSummary(@AuthenticationPrincipal AuthenticatedUser principal) {
        User user = principal.getUser();
        TaskSummary summary = taskService.getSummary(user);
        return new TaskSummaryResponse(summary.total(), summary.completed(), summary.overdue(), summary.dueToday());
    }

    @GetMapping("/{id}")
    public TaskResponse getTask(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable Long id) {
        User user = principal.getUser();
        Task task = taskService.getTask(user, id);
        return taskMapper.toResponse(task);
    }

    @PostMapping
    public TaskResponse createTask(
            @AuthenticationPrincipal AuthenticatedUser principal, @Valid @RequestBody TaskRequest request) {
        User user = principal.getUser();
        Task task = taskService.createTask(user, request);
        return taskMapper.toResponse(task);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        User user = principal.getUser();
        Task task = taskService.updateTask(user, id, request);
        return taskMapper.toResponse(task);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable Long id) {
        User user = principal.getUser();
        taskService.deleteTask(user, id);
    }
}
