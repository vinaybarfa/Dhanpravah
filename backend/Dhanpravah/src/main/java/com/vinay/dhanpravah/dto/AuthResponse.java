package com.vinay.dhanpravah.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
