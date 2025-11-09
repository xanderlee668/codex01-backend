package com.codex.backend.web.dto.trip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * 创建行程请求体。
 */
public record CreateTripRequest(
        @NotBlank String title,
        @NotBlank String destination,
        @NotBlank String description,
        @NotNull Instant startAt,
        @NotNull Instant endAt,
        String status) {}
