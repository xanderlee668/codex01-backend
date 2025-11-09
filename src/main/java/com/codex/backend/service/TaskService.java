package com.codex.backend.service;

import com.codex.backend.domain.task.Task;
import com.codex.backend.domain.user.User;
import com.codex.backend.repository.TaskRepository;
import com.codex.backend.web.dto.TaskRequest;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
    public List<Task> listTasks(User owner, TaskFilter filter) {
        List<Task> tasks = taskRepository.findAllByOwner(owner);

        return tasks.stream()
                .filter(task -> filter.completed().map(value -> task.isCompleted() == value).orElse(true))
                .filter(task -> filter.dueFrom().map(date -> isDueOnOrAfter(task, date)).orElse(true))
                .filter(task -> filter.dueTo().map(date -> isDueOnOrBefore(task, date)).orElse(true))
                .filter(task -> filter.keyword().map(keyword -> matchesKeyword(task, keyword)).orElse(true))
                .sorted(buildDefaultComparator())
                .toList();
    }

    @Transactional(readOnly = true)
    public Task getTask(User owner, Long id) {
        return taskRepository
                .findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    @Transactional
    public Task createTask(User owner, TaskRequest request) {
        Task task = new Task(owner, request.title(), request.notes(), request.dueDate(), request.completed());
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

    @Transactional(readOnly = true)
    public TaskSummary getSummary(User owner) {
        List<Task> tasks = taskRepository.findAllByOwner(owner);
        long total = tasks.size();
        long completed = tasks.stream().filter(Task::isCompleted).count();
        LocalDate today = LocalDate.now();
        long overdue = tasks.stream()
                .filter(task -> !task.isCompleted())
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(today))
                .count();
        long dueToday = tasks.stream()
                .filter(task -> !task.isCompleted())
                .filter(task -> task.getDueDate() != null && task.getDueDate().isEqual(today))
                .count();
        return new TaskSummary(total, completed, overdue, dueToday);
    }

    private boolean isDueOnOrAfter(Task task, LocalDate date) {
        return task.getDueDate() != null && !task.getDueDate().isBefore(date);
    }

    private boolean isDueOnOrBefore(Task task, LocalDate date) {
        return task.getDueDate() != null && !task.getDueDate().isAfter(date);
    }

    private boolean matchesKeyword(Task task, String keyword) {
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        return containsIgnoreCase(task.getTitle(), lowerKeyword) || containsIgnoreCase(task.getNotes(), lowerKeyword);
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private Comparator<Task> buildDefaultComparator() {
        return Comparator.comparing(Task::isCompleted)
                .thenComparing(task -> task.getDueDate(), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Task::getCreatedAt, Comparator.reverseOrder());
    }
}
