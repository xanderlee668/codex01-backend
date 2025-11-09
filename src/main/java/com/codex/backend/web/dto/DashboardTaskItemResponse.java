package com.codex.backend.web.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 首页任务卡片响应，复用 Task 基础字段并补充展示需要的分类与标签。
 */
public record DashboardTaskItemResponse(
        Long id,
        String title,
        String description,
        String category,
        String priority,
        LocalDate dueDate,
        Integer estimatedMinutes,
        List<String> tags,
        boolean completed) {}
