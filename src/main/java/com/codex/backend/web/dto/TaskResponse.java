package com.codex.backend.web.dto;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        String title,
        String notes,
        LocalDate dueDate,
        boolean completed,
        Instant createdAt,
        Instant updatedAt) {
}
