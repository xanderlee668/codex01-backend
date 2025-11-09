package com.codex.backend.web.dto;

/**
 * 登录/注册成功后返回的响应结构，包含 JWT 与用户信息。
 */
public record AuthResponse(String token, UserPayload user) {

    /**
     * 统一的用户信息载体，字段名称与前端展示完全一致。
     */
    public record UserPayload(
            String userId,
            String email,
            String displayName,
            String location,
            String bio,
            double rating,
            int dealsCount) {
    }
}
