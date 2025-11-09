package com.codex.backend.service;

/**
 * 任务统计结果，用于 iOS 首页/统计组件展示。
 */
public record TaskSummary(
        long total,
        long completed,
        long overdue,
        long dueToday,
        long focusMinutes,
        double completionRate) {}
