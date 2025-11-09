package com.codex.backend.repository;

import com.codex.backend.domain.trip.Trip;
import com.codex.backend.domain.trip.TripParticipant;
import com.codex.backend.domain.user.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 行程成员仓储。
 */
public interface TripParticipantRepository extends JpaRepository<TripParticipant, UUID> {

    List<TripParticipant> findByTrip(Trip trip);

    Optional<TripParticipant> findByTripAndUser(Trip trip, User user);
}
