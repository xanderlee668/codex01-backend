package com.codex.backend.service;

import com.codex.backend.domain.task.Task;
import com.codex.backend.web.dto.TaskResponse;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getCategory(),
                task.getPriority(),
                task.getDueDate(),
                task.getEstimatedMinutes(),
                task.getTags(),
                task.isCompleted(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}
