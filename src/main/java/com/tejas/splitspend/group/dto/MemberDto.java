package com.tejas.splitspend.group.dto;

import com.tejas.splitspend.group.GroupMember;
import com.tejas.splitspend.group.MemberRole;

import java.time.ZonedDateTime;

/*
 * DTO representing a group member's basic info.
 */

public record MemberDto(
        Long userId,
        String name,
        String email,
        MemberRole role,
        ZonedDateTime joinedAt
) {
    public static MemberDto from(GroupMember member) {
        return new MemberDto(
                member.getUser().getUserId(),
                member.getUser().getName(),
                member.getUser().getEmail(),
                member.getRole(),
                member.getCreatedAt()
        );
    }
}
