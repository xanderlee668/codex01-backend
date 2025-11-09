package com.codex.backend.web.messages;

import com.codex.backend.security.UserDetailsServiceImpl.AuthenticatedUser;
import com.codex.backend.service.MessageService;
import com.codex.backend.web.dto.message.CreateMessageThreadRequest;
import com.codex.backend.web.dto.message.MessageThreadResponse;
import com.codex.backend.web.dto.message.SendMessageRequest;
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
 * 站内信控制器：对接 iOS `APIClient` 中的消息相关方法。
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public List<MessageThreadResponse> list(@AuthenticationPrincipal AuthenticatedUser principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return messageService.listThreads(principal.getUser());
    }

    @GetMapping("/{threadId}")
    public MessageThreadResponse detail(
            @AuthenticationPrincipal AuthenticatedUser principal, @PathVariable String threadId) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return messageService.getThread(principal.getUser(), parseUuid(threadId, "thread"));
    }

    @PostMapping
    public MessageThreadResponse create(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody CreateMessageThreadRequest request) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return messageService.createThread(principal.getUser(), request);
    }

    @PostMapping("/{threadId}/messages")
    public MessageThreadResponse send(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable String threadId,
            @Valid @RequestBody SendMessageRequest request) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return messageService.sendMessage(principal.getUser(), parseUuid(threadId, "thread"), request);
    }

    private UUID parseUuid(String value, String field) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid " + field + " id");
        }
    }
}
