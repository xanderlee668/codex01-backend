package com.codex.backend.web.dto;

/**
 * 任务统计数据响应体，字段设计与前端 DashboardSummary 保持一致，供 /api/tasks/summary 与 /api/dashboard 共用。
 */
public record TaskSummaryResponse(
        long totalTasks,
        long completedTasks,
        long overdueTasks,
        long dueTodayTasks,
        long focusMinutes,
        double completionRate) {}
