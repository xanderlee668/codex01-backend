package com.codex.backend.domain.trip;

/**
 * 行程报名请求的状态枚举。
 */
public enum TripRequestStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public String toJson() {
        return name().toLowerCase();
    }
}
