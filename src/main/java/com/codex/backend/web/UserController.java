package com.codex.backend.web;

import com.codex.backend.domain.user.User;
import com.codex.backend.security.UserDetailsServiceImpl.AuthenticatedUser;
import com.codex.backend.service.UserService;
import com.codex.backend.web.dto.UpdateProfileRequest;
import com.codex.backend.web.dto.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserProfileResponse getProfile(@AuthenticationPrincipal AuthenticatedUser principal) {
        User user = principal.getUser();
        return userService.getProfile(user);
    }

    @PutMapping
    public UserProfileResponse updateProfile(
            @AuthenticationPrincipal AuthenticatedUser principal, @Valid @RequestBody UpdateProfileRequest request) {
        User user = principal.getUser();
        return userService.updateProfile(user, request);
    }
}
