package com.codex.backend.web.dto.trip;

import jakarta.validation.constraints.NotBlank;

/**
 * 行程报名请求体。
 */
public record TripJoinRequestCommand(@NotBlank String message) {}
