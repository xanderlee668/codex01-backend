package com.codex.backend.web.dto.trip;

import jakarta.validation.constraints.NotBlank;

/**
 * 行程群聊发送消息请求体。
 */
public record SendTripMessageRequest(@NotBlank String content) {}
