package com.tejas.splitspend.group.dto;

import com.tejas.splitspend.group.Group;

import java.util.List;

public record GroupMembersResponseDto(
        String groupName,
        List<MemberDto> members,
        int memberCount
) {
    public static GroupMembersResponseDto from(Group group){
        List<MemberDto> memberDto = group.getMembers().stream()
                .map(MemberDto::from).toList();
        return new GroupMembersResponseDto(
                group.getGroupName(),
                memberDto,
                memberDto.size());
    }
}
