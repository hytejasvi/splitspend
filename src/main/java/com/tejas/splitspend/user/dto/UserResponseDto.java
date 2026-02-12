package com.tejas.splitspend.user.dto;

import com.tejas.splitspend.user.User;

import java.time.ZonedDateTime;

/**
 * Response DTO for user data.
 * Never expose password or sensitive fields!
 */
public record UserResponseDto (
        Long userId,
        String name,
        String email,
        String phoneNumber,
        ZonedDateTime createdAt
) {
    public static UserResponseDto from(User user) {
        return new UserResponseDto(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getCreatedAt()
        );
    }
}