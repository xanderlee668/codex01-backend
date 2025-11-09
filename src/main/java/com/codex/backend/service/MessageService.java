package com.codex.backend.service;

import com.codex.backend.domain.listing.Listing;
import com.codex.backend.domain.message.Message;
import com.codex.backend.domain.message.MessageThread;
import com.codex.backend.domain.user.User;
import com.codex.backend.repository.ListingRepository;
import com.codex.backend.repository.MessageRepository;
import com.codex.backend.repository.MessageThreadRepository;
import com.codex.backend.web.dto.AuthResponse;
import com.codex.backend.web.dto.ListingResponse;
import com.codex.backend.web.dto.message.CreateMessageThreadRequest;
import com.codex.backend.web.dto.message.MessageThreadResponse;
import com.codex.backend.web.dto.message.SendMessageRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 站内信业务逻辑：支持会话查询、新建以及发送消息。
 */
@Service
public class MessageService {

    private final MessageThreadRepository messageThreadRepository;
    private final MessageRepository messageRepository;
    private final ListingRepository listingRepository;
    private final ListingService listingService;
    private final AuthService authService;

    public MessageService(
            MessageThreadRepository messageThreadRepository,
            MessageRepository messageRepository,
            ListingRepository listingRepository,
            ListingService listingService,
            AuthService authService) {
        this.messageThreadRepository = messageThreadRepository;
        this.messageRepository = messageRepository;
        this.listingRepository = listingRepository;
        this.listingService = listingService;
        this.authService = authService;
    }

    /**
     * 查询当前用户参与的所有会话。
     */
    @Transactional(readOnly = true)
    public List<MessageThreadResponse> listThreads(User user) {
        return messageThreadRepository
                .findByBuyerOrSellerOrderByUpdatedAtDesc(user, user)
                .stream()
                .map(thread -> toResponse(thread, user))
                .toList();
    }

    /**
     * 根据 ID 获取具体会话。
     */
    @Transactional(readOnly = true)
    public MessageThreadResponse getThread(User user, UUID threadId) {
        MessageThread thread = messageThreadRepository
                .findByIdAndBuyerOrSeller(threadId, user, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found"));
        return toResponse(thread, user);
    }

    /**
     * 新建与卖家的会话并发送第一条消息。
     */
    @Transactional
    public MessageThreadResponse createThread(User buyer, CreateMessageThreadRequest request) {
        UUID listingId;
        try {
            listingId = UUID.fromString(request.listingId());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid listing id");
        }
        Listing listing = listingRepository
                .findById(listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
        if (listing.getSeller().equals(buyer)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Cannot message yourself");
        }
        MessageThread thread = messageThreadRepository
                .findByBuyerOrSellerOrderByUpdatedAtDesc(buyer, buyer)
                .stream()
                .filter(existing -> existing.getListing().equals(listing) && existing.getBuyer().equals(buyer))
                .findFirst()
                .orElseGet(() -> messageThreadRepository.save(new MessageThread(
                        listing,
                        listing.getSeller(),
                        buyer,
                        listing.getTitle())));
        Message message = messageRepository.save(new Message(thread, buyer, request.message()));
        thread.getMessages().add(message);
        thread.touch();
        return toResponse(thread, buyer);
    }

    /**
     * 在指定线程发送消息。
     */
    @Transactional
    public MessageThreadResponse sendMessage(User sender, UUID threadId, SendMessageRequest request) {
        MessageThread thread = messageThreadRepository
                .findByIdAndBuyerOrSeller(threadId, sender, sender)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found"));
        Message saved = messageRepository.save(new Message(thread, sender, request.content()));
        thread.getMessages().add(saved);
        thread.touch();
        return toResponse(thread, sender);
    }

    private MessageThreadResponse toResponse(MessageThread thread, User currentUser) {
        ListingResponse listing = listingService.toResponse(
                thread.getListing(),
                listingService.isFavoriteForUser(thread.getListing(), currentUser));
        MessageThreadResponse.ParticipantResponse buyer = toParticipant(thread.getBuyer());
        MessageThreadResponse.ParticipantResponse seller = toParticipant(thread.getSeller());
        List<MessageThreadResponse.MessageResponse> messages = thread.getMessages().stream()
                .map(message -> new MessageThreadResponse.MessageResponse(
                        message.getId().toString(),
                        message.getSender().getId().toString(),
                        message.getContent(),
                        message.getSentAt()))
                .toList();
        return new MessageThreadResponse(
                thread.getId().toString(),
                listing,
                buyer,
                seller,
                messages,
                0,
                thread.isArchived(),
                thread.getUpdatedAt());
    }

    private MessageThreadResponse.ParticipantResponse toParticipant(User user) {
        AuthResponse.UserPayload payload = authService.toPayload(user);
        return new MessageThreadResponse.ParticipantResponse(
                payload.userId(), payload.displayName(), payload.rating(), payload.dealsCount());
    }
}
