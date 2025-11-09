package com.codex.backend.domain.trip;

import com.codex.backend.domain.BaseEntity;
import com.codex.backend.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * 行程群聊消息。
 */
@Entity
@Table(name = "trip_messages")
public class TripMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 2000)
    private String content;

    protected TripMessage() {
        // JPA only
    }

    public TripMessage(Trip trip, User sender, String content) {
        this.trip = trip;
        this.sender = sender;
        this.content = content;
    }

    public Trip getTrip() {
        return trip;
    }

    public User getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    public Instant getSentAt() {
        return getCreatedAt();
    }
}
