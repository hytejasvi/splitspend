package com.tejas.splitspend.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateGroupDto(
        @NotBlank(message = "Group name is required")
        @Size(min = 2, max = 100, message = "Group name must be between 2 and 100 characters")
        String groupName,

        @NotNull(message = "Creator user ID is required")
        Long createdById) {
}
