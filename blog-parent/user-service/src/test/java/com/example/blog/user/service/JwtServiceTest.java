package com.example.blog.user.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "test-secret-key-that-is-at-least-256-bits-long-for-hs256!!",
                900_000L,
                604_800_000L
        );
    }

    @Test
    void shouldGenerateAndParseToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "blogger");

        Claims claims = jwtService.parseToken(token);
        assertNotNull(claims);
        assertEquals(userId, jwtService.getUserId(claims));
        assertEquals("blogger", jwtService.getRole(claims));
    }

    @Test
    void shouldReturnNullForExpiredToken() throws InterruptedException {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId, "user", Duration.ofMillis(1));

        Thread.sleep(5);

        Claims claims = jwtService.parseToken(token);
        assertNull(claims);
    }

    @Test
    void shouldReturnNullForInvalidToken() {
        Claims claims = jwtService.parseToken("invalid.token.here");
        assertNull(claims);
    }

    @Test
    void shouldReturnNullForNullClaims() {
        assertNull(jwtService.getUserId(null));
        assertNull(jwtService.getRole(null));
    }

    @Test
    void shouldGenerateRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken();
        assertNotNull(refreshToken);
        assertDoesNotThrow(() -> UUID.fromString(refreshToken));
    }
}
