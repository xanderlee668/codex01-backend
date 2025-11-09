package com.codex.backend.web.dto;

/**
 * 会话恢复接口的响应，只返回用户节点以便前端合并状态。
 */
public record CurrentUserResponse(AuthResponse.UserPayload user) {
}
