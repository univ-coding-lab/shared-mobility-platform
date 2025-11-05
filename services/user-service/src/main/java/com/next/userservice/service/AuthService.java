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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .isVerified(false)
                .totalRentals(0L)
                .totalDistanceKm(0.0)
                .walletBalance(0.0)
                .build();

        user = userRepository.save(user);
        log.info("User registered: {}", user.getEmail());

        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ValidationException("Invalid credentials");
        }

        if (!user.getIsActive()) {
            throw new ValidationException("Account is inactive");
        }

        user.updateLastLogin();
        userRepository.save(user);

        String token = jwtTokenProvider.createToken(user.getId(), user.getEmail());

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
}
