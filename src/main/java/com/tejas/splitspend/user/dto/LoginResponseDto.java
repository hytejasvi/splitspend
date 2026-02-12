package com.tejas.splitspend.user.dto;

import com.tejas.splitspend.user.User;

import java.time.ZonedDateTime;

/*
 * Response DTO for successful login.
 * Returns user details (no password).

 * TODO Phase 3: Add JWT token field.
 */
public record LoginResponseDto(
        Long userId,
        String name,
        String email,
        String phoneNumber,
        ZonedDateTime createdAt) {

    public static LoginResponseDto From(User user) {
        return new LoginResponseDto(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getCreatedAt()
        );
    }
}
