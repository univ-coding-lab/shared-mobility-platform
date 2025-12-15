package com.next.common.domain.model;

import com.next.common.domain.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank(message = "First name is required")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @NotNull(message = "User role is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "membership_tier")
    private String membershipTier;

    @Column(name = "total_rentals")
    private Long totalRentals = 0L;

    @Column(name = "total_distance_km")
    private Double totalDistanceKm = 0.0;

    @Column(name = "wallet_balance")
    private Double walletBalance = 0.0;

    @Column(name = "last_login_at")
    private java.time.LocalDateTime lastLoginAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean canRent() {
        return isActive && isVerified && walletBalance >= 0;
    }

    public void updateLastLogin() {
        this.lastLoginAt = java.time.LocalDateTime.now();
    }
}
