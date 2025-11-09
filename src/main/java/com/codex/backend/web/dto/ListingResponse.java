package com.codex.backend.web.dto;

import java.math.BigDecimal;

/**
 * 雪板 Listing 的响应结构，完全对齐 iOS 端字段。
 */
public record ListingResponse(
        String listingId,
        String title,
        String description,
        String condition,
        BigDecimal price,
        String location,
        String tradeOption,
        boolean isFavorite,
        String imageUrl,
        SellerResponse seller) {

    /**
     * 内嵌卖家信息节点，供前端展示昵称、评分和成交次数。
     */
    public record SellerResponse(
            String sellerId,
            String displayName,
            Double rating,
            Integer dealsCount) {
    }
}
