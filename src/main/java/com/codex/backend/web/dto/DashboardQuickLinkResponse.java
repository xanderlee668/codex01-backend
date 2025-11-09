package com.codex.backend.web.dto;

/**
 * 首页快捷入口数据结构，例如“开始专注”、“添加任务”等按钮。
 */
public record DashboardQuickLinkResponse(
        String id,
        String title,
        String icon,
        String target) {}
