package com.codex.backend.service;

import com.codex.backend.domain.user.User;
import com.codex.backend.repository.UserRepository;
import com.codex.backend.security.JwtTokenProvider;
import com.codex.backend.security.UserDetailsServiceImpl.AuthenticatedUser;
import com.codex.backend.web.dto.AuthResponse;
import com.codex.backend.web.dto.LoginRequest;
import com.codex.backend.web.dto.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        User user = new User(request.email(), passwordEncoder.encode(request.password()), request.displayName());
        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(saved.getId(), saved.getEmail());
        return new AuthResponse(token, new AuthResponse.UserSummary(saved.getId(), saved.getEmail(), saved.getDisplayName()));
    }

    public AuthResponse authenticate(LoginRequest request) {
        System.out.println("🧩 尝试登录邮箱: " + request.email());
        System.out.println("🧩 尝试登录密码: " + request.password());

        // 打印数据库中是否存在该用户，以及密码匹配结果
        userRepository.findByEmail(request.email())
                .ifPresentOrElse(
                        u -> {
                            System.out.println("🧩 数据库哈希: " + u.getPasswordHash());
                            System.out.println("🧩 匹配结果: " + passwordEncoder.matches(request.password(), u.getPasswordHash()));
                        },
                        () -> System.out.println("🧩 用户不存在！")
                );

        // 正式认证逻辑
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        User user = authenticatedUser.getUser();
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, new AuthResponse.UserSummary(user.getId(), user.getEmail(), user.getDisplayName()));
    }


    public User requireUser(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
