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

/**
 * 任务业务逻辑服务层，负责处理筛选、统计等核心能力，供 REST 控制器调用。
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * 查询用户任务并根据筛选条件过滤，过滤逻辑与前端筛选器保持一致。
     */
    @Transactional(readOnly = true)
    public List<Task> listTasks(User owner, TaskFilter filter) {
        List<Task> tasks = taskRepository.findAllByOwner(owner);

        return tasks.stream()
                .filter(task -> filter.completed().map(value -> task.isCompleted() == value).orElse(true))
                .filter(task -> filter.dueFrom().map(date -> isDueOnOrAfter(task, date)).orElse(true))
                .filter(task -> filter.dueTo().map(date -> isDueOnOrBefore(task, date)).orElse(true))
                .filter(task -> filter.keyword().map(keyword -> matchesKeyword(task, keyword)).orElse(true))
                .filter(task -> filter.category().map(category -> matchesEquals(task.getCategory(), category)).orElse(true))
                .filter(task -> filter.priority().map(priority -> matchesEquals(task.getPriority(), priority)).orElse(true))
                .filter(task -> filter.tag().map(tag -> task.getTags().stream().anyMatch(t -> t.equalsIgnoreCase(tag))).orElse(true))
                .sorted(buildDefaultComparator())
                .toList();
    }

    /**
     * 获取指定任务详情，如不存在则抛出 404。
     */
    @Transactional(readOnly = true)
    public Task getTask(User owner, Long id) {
        return taskRepository
                .findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    /**
     * 创建任务。
     */
    @Transactional
    public Task createTask(User owner, TaskRequest request) {
        Task task = new Task(
                owner,
                request.title(),
                request.description(),
                request.category(),
                request.priority(),
                request.dueDate(),
                request.estimatedMinutes(),
                request.tags(),
                request.completed());
        return taskRepository.save(task);
    }

    /**
     * 更新任务。
     */
    @Transactional
    public Task updateTask(User owner, Long id, TaskRequest request) {
        Task task = getTask(owner, id);
        task.update(
                request.title(),
                request.description(),
                request.category(),
                request.priority(),
                request.dueDate(),
                request.estimatedMinutes(),
                request.tags(),
                request.completed());
        return task;
    }

    /**
     * 删除任务。
     */
    @Transactional
    public void deleteTask(User owner, Long id) {
        Task task = getTask(owner, id);
        taskRepository.delete(task);
    }

    /**
     * 汇总任务统计数据，为首页组件提供指标。
     */
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
        long focusMinutes = tasks.stream()
                .filter(Task::isCompleted)
                .map(Task::getEstimatedMinutes)
                .filter(minutes -> minutes != null && minutes > 0)
                .mapToLong(Integer::longValue)
                .sum();
        double completionRate = total == 0 ? 0.0 : (double) completed / (double) total;
        return new TaskSummary(total, completed, overdue, dueToday, focusMinutes, completionRate);
    }

    private boolean isDueOnOrAfter(Task task, LocalDate date) {
        return task.getDueDate() != null && !task.getDueDate().isBefore(date);
    }

    private boolean isDueOnOrBefore(Task task, LocalDate date) {
        return task.getDueDate() != null && !task.getDueDate().isAfter(date);
    }

    private boolean matchesKeyword(Task task, String keyword) {
        String lowerKeyword = keyword.toLowerCase(Locale.ROOT);
        return containsIgnoreCase(task.getTitle(), lowerKeyword)
                || containsIgnoreCase(task.getDescription(), lowerKeyword)
                || containsIgnoreCase(task.getCategory(), lowerKeyword)
                || task.getTags().stream().anyMatch(tag -> tag.toLowerCase(Locale.ROOT).contains(lowerKeyword));
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private Comparator<Task> buildDefaultComparator() {
        return Comparator.comparing(Task::isCompleted)
                .thenComparing(task -> task.getDueDate(), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Task::getCreatedAt, Comparator.reverseOrder());
    }

    private boolean matchesEquals(String source, String target) {
        return source != null && source.equalsIgnoreCase(target);
    }
}
