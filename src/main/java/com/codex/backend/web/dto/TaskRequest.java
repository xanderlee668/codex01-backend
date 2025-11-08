package com.codex.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TaskRequest(
        @NotBlank @Size(max = 150) String title,
        @Size(max = 1000) String notes,
        LocalDate dueDate,
        boolean completed) {
}
