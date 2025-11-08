package com.codex.backend.service;

import com.codex.backend.domain.task.Task;
import com.codex.backend.domain.user.User;
import com.codex.backend.repository.TaskRepository;
import com.codex.backend.web.dto.TaskRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<Task> listTasks(User owner) {
        return taskRepository.findAllByOwnerOrderByCreatedAtDesc(owner);
    }

    @Transactional(readOnly = true)
    public Task getTask(User owner, Long id) {
        return taskRepository
                .findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    @Transactional
    public Task createTask(User owner, TaskRequest request) {
        Task task = new Task(owner, request.title(), request.notes(), request.dueDate());
        task.update(request.title(), request.notes(), request.dueDate(), request.completed());
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTask(User owner, Long id, TaskRequest request) {
        Task task = getTask(owner, id);
        task.update(request.title(), request.notes(), request.dueDate(), request.completed());
        return task;
    }

    @Transactional
    public void deleteTask(User owner, Long id) {
        Task task = getTask(owner, id);
        taskRepository.delete(task);
    }
}
