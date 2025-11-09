package com.codex.backend.service;

import com.codex.backend.domain.trip.Trip;
import com.codex.backend.domain.trip.TripJoinRequest;
import com.codex.backend.domain.trip.TripMessage;
import com.codex.backend.domain.trip.TripParticipant;
import com.codex.backend.domain.trip.TripParticipantRole;
import com.codex.backend.domain.trip.TripRequestStatus;
import com.codex.backend.domain.trip.TripStatus;
import com.codex.backend.domain.user.User;
import com.codex.backend.repository.TripJoinRequestRepository;
import com.codex.backend.repository.TripMessageRepository;
import com.codex.backend.repository.TripParticipantRepository;
import com.codex.backend.repository.TripRepository;
import com.codex.backend.web.dto.AuthResponse;
import com.codex.backend.web.dto.trip.CreateTripRequest;
import com.codex.backend.web.dto.trip.SendTripMessageRequest;
import com.codex.backend.web.dto.trip.TripJoinRequestCommand;
import com.codex.backend.web.dto.trip.TripResponse;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 行程业务逻辑：对应前端的行程列表、报名与群聊操作。
 */
@Service
public class TripService {

    private final TripRepository tripRepository;
    private final TripParticipantRepository participantRepository;
    private final TripJoinRequestRepository joinRequestRepository;
    private final TripMessageRepository tripMessageRepository;
    private final AuthService authService;

    public TripService(
            TripRepository tripRepository,
            TripParticipantRepository participantRepository,
            TripJoinRequestRepository joinRequestRepository,
            TripMessageRepository tripMessageRepository,
            AuthService authService) {
        this.tripRepository = tripRepository;
        this.participantRepository = participantRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.tripMessageRepository = tripMessageRepository;
        this.authService = authService;
    }

    /**
     * 查询全部行程数据，按开始时间排序。
     */
    @Transactional(readOnly = true)
    public List<TripResponse> listTrips() {
        return tripRepository.findAll().stream().sorted(Comparator.comparing(Trip::getStartAt)).map(this::toResponse).toList();
    }

    /**
     * 创建行程并自动把组织者加入参与者列表。
     */
    @Transactional
    public TripResponse createTrip(User organizer, CreateTripRequest request) {
        TripStatus status = request.status() != null ? TripStatus.fromJson(request.status()) : TripStatus.PLANNED;
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid status value");
        }
        Trip trip = new Trip(
                request.title(),
                request.destination(),
                request.description(),
                request.startAt(),
                request.endAt(),
                status,
                organizer);
        Trip saved = tripRepository.save(trip);
        TripParticipant organizerParticipant = participantRepository.save(new TripParticipant(saved, organizer, TripParticipantRole.ORGANIZER));
        saved.getParticipants().add(organizerParticipant);
        return toResponse(saved);
    }

    /**
     * 提交加入行程的请求。
     */
    @Transactional
    public TripResponse requestToJoin(User applicant, UUID tripId, TripJoinRequestCommand command) {
        Trip trip = tripRepository
                .findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (isParticipant(trip, applicant)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Already joined");
        }
        joinRequestRepository
                .findByTripAndApplicant(trip, applicant)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Request already submitted");
                });
        TripJoinRequest request = new TripJoinRequest(trip, applicant, TripRequestStatus.PENDING, command.message());
        TripJoinRequest saved = joinRequestRepository.save(request);
        trip.getJoinRequests().add(saved);
        return toResponse(trip);
    }

    /**
     * 审批加入请求，仅组织者可操作。
     */
    @Transactional
    public TripResponse approveRequest(User organizer, UUID tripId, UUID requestId) {
        Trip trip = tripRepository
                .findByIdAndOrganizer(tripId, organizer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Only organizer can approve"));
        TripJoinRequest request = joinRequestRepository
                .findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
        if (!request.getTrip().equals(trip)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Request mismatch");
        }
        if (request.getStatus() != TripRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Request already processed");
        }
        request.setStatus(TripRequestStatus.APPROVED);
        joinRequestRepository.save(request);
        TripParticipant participant = participantRepository
                .findByTripAndUser(trip, request.getApplicant())
                .orElseGet(() -> participantRepository.save(
                        new TripParticipant(trip, request.getApplicant(), TripParticipantRole.MEMBER)));
        boolean exists = trip.getParticipants().stream()
                .anyMatch(existing -> existing.getUser().equals(request.getApplicant()));
        if (!exists) {
            trip.getParticipants().add(participant);
        }
        return toResponse(trip);
    }

    /**
     * 在行程群聊中发送消息，必须是参与者或组织者。
     */
    @Transactional
    public TripResponse sendMessage(User sender, UUID tripId, SendTripMessageRequest request) {
        Trip trip = tripRepository
                .findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (!isParticipant(trip, sender)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not part of trip");
        }
        TripMessage message = tripMessageRepository.save(new TripMessage(trip, sender, request.content()));
        trip.getMessages().add(message);
        return toResponse(trip);
    }

    private boolean isParticipant(Trip trip, User user) {
        return trip.getOrganizer().equals(user)
                || participantRepository.findByTripAndUser(trip, user).isPresent();
    }

    private TripResponse toResponse(Trip trip) {
        TripResponse.TripMemberResponse organizer = toMemberResponse(trip.getOrganizer(), TripParticipantRole.ORGANIZER);
        List<TripResponse.TripMemberResponse> participants = participantRepository.findByTrip(trip).stream()
                .map(participant -> toMemberResponse(participant.getUser(), participant.getRole()))
                .toList();
        List<TripResponse.TripJoinRequestResponse> pendingRequests = trip.getJoinRequests().stream()
                .filter(request -> request.getStatus() == TripRequestStatus.PENDING)
                .map(request -> new TripResponse.TripJoinRequestResponse(
                        request.getId().toString(),
                        toMemberResponse(request.getApplicant(), TripParticipantRole.MEMBER),
                        request.getStatus().toJson(),
                        request.getMessage(),
                        request.getCreatedAt()))
                .toList();
        List<TripResponse.TripMessageResponse> messages = tripMessageRepository.findByTripOrderByCreatedAtAsc(trip).stream()
                .map(message -> new TripResponse.TripMessageResponse(
                        message.getId().toString(),
                        toMemberResponse(message.getSender(), determineRole(trip, message.getSender())),
                        message.getContent(),
                        message.getSentAt()))
                .toList();
        return new TripResponse(
                trip.getId().toString(),
                trip.getTitle(),
                trip.getDestination(),
                trip.getDescription(),
                trip.getStartAt(),
                trip.getEndAt(),
                trip.getStatus().toJson(),
                organizer,
                participants,
                pendingRequests,
                messages);
    }

    private TripResponse.TripMemberResponse toMemberResponse(User user, TripParticipantRole role) {
        AuthResponse.UserPayload payload = authService.toPayload(user);
        return new TripResponse.TripMemberResponse(
                payload.userId(),
                payload.displayName(),
                payload.location(),
                payload.rating(),
                payload.dealsCount(),
                role != null ? role.toJson() : TripParticipantRole.MEMBER.toJson());
    }

    private TripParticipantRole determineRole(Trip trip, User user) {
        if (trip.getOrganizer().equals(user)) {
            return TripParticipantRole.ORGANIZER;
        }
        return participantRepository
                .findByTripAndUser(trip, user)
                .map(TripParticipant::getRole)
                .orElse(TripParticipantRole.MEMBER);
    }
}
