package com.codex.backend.web.dto;

import java.util.List;

/**
 * iOS 首页聚合数据响应，组合统计、项目、任务与快捷入口信息。
 * summary 字段直接复用 TaskSummaryResponse，避免前后端重复解析结构。
 */
public record DashboardResponse(
        TaskSummaryResponse summary,
        List<DashboardProjectResponse> projects,
        List<DashboardTaskItemResponse> todayTasks,
        List<DashboardTaskItemResponse> upcomingTasks,
        List<DashboardQuickLinkResponse> quickLinks) {}
