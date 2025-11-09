package com.codex.backend.web.dto.message;

import jakarta.validation.constraints.NotBlank;

/**
 * 发送站内信请求体。
 */
public record SendMessageRequest(@NotBlank String content) {}
