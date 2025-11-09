package com.codex.backend.repository;

import com.codex.backend.domain.trip.Trip;
import com.codex.backend.domain.user.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 行程仓储：提供按成员或组织者查询的能力。
 */
public interface TripRepository extends JpaRepository<Trip, UUID> {

    List<Trip> findByOrganizerOrParticipants_User(User organizer, User participant);

    Optional<Trip> findByIdAndOrganizer(UUID id, User organizer);
}
