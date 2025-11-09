package com.codex.backend.domain.listing;

import com.codex.backend.domain.BaseEntity;
import com.codex.backend.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Listing 实体：对应前端的雪板发布信息。
 */
@Entity
@Table(name = "listings")
public class Listing extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ListingCondition condition;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 120)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TradeOption tradeOption;

    @Column(nullable = false)
    private boolean favorite;

    @Column(length = 500)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    protected Listing() {
        // JPA only
    }

    public Listing(
            String title,
            String description,
            ListingCondition condition,
            BigDecimal price,
            String location,
            TradeOption tradeOption,
            boolean favorite,
            String imageUrl,
            User seller) {
        this.title = title;
        this.description = description;
        this.condition = condition;
        this.price = price;
        this.location = location;
        this.tradeOption = tradeOption;
        this.favorite = favorite;
        this.imageUrl = imageUrl;
        this.seller = seller;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ListingCondition getCondition() {
        return condition;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getLocation() {
        return location;
    }

    public TradeOption getTradeOption() {
        return tradeOption;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public User getSeller() {
        return seller;
    }
}
