package com.codex.backend.service;

import com.codex.backend.domain.favorite.Favorite;
import com.codex.backend.domain.listing.Listing;
import com.codex.backend.domain.user.User;
import com.codex.backend.repository.FavoriteRepository;
import com.codex.backend.repository.ListingRepository;
import com.codex.backend.web.dto.FavoriteResponse;
import com.codex.backend.web.dto.ListingResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 收藏业务逻辑：支持查询、添加与取消收藏。
 */
@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ListingRepository listingRepository;
    private final ListingService listingService;

    public FavoriteService(
            FavoriteRepository favoriteRepository,
            ListingRepository listingRepository,
            ListingService listingService) {
        this.favoriteRepository = favoriteRepository;
        this.listingRepository = listingRepository;
        this.listingService = listingService;
    }

    /**
     * 查询当前用户所有收藏，保持与前端 `SampleData.favoriteListings` 一致的顺序（按收藏时间倒序）。
     */
    @Transactional(readOnly = true)
    public List<FavoriteResponse> list(User user) {
        return favoriteRepository
                .findByUserAndArchivedFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 添加收藏，如已存在则直接返回。
     */
    @Transactional
    public FavoriteResponse add(User user, UUID listingId) {
        Listing listing = listingRepository
                .findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        Favorite favorite = favoriteRepository
                .findByUserAndListing(user, listing)
                .orElseGet(() -> favoriteRepository.save(new Favorite(user, listing)));
        favorite.setArchived(false);
        return toResponse(favorite);
    }

    /**
     * 取消收藏。
     */
    @Transactional
    public void remove(User user, UUID listingId) {
        Listing listing = listingRepository
                .findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        favoriteRepository
                .findByUserAndListing(user, listing)
                .ifPresent(favoriteRepository::delete);
    }

    private FavoriteResponse toResponse(Favorite favorite) {
        Listing listing = favorite.getListing();
        ListingResponse listingResponse = listingService.toResponse(listing, true);
        return new FavoriteResponse(
                favorite.getId().toString(),
                listingResponse,
                favorite.getCreatedAt());
    }
}
