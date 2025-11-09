package com.codex.backend.web.dto.message;

import jakarta.validation.constraints.NotBlank;

/**
 * 新建站内信线程请求体。
 */
public record CreateMessageThreadRequest(@NotBlank String listingId, @NotBlank String message) {}
