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
        return tripRepository
                .findAll()
                .stream()
                // 与前端的行程时间轴一致：按照开始时间升序排列。
                .sorted(Comparator.comparing(Trip::getStartAt))
                .map(this::toResponse)
                .toList();
    }

    /**
     * 创建行程并自动把组织者加入参与者列表。
     */
    @Transactional
    public TripResponse createTrip(User organizer, CreateTripRequest request) {
        TripStatus status;
        try {
            status = request.status() == null || request.status().isBlank()
                    ? TripStatus.PLANNED
                    : TripStatus.fromJson(request.status());
        } catch (IllegalArgumentException ex) {
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
        // 组织者默认出现在行程参与者列表中，对应前端 SampleData.organizer。
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
        // 立即回传最新行程详情，便于前端刷新待审批列表。
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
        // 返回最新行程信息，前端会将申请人移动到 participants 中展示。
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
            // 与前端规则一致：只有已加入行程的用户才能进入群聊。
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not part of trip");
        }
        TripMessage message = tripMessageRepository.save(new TripMessage(trip, sender, request.content()));
        trip.getMessages().add(message);
        // 返回最新的行程详情，客户端会实时刷新聊天窗口。
        return toResponse(trip);
    }

    private boolean isParticipant(Trip trip, User user) {
        return trip.getOrganizer().equals(user)
                || participantRepository.findByTripAndUser(trip, user).isPresent();
    }

    private TripResponse toResponse(Trip trip) {
        TripResponse.TripMemberResponse organizer = toMemberResponse(trip.getOrganizer(), TripParticipantRole.ORGANIZER);
        List<TripResponse.TripMemberResponse> participants = participantRepository.findByTrip(trip).stream()
                // iOS 端的 participants 列表不重复展示组织者，这里过滤并按加入时间排序。
                .filter(participant -> !participant.getUser().equals(trip.getOrganizer()))
                .sorted(Comparator.comparing(TripParticipant::getCreatedAt))
                .map(participant -> toMemberResponse(participant.getUser(), participant.getRole()))
                .toList();
        List<TripResponse.TripJoinRequestResponse> pendingRequests = trip.getJoinRequests().stream()
                // 前端在待处理列表中依赖申请时间排序，这里按照 created_at 升序输出。
                .filter(request -> request.getStatus() == TripRequestStatus.PENDING)
                .sorted(Comparator.comparing(TripJoinRequest::getCreatedAt))
                .map(request -> new TripResponse.TripJoinRequestResponse(
                        request.getId().toString(),
                        toMemberResponse(request.getApplicant(), TripParticipantRole.MEMBER),
                        request.getStatus().toJson(),
                        request.getMessage(),
                        request.getCreatedAt()))
                .toList();
        List<TripResponse.TripMessageResponse> messages = tripMessageRepository.findByTripOrderByCreatedAtAsc(trip).stream()
                // 与前端的群聊时间线保持升序展示。
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
