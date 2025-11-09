package com.codex.backend.domain.trip;

/**
 * 行程状态：与前端枚举保持一致。
 */
public enum TripStatus {
    PLANNED,
    UPCOMING,
    ACTIVE,
    COMPLETED;

    public String toJson() {
        return name().toLowerCase();
    }

    public static TripStatus fromJson(String value) {
        if (value == null) {
            return null;
        }
        return TripStatus.valueOf(value.toUpperCase());
    }
}
