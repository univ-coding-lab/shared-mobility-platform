package com.next.userservice.service;

import com.next.common.domain.enums.UserRole;
import com.next.common.domain.exception.ResourceNotFoundException;
import com.next.common.domain.exception.ValidationException;
import com.next.common.domain.model.User;
import com.next.userservice.dto.AuthResponse;
import com.next.userservice.dto.LoginRequest;
import com.next.userservice.dto.RegisterRequest;
import com.next.userservice.repository.UserRepository;
import com.next.userservice.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setPhoneNumber("+821012345678");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        user = User.builder()
                .email("test@example.com")
                .passwordHash("$2a$10$hashedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .isVerified(false)
                .build();

        user.setId("user123");
    }

    @Test
    void register_WithNewEmail_ShouldSucceed() {

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.createToken(anyString(), anyString())).thenReturn("jwt.token.here");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        assertEquals("user123", response.getUserId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("John Doe", response.getFullName());

        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(jwtTokenProvider).createToken("user123", "test@example.com");
    }

    @Test
    void register_WithExistingEmail_ShouldThrowValidationException() {

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(ValidationException.class, () -> authService.register(registerRequest));
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_WithValidCredentials_ShouldSucceed() {

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.createToken(anyString(), anyString())).thenReturn("jwt.token.here");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt.token.here", response.getToken());
        assertEquals("user123", response.getUserId());

        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password123", "$2a$10$hashedPassword");
        verify(userRepository).save(user);
    }

    @Test
    void login_WithNonExistentEmail_ShouldThrowResourceNotFoundException() {

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.login(loginRequest));
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowValidationException() {

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(ValidationException.class, () -> authService.login(loginRequest));
        verify(passwordEncoder).matches("password123", "$2a$10$hashedPassword");
    }

    @Test
    void login_WithInactiveAccount_ShouldThrowValidationException() {

        user.setIsActive(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        assertThrows(ValidationException.class, () -> authService.login(loginRequest));
    }
}
