package com.next.userservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        String secret = "test-secret-key-for-jwt-token-generation-must-be-at-least-256-bits-long";
        long expiration = 3600000;
        jwtTokenProvider = new JwtTokenProvider(secret, expiration);
    }

    @Test
    void createToken_ShouldGenerateValidToken() {

        String userId = "user123";
        String email = "test@example.com";

        String token = jwtTokenProvider.createToken(userId, email);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void getUserId_ShouldExtractUserIdFromToken() {

        String userId = "user123";
        String email = "test@example.com";
        String token = jwtTokenProvider.createToken(userId, email);

        String extractedUserId = jwtTokenProvider.getUserId(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {

        String userId = "user123";
        String email = "test@example.com";
        String token = jwtTokenProvider.createToken(userId, email);

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {

        String invalidToken = "invalid.token.here";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {

        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider(
            "test-secret-key-for-jwt-token-generation-must-be-at-least-256-bits-long",
            1
        );
        String token = shortExpirationProvider.createToken("user123", "test@example.com");

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            fail("Thread sleep interrupted");
        }
        boolean isValid = shortExpirationProvider.validateToken(token);

        assertFalse(isValid);
    }
}
