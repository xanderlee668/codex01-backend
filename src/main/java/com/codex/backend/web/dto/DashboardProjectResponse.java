package com.codex.backend.web.dto;

/**
 * 首页项目/分类卡片，展示某个分类的任务进度信息。
 * progress 采用 0~1 的小数，方便前端 ProgressView 直接绑定。
 */
public record DashboardProjectResponse(
        String category,
        long totalTasks,
        long completedTasks,
        long remainingTasks,
        double progress,
        String themeColor) {}
