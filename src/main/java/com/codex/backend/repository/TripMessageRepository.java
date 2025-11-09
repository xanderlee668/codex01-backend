package com.codex.backend.repository;

import com.codex.backend.domain.trip.Trip;
import com.codex.backend.domain.trip.TripMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 行程群聊消息仓储。
 */
public interface TripMessageRepository extends JpaRepository<TripMessage, UUID> {

    List<TripMessage> findByTripOrderByCreatedAtAsc(Trip trip);
}
