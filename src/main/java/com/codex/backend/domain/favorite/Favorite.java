package com.codex.backend.domain.favorite;

import com.codex.backend.domain.BaseEntity;
import com.codex.backend.domain.listing.Listing;
import com.codex.backend.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 收藏实体：关联用户与 Listing，标记其已收藏。
 */
@Entity
@Table(name = "favorites", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "listing_id"}))
public class Favorite extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(nullable = false)
    private boolean archived;

    protected Favorite() {
        // JPA only
    }

    public Favorite(User user, Listing listing) {
        this(user, listing, false);
    }

    public Favorite(User user, Listing listing, boolean archived) {
        this.user = user;
        this.listing = listing;
        this.archived = archived;
    }

    public User getUser() {
        return user;
    }

    public Listing getListing() {
        return listing;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
