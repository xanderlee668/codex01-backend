package com.codex.backend.web.dto;

public record TaskSummaryResponse(long total, long completed, long overdue, long dueToday) {}
