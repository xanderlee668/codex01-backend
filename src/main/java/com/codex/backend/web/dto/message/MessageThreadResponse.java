package com.codex.backend.web.dto.message;

import com.codex.backend.web.dto.ListingResponse;
import java.time.Instant;
import java.util.List;

/**
 * 站内信线程响应结构。
 */
public record MessageThreadResponse(
        String threadId,
        ListingResponse listing,
        ParticipantResponse buyer,
        ParticipantResponse seller,
        List<MessageResponse> messages,
        int unreadCount,
        boolean archived,
        Instant updatedAt) {

    /** 用户摘要响应。 */
    public record ParticipantResponse(String userId, String displayName, Double rating, Integer dealsCount) {}

    /** 消息响应节点。 */
    public record MessageResponse(String messageId, String senderId, String content, Instant sentAt) {}
}
