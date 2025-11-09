package com.codex.backend.domain.trip;

/**
 * 行程成员角色：区分组织者与普通参与者。
 */
public enum TripParticipantRole {
    ORGANIZER,
    MEMBER;

    public String toJson() {
        return name().toLowerCase();
    }
}
