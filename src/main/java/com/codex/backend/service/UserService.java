package com.codex.backend.service;

import com.codex.backend.domain.user.User;
import com.codex.backend.repository.UserRepository;
import com.codex.backend.web.dto.UpdateProfileRequest;
import com.codex.backend.web.dto.UserProfileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(User user) {
        User persisted = findUser(user);
        return toResponse(persisted);
    }

    @Transactional
    public UserProfileResponse updateProfile(User user, UpdateProfileRequest request) {
        User persisted = findUser(user);
        persisted.setDisplayName(request.displayName());
        User saved = userRepository.save(persisted);
        return toResponse(saved);
    }

    private User findUser(User user) {
        return userRepository
                .findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getCreatedAt());
    }
}
