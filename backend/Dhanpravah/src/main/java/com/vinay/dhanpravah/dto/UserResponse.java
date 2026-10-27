package com.vinay.dhanpravah.dto;

import com.vinay.dhanpravah.domain.AppUser;
import com.vinay.dhanpravah.domain.UserRole;
import com.vinay.dhanpravah.domain.UserStatus;

import java.time.Instant;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String mobileNumber,
        UserRole role,
        UserStatus status,
        Instant createdAt) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt());
    }
}