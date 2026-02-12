package com.tejas.splitspend.group.dto;

import jakarta.validation.constraints.NotNull;

public record AddGroupMembersDto(
        @NotNull(message = "Group ID is required")
        Long groupId,

        @NotNull(message = "User ID is required")
        Long userId) {
}
