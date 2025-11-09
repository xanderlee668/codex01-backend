package com.codex.backend.domain.message;

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
 * 站内信消息：包含发送者、内容与时间。
 */
@Entity
@Table(name = "messages")
public class Message extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private MessageThread thread;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 2000)
    private String content;

    protected Message() {
        // JPA only
    }

    public Message(MessageThread thread, User sender, String content) {
        this.thread = thread;
        this.sender = sender;
        this.content = content;
    }

    public MessageThread getThread() {
        return thread;
    }

    public User getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    /**
     * 统一使用 BaseEntity 的创建时间作为消息发送时间。
     */
    public Instant getSentAt() {
        return getCreatedAt();
    }
}
