package com.codex.backend.repository;

import com.codex.backend.domain.message.MessageThread;
import com.codex.backend.domain.user.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 会话仓储：按参与者查询站内信线程。
 */
public interface MessageThreadRepository extends JpaRepository<MessageThread, UUID> {

    List<MessageThread> findByBuyerOrSellerOrderByUpdatedAtDesc(User buyer, User seller);

    Optional<MessageThread> findByIdAndBuyerOrSeller(UUID id, User buyer, User seller);
}
