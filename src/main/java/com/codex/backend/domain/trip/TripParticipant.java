package com.codex.backend.domain.trip;

import com.codex.backend.domain.BaseEntity;
import com.codex.backend.domain.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 行程成员，含组织者与普通参与者。
 */
@Entity
@Table(name = "trip_participants")
public class TripParticipant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private TripParticipantRole role;

    protected TripParticipant() {
        // JPA only
    }

    public TripParticipant(Trip trip, User user, TripParticipantRole role) {
        this.trip = trip;
        this.user = user;
        this.role = role;
    }

    public Trip getTrip() {
        return trip;
    }

    public User getUser() {
        return user;
    }

    public TripParticipantRole getRole() {
        return role;
    }
}
