package com.codex.backend.web.dto.trip;

import java.time.Instant;
import java.util.List;

/**
 * 行程响应结构，包含成员、报名与群聊消息。
 */
public record TripResponse(
        String tripId,
        String title,
        String destination,
        String description,
        Instant startAt,
        Instant endAt,
        String status,
        TripMemberResponse organizer,
        List<TripMemberResponse> participants,
        List<TripJoinRequestResponse> pendingRequests,
        List<TripMessageResponse> messages) {

    /** 行程成员结构。 */
    public record TripMemberResponse(
            String userId, String displayName, String location, Double rating, Integer dealsCount, String role) {}

    /** 报名请求结构。 */
    public record TripJoinRequestResponse(
            String requestId, TripMemberResponse applicant, String status, String message, Instant createdAt) {}

    /** 群聊消息结构。 */
    public record TripMessageResponse(
            String messageId, TripMemberResponse sender, String content, Instant sentAt) {}
}
