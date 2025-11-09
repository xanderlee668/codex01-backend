package com.codex.backend.web.dto;

import java.time.Instant;

/**
 * 收藏响应：返回收藏记录与关联的 Listing。
 */
public record FavoriteResponse(String favoriteId, ListingResponse listing, Instant addedAt) {}
