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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 行程实体：用于展示前端行程列表与详情。
 */
@Entity
@Table(name = "trips")
public class Trip extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 120)
    private String destination;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private Instant startAt;

    @Column(nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @OneToMany(mappedBy = "trip")
    private List<TripParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "trip")
    private List<TripJoinRequest> joinRequests = new ArrayList<>();

    @OneToMany(mappedBy = "trip")
    @OrderBy("createdAt ASC")
    private List<TripMessage> messages = new ArrayList<>();

    protected Trip() {
        // JPA only
    }

    public Trip(
            String title,
            String destination,
            String description,
            Instant startAt,
            Instant endAt,
            TripStatus status,
            User organizer) {
        this.title = title;
        this.destination = destination;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = status;
        this.organizer = organizer;
    }

    public String getTitle() {
        return title;
    }

    public String getDestination() {
        return destination;
    }

    public String getDescription() {
        return description;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public User getOrganizer() {
        return organizer;
    }

    public List<TripParticipant> getParticipants() {
        return participants;
    }

    public List<TripJoinRequest> getJoinRequests() {
        return joinRequests;
    }

    public List<TripMessage> getMessages() {
        return messages;
    }
}
