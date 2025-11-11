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
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        String lowerCased = normalized.toLowerCase();
        return switch (lowerCased) {
            case "planned", "planning" -> PLANNED;
            case "upcoming" -> UPCOMING;
            case "active", "ongoing" -> ACTIVE;
            case "completed", "complete" -> COMPLETED;
            default -> throw new IllegalArgumentException("Unsupported trip status: " + value);
        };
    }
}
