package com.codex.backend.service;

import com.codex.backend.domain.user.User;
import com.codex.backend.repository.UserRepository;
import com.codex.backend.security.JwtTokenProvider;
import com.codex.backend.security.UserDetailsServiceImpl.AuthenticatedUser;
import com.codex.backend.web.dto.AuthResponse;
import com.codex.backend.web.dto.LoginRequest;
import com.codex.backend.web.dto.RegisterRequest;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 鉴权业务逻辑：负责处理注册、登录与用户信息映射。
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

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

    /**
     * 注册用户并立即生成访问令牌，字段与 iOS 端要求完全一致。
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.displayName(),
                "",
                "",
                0.0,
                0);
        User saved = userRepository.save(user);
        String token = jwtTokenProvider.generateToken(saved.getId(), saved.getEmail());
        return new AuthResponse(token, toPayload(saved));
    }

    /**
     * 登录校验，成功后返回 JWT + 用户信息。
     */
    public AuthResponse authenticate(LoginRequest request) {
        log.debug("Attempting authentication for email: {}", request.email());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        User user = authenticatedUser.getUser();
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, toPayload(user));
    }

    /**
     * 将用户实体转换为对接 iOS 的响应结构。
     */
    public AuthResponse.UserPayload toPayload(User user) {
        UUID id = user.getId();
        return new AuthResponse.UserPayload(
                id != null ? id.toString() : null,
                user.getEmail(),
                user.getDisplayName(),
                user.getLocation() != null ? user.getLocation() : "",
                user.getBio() != null ? user.getBio() : "",
                user.getRating(),
                user.getDealsCount());
    }
}
