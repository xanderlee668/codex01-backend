package com.codex.backend.web.trips;

import com.codex.backend.security.UserDetailsServiceImpl.AuthenticatedUser;
import com.codex.backend.service.TripService;
import com.codex.backend.web.dto.trip.CreateTripRequest;
import com.codex.backend.web.dto.trip.SendTripMessageRequest;
import com.codex.backend.web.dto.trip.TripJoinRequestCommand;
import com.codex.backend.web.dto.trip.TripResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 行程模块 API：提供列表、报名与群聊能力。
 */
@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping
    public List<TripResponse> list(@AuthenticationPrincipal AuthenticatedUser principal) {
        ensureLogin(principal);
        return tripService.listTrips();
    }

    @PostMapping
    public TripResponse create(
            @AuthenticationPrincipal AuthenticatedUser principal, @Valid @RequestBody CreateTripRequest request) {
        ensureLogin(principal);
        return tripService.createTrip(principal.getUser(), request);
    }

    @PostMapping("/{tripId}/requests")
    public TripResponse requestJoin(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String tripId,
            @Valid @RequestBody TripJoinRequestCommand command) {
        ensureLogin(principal);
        return tripService.requestToJoin(principal.getUser(), parseUuid(tripId, "trip"), command);
    }

    @PostMapping("/{tripId}/requests/{requestId}/approve")
    public TripResponse approve(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String tripId,
            @PathVariable String requestId) {
        ensureLogin(principal);
        return tripService.approveRequest(
                principal.getUser(), parseUuid(tripId, "trip"), parseUuid(requestId, "request"));
    }

    @PostMapping("/{tripId}/messages")
    public TripResponse sendMessage(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String tripId,
            @Valid @RequestBody SendTripMessageRequest request) {
        ensureLogin(principal);
        return tripService.sendMessage(principal.getUser(), parseUuid(tripId, "trip"), request);
    }

    private void ensureLogin(AuthenticatedUser principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    private UUID parseUuid(String value, String field) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid " + field + " id");
        }
    }
}
