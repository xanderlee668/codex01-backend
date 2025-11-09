package com.codex.backend.service;

import com.codex.backend.domain.listing.Listing;
import com.codex.backend.domain.listing.ListingCondition;
import com.codex.backend.domain.listing.TradeOption;
import com.codex.backend.domain.user.User;
import com.codex.backend.repository.ListingRepository;
import com.codex.backend.web.dto.AuthResponse;
import com.codex.backend.web.dto.CreateListingRequest;
import com.codex.backend.web.dto.ListingResponse;
import java.util.List;
import java.util.UUID;
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
    private final AuthService authService;

    public ListingService(ListingRepository listingRepository, AuthService authService) {
        this.listingRepository = listingRepository;
        this.authService = authService;
    }

    /**
     * 返回当前全部雪板 Listing，供 iOS 客户端刷新列表。
     */
    @Transactional(readOnly = true)
    public List<ListingResponse> fetchAll() {
        return listingRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * 创建新的雪板 Listing，自动绑定当前登录用户为卖家。
     */
    @Transactional
    public ListingResponse create(User seller, CreateListingRequest request) {
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
        return toResponse(saved);
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

    private ListingResponse toResponse(Listing listing) {
        AuthResponse.UserPayload sellerPayload = authService.toPayload(listing.getSeller());
        return new ListingResponse(
                toStringId(listing.getId()),
                listing.getTitle(),
                listing.getDescription(),
                listing.getCondition().toJson(),
                listing.getPrice(),
                listing.getLocation(),
                listing.getTradeOption().toJson(),
                listing.isFavorite(),
                listing.getImageUrl(),
                new ListingResponse.SellerResponse(
                        sellerPayload.userId(),
                        sellerPayload.displayName(),
                        sellerPayload.rating(),
                        sellerPayload.dealsCount()));
    }

    private String toStringId(UUID id) {
        return id != null ? id.toString() : null;
    }
}
