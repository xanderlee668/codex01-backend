package com.codex.backend.web.dto;

import java.util.List;

/**
 * iOS 首页聚合数据响应，组合统计、项目、任务与快捷入口信息。
 */
public record DashboardResponse(
        DashboardSummaryResponse summary,
        List<DashboardProjectResponse> projects,
        List<DashboardTaskItemResponse> todayTasks,
        List<DashboardTaskItemResponse> upcomingTasks,
        List<DashboardQuickLinkResponse> quickLinks) {}
