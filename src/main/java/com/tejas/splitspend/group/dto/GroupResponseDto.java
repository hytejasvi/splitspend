package com.tejas.splitspend.group.dto;

import com.tejas.splitspend.group.Group;

import java.time.ZonedDateTime;

public record GroupResponseDto(
        Long groupId,
        String groupName,
        Long createdById,
        ZonedDateTime createdAt,
        int memberCount
) {
    public static GroupResponseDto from(Group group) {
        return new GroupResponseDto(
                group.getGroupId(),
                group.getGroupName(),
                group.getCreatedById(),
                group.getCreatedAt(),
                group.getMembers().size());
    }
}
