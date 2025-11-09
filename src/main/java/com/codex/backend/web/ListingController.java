package com.codex.backend.web;

import com.codex.backend.security.UserDetailsServiceImpl.AuthenticatedUser;
import com.codex.backend.service.ListingService;
import com.codex.backend.web.dto.CreateListingRequest;
import com.codex.backend.web.dto.ListingResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Listing 控制器：提供雪板列表查询与发布接口。
 */
@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final ListingService listingService;

    public ListingController(ListingService listingService) {
        this.listingService = listingService;
    }

    /**
     * 获取全部雪板列表，字段与前端的 SampleData 完全对齐。
     */
    @GetMapping
    public List<ListingResponse> list(@AuthenticationPrincipal AuthenticatedUser principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return listingService.fetchAll(principal.getUser());
    }

    /**
     * 发布雪板信息：自动根据 JWT 中的用户绑定 seller 信息。
     */
    @PostMapping
    public ListingResponse create(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody CreateListingRequest request) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return listingService.create(principal.getUser(), request);
    }
}
