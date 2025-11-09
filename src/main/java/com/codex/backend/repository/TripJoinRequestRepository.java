package com.codex.backend.repository;

import com.codex.backend.domain.trip.Trip;
import com.codex.backend.domain.trip.TripJoinRequest;
import com.codex.backend.domain.user.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 行程报名仓储。
 */
public interface TripJoinRequestRepository extends JpaRepository<TripJoinRequest, UUID> {

    Optional<TripJoinRequest> findByTripAndApplicant(Trip trip, User applicant);
}
