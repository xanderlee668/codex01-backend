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

/**
 * 用户个人资料接口，负责获取与更新当前登录用户的展示名。
 */
@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取当前用户资料。
     */
    @GetMapping
    public UserProfileResponse getProfile(@AuthenticationPrincipal AuthenticatedUser principal) {
        User user = principal.getUser();
        return userService.getProfile(user);
    }

    /**
     * 更新当前用户资料，目前支持修改展示名。
     */
    @PutMapping
    public UserProfileResponse updateProfile(
            @AuthenticationPrincipal AuthenticatedUser principal, @Valid @RequestBody UpdateProfileRequest request) {
        User user = principal.getUser();
        return userService.updateProfile(user, request);
    }
}
