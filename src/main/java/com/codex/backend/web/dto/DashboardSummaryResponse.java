package com.codex.backend.web.dto;

/**
 * 首页统计数据响应，用于呈现任务数量、专注时长、完成率等指标。
 */
public record DashboardSummaryResponse(
        long totalTasks,
        long completedTasks,
        long overdueTasks,
        long dueTodayTasks,
        long focusMinutes,
        double completionRate) {}
