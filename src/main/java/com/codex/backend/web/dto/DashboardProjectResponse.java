package com.codex.backend.web.dto;

/**
 * 首页项目/分类卡片，展示某个分类的任务进度信息。
 */
public record DashboardProjectResponse(
        String category,
        long totalTasks,
        long completedTasks,
        long remainingTasks,
        int progress,
        String themeColor) {}
