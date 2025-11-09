package com.codex.backend.service;

import com.codex.backend.domain.listing.Listing;
import com.codex.backend.domain.listing.ListingCondition;
import com.codex.backend.domain.listing.TradeOption;
import com.codex.backend.domain.user.User;
import com.codex.backend.repository.FavoriteRepository;
import com.codex.backend.repository.ListingRepository;
import com.codex.backend.web.dto.AuthResponse;
import com.codex.backend.web.dto.CreateListingRequest;
import com.codex.backend.web.dto.ListingResponse;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Listing 业务逻辑：负责雪板列表查询与创建。
 */
@Service
public class ListingService {

    private final ListingRepository listingRepository;
    private final FavoriteRepository favoriteRepository;
    private final AuthService authService;

    public ListingService(
            ListingRepository listingRepository, FavoriteRepository favoriteRepository, AuthService authService) {
        this.listingRepository = listingRepository;
        this.favoriteRepository = favoriteRepository;
        this.authService = authService;
    }

    /**
     * 返回当前全部雪板 Listing，供 iOS 客户端刷新列表。
     */
    @Transactional(readOnly = true)
    public List<ListingResponse> fetchAll(User user) {
        // iOS 端会把最新发布的放在顶部，这里按 created_at 倒序返回。
        List<Listing> listings = listingRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        if (user == null) {
            return listings.stream().map(listing -> toResponse(listing, listing.isFavorite())).toList();
        }
        Set<UUID> favoriteIds = favoriteRepository
                .findByUserAndArchivedFalseOrderByCreatedAtDesc(user)
                .stream()
                // 前端需要根据 is_favorite 渲染收藏状态，这里预计算当前用户的收藏集合。
                .map(favorite -> favorite.getListing().getId())
                .collect(Collectors.toSet());
        return listings.stream()
                .map(listing -> toResponse(
                        listing, favoriteIds.contains(listing.getId()) || listing.isFavorite()))
                .toList();
    }

    /**
     * 创建新的雪板 Listing，自动绑定当前登录用户为卖家。
     */
    @Transactional
    public ListingResponse create(User seller, CreateListingRequest request) {
        // CreateListingRequest 对应前端发布表单字段，保持逐一映射。
        Listing listing = new Listing(
                request.title(),
                request.description(),
                parseCondition(request.condition()),
                request.price(),
                request.location(),
                parseTradeOption(request.tradeOption()),
                request.isFavorite(),
                request.imageUrl(),
                seller);
        Listing saved = listingRepository.save(listing);
        return toResponse(saved, request.isFavorite());
    }

    public ListingResponse toResponse(Listing listing, boolean favorite) {
        AuthResponse.UserPayload sellerPayload = authService.toPayload(listing.getSeller());
        // Swift 端直接映射该结构到 ListingViewModel，因此字段名/含义保持 1:1。
        return new ListingResponse(
                toStringId(listing.getId()),
                listing.getTitle(),
                listing.getDescription(),
                listing.getCondition().toJson(),
                listing.getPrice(),
                listing.getLocation(),
                listing.getTradeOption().toJson(),
                favorite,
                listing.getImageUrl(),
                new ListingResponse.SellerResponse(
                        sellerPayload.userId(),
                        sellerPayload.displayName(),
                        sellerPayload.rating(),
                        sellerPayload.dealsCount()));
    }

    public boolean isFavoriteForUser(Listing listing, User user) {
        if (user == null) {
            return listing.isFavorite();
        }
        return favoriteRepository
                .findByUserAndListing(user, listing)
                .filter(favorite -> !favorite.isArchived())
                .isPresent();
    }

    private ListingCondition parseCondition(String value) {
        try {
            ListingCondition condition = ListingCondition.fromJson(value);
            if (condition == null) {
                throw new IllegalArgumentException();
            }
            return condition;
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid condition value");
        }
    }

    private TradeOption parseTradeOption(String value) {
        try {
            TradeOption option = TradeOption.fromJson(value);
            if (option == null) {
                throw new IllegalArgumentException();
            }
            return option;
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid trade_option value");
        }
    }

    private String toStringId(UUID id) {
        return id != null ? id.toString() : null;
    }
}
