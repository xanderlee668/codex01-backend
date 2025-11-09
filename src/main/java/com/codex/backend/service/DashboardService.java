package com.codex.backend.service;

import com.codex.backend.domain.task.Task;
import com.codex.backend.domain.user.User;
import com.codex.backend.repository.TaskRepository;
import com.codex.backend.web.dto.DashboardProjectResponse;
import com.codex.backend.web.dto.DashboardQuickLinkResponse;
import com.codex.backend.web.dto.DashboardResponse;
import com.codex.backend.web.dto.DashboardTaskItemResponse;
import com.codex.backend.web.dto.TaskSummaryResponse;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * 首页聚合数据服务：组装统计、分类进度、今日/即将到来的任务、快捷入口。
 */
@Service
public class DashboardService {

    private final TaskRepository taskRepository;
    private final TaskService taskService;

    public DashboardService(TaskRepository taskRepository, TaskService taskService) {
        this.taskRepository = taskRepository;
        this.taskService = taskService;
    }

    /**
     * 构建首页响应数据，调用 TaskService 计算统计并补充分类、任务列表等信息。
     */
    public DashboardResponse buildDashboard(User user) {
        List<Task> tasks = taskRepository.findAllByOwner(user);
        TaskSummary summary = taskService.getSummary(user);
        TaskSummaryResponse summaryResponse = toSummaryResponse(summary);

        List<DashboardProjectResponse> projects = buildProjectResponses(tasks);
        List<DashboardTaskItemResponse> todayTasks = buildTaskCards(tasks, LocalDate.now());
        List<DashboardTaskItemResponse> upcomingTasks = buildUpcomingTaskCards(tasks);
        List<DashboardQuickLinkResponse> quickLinks = buildQuickLinks();

        return new DashboardResponse(summaryResponse, projects, todayTasks, upcomingTasks, quickLinks);
    }

    private List<DashboardProjectResponse> buildProjectResponses(List<Task> tasks) {
        Map<String, List<Task>> grouped = tasks.stream()
                .collect(Collectors.groupingBy(task -> normalize(task.getCategory())));

        return grouped.entrySet().stream()
                .map(entry -> toProjectResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(DashboardProjectResponse::category, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    /**
     * 根据任务分类构建项目卡片，progress 直接输出 0~1 的小数，方便 SwiftUI ProgressView 使用。
     */
    private DashboardProjectResponse toProjectResponse(String rawCategory, List<Task> tasks) {
        String category = rawCategory == null ? "未分类" : rawCategory;
        long total = tasks.size();
        long completed = tasks.stream().filter(Task::isCompleted).count();
        long remaining = total - completed;
        double progress = total == 0 ? 0.0 : (double) completed / (double) total;
        String themeColor = pickThemeColor(category);
        return new DashboardProjectResponse(category, total, completed, remaining, progress, themeColor);
    }

    private String pickThemeColor(String category) {
        if (category == null) {
            return "#6366F1";
        }
        return switch (category.toLowerCase(Locale.ROOT)) {
            case "design" -> "#F97316";
            case "development" -> "#22C55E";
            case "marketing" -> "#06B6D4";
            case "personal" -> "#A855F7";
            default -> "#6366F1";
        };
    }

    /**
     * 构建“今日任务”列表，包含今日到期的所有任务，并优先展示未完成项。
     */
    private List<DashboardTaskItemResponse> buildTaskCards(List<Task> tasks, LocalDate targetDate) {
        return tasks.stream()
                .filter(task -> task.getDueDate() != null && task.getDueDate().isEqual(targetDate))
                .sorted(Comparator.comparing(Task::isCompleted).thenComparing(Task::getDueDate, Comparator.nullsLast(LocalDate::compareTo)))
                .map(this::toDashboardTaskItem)
                .toList();
    }

    /**
     * 构建“即将开始”任务卡片，只展示未来 7 天内且尚未完成的任务，确保与前端标签区文案一致。
     */
    private List<DashboardTaskItemResponse> buildUpcomingTaskCards(List<Task> tasks) {
        LocalDate today = LocalDate.now();
        LocalDate sevenDays = today.plusDays(7);
        return tasks.stream()
                .filter(task -> task.getDueDate() != null)
                .filter(task -> !task.isCompleted())
                .filter(task -> task.getDueDate().isAfter(today.minusDays(1)))
                .filter(task -> task.getDueDate().isBefore(sevenDays.plusDays(1)))
                .sorted(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(LocalDate::compareTo)))
                .map(this::toDashboardTaskItem)
                .toList();
    }

    private DashboardTaskItemResponse toDashboardTaskItem(Task task) {
        return new DashboardTaskItemResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getCategory(),
                task.getPriority(),
                task.getDueDate(),
                task.getEstimatedMinutes(),
                task.getTags(),
                task.isCompleted());
    }

    private List<DashboardQuickLinkResponse> buildQuickLinks() {
        return List.of(
                new DashboardQuickLinkResponse("start_focus", "开始专注", "timer", "focus"),
                new DashboardQuickLinkResponse("new_task", "新建任务", "plus", "tasks/new"),
                new DashboardQuickLinkResponse("view_projects", "查看项目", "rectangle.stack", "projects"));
    }

    private String normalize(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        return category.trim();
    }

    private TaskSummaryResponse toSummaryResponse(TaskSummary summary) {
        return new TaskSummaryResponse(
                summary.total(),
                summary.completed(),
                summary.overdue(),
                summary.dueToday(),
                summary.focusMinutes(),
                summary.completionRate());
    }
}
