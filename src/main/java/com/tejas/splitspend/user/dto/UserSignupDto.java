package com.tejas.splitspend.user.dto;

import jakarta.validation.constraints.*;

public record UserSignupDto(
        @NotBlank(message = "Name cannot be blank")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[0-9]+$", message = "Phone number must contain only digits")
        @Size(min = 10, max = 10, message = "Phone number must be 10 digits")
        String phoneNumber,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 15, message = "Password must be between 8 and 15 characters")
        String password)
{ }