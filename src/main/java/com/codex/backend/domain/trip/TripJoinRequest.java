package com.codex.backend.domain.trip;

import com.codex.backend.domain.BaseEntity;
import com.codex.backend.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 行程报名请求。
 */
@Entity
@Table(name = "trip_join_requests")
public class TripJoinRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripRequestStatus status;

    @Column(length = 500)
    private String message;

    protected TripJoinRequest() {
        // JPA only
    }

    public TripJoinRequest(Trip trip, User applicant, TripRequestStatus status, String message) {
        this.trip = trip;
        this.applicant = applicant;
        this.status = status;
        this.message = message;
    }

    public Trip getTrip() {
        return trip;
    }

    public User getApplicant() {
        return applicant;
    }

    public TripRequestStatus getStatus() {
        return status;
    }

    public void setStatus(TripRequestStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }
}
