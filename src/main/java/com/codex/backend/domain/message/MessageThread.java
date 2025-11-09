package com.codex.backend.domain.message;

import com.codex.backend.domain.BaseEntity;
import com.codex.backend.domain.listing.Listing;
import com.codex.backend.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;

/**
 * 站内信会话：绑定某条 Listing 以及买家/卖家双方。
 */
@Entity
@Table(
        name = "message_threads",
        uniqueConstraints = @UniqueConstraint(columnNames = {"listing_id", "buyer_id", "seller_id"}))
public class MessageThread extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Column(length = 200)
    private String subject;

    @Column(nullable = false)
    private boolean archived;

    @OneToMany(mappedBy = "thread")
    @OrderBy("createdAt ASC")
    private List<Message> messages = new ArrayList<>();

    protected MessageThread() {
        // JPA only
    }

    public MessageThread(Listing listing, User seller, User buyer, String subject) {
        this.listing = listing;
        this.seller = seller;
        this.buyer = buyer;
        this.subject = subject;
        this.archived = false;
    }

    public Listing getListing() {
        return listing;
    }

    public User getSeller() {
        return seller;
    }

    public User getBuyer() {
        return buyer;
    }

    public String getSubject() {
        return subject;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public List<Message> getMessages() {
        return messages;
    }

    /**
     * 轻量触发一次更新，便于刷新 `updated_at` 时间戳。
     */
    public void touch() {
        // 与前端的会话更新时间对齐：每次有新消息都刷新 updated_at，便于列表按最新排序。
        markUpdated();
    }
}
