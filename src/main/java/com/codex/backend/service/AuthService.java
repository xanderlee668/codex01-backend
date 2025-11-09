package com.codex.backend.service;

import com.codex.backend.domain.user.User;
import com.codex.backend.repository.UserRepository;
import com.codex.backend.security.JwtTokenProvider;
import com.codex.backend.security.UserDetailsServiceImpl.AuthenticatedUser;
import com.codex.backend.web.dto.AuthResponse;
import com.codex.backend.web.dto.LoginRequest;
import com.codex.backend.web.dto.RegisterRequest;
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
 * 登录/注册业务逻辑，封装用户校验、密码加密、JWT 生成等流程。
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
     * 注册用户并立即生成访问令牌。
     */
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

    /**
     * 校验登录凭证并返回访问令牌。
     */
    public AuthResponse authenticate(LoginRequest request) {
        log.debug("Attempting authentication for email: {}", request.email());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        User user = authenticatedUser.getUser();
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, new AuthResponse.UserSummary(user.getId(), user.getEmail(), user.getDisplayName()));
    }

}
