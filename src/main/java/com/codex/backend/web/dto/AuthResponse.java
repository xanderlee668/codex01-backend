package com.codex.backend.web.dto;

public record AuthResponse(String token, UserSummary user) {

    public record UserSummary(Long id, String email, String displayName) {}
}
