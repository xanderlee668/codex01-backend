package com.codex.backend.web.dto;

/**
 * 任务统计数据响应体，字段设计与前端 DashboardSummary 保持一致。
 */
public record TaskSummaryResponse(
        long total,
        long completed,
        long overdue,
        long dueToday,
        long focusMinutes,
        double completionRate) {}
