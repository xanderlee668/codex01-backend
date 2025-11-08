package com.codex.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.codex.backend.config.JwtProperties;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setIssuer("test-issuer");
        properties.setSecret("0123456789ABCDEF0123456789ABCDEF");
        properties.setExpiration(Duration.ofMinutes(30).toString());
        tokenProvider = new JwtTokenProvider(properties);
    }

    @Test
    void generateAndValidateToken() {
        String token = tokenProvider.generateToken(1L, "user@example.com");
        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.extractUserId(token)).isEqualTo(1L);
    }
}
