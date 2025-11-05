package com.next.userservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        String secret = "test-secret-key-for-jwt-token-generation-must-be-at-least-256-bits-long";
        long expiration = 3600000; // 1 hour
        jwtTokenProvider = new JwtTokenProvider(secret, expiration);
    }

    @Test
    void createToken_ShouldGenerateValidToken() {
        // given
        String userId = "user123";
        String email = "test@example.com";

        // when
        String token = jwtTokenProvider.createToken(userId, email);

        // then
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void getUserId_ShouldExtractUserIdFromToken() {
        // given
        String userId = "user123";
        String email = "test@example.com";
        String token = jwtTokenProvider.createToken(userId, email);

        // when
        String extractedUserId = jwtTokenProvider.getUserId(token);

        // then
        assertEquals(userId, extractedUserId);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // given
        String userId = "user123";
        String email = "test@example.com";
        String token = jwtTokenProvider.createToken(userId, email);

        // when
        boolean isValid = jwtTokenProvider.validateToken(token);

        // then
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // given
        String invalidToken = "invalid.token.here";

        // when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // given
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider(
            "test-secret-key-for-jwt-token-generation-must-be-at-least-256-bits-long",
            1 // 1ms expiration
        );
        String token = shortExpirationProvider.createToken("user123", "test@example.com");

        // when
        try {
            Thread.sleep(10); // Wait for token to expire
        } catch (InterruptedException e) {
            fail("Thread sleep interrupted");
        }
        boolean isValid = shortExpirationProvider.validateToken(token);

        // then
        assertFalse(isValid);
    }
}
