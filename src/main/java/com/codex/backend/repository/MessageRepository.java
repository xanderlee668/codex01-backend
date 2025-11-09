package com.codex.backend.repository;

import com.codex.backend.domain.message.Message;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 站内信消息仓储。
 */
public interface MessageRepository extends JpaRepository<Message, UUID> {}
