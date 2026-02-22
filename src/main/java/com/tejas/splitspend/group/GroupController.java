package com.tejas.splitspend.group;

import com.tejas.splitspend.group.dto.AddGroupMembersDto;
import com.tejas.splitspend.group.dto.CreateGroupDto;
import com.tejas.splitspend.group.dto.GroupMembersResponseDto;
import com.tejas.splitspend.group.dto.GroupResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/groups")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * Create a new group.
     */
    @PostMapping("/create")
    public ResponseEntity<GroupResponseDto> createGroup(
            @Valid @RequestBody CreateGroupDto createDto) {

        Group group = groupService.createGroup(createDto);
        GroupResponseDto response = GroupResponseDto.from(group);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all groups that a user belongs to.
     *
     * @param userId User ID (query parameter)
     * @return List of user's groups
     */
    @GetMapping
    public ResponseEntity<List<GroupResponseDto>> getUserGroups(@RequestParam Long userId) {
        List<Group> groups = groupService.getUserGroups(userId);
        List<GroupResponseDto> response = groups.stream()
                .map(GroupResponseDto::from)
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Add a member to a group.
     */
    @PostMapping("/member/add")
    public ResponseEntity<GroupMembersResponseDto> addMember(
            @Valid @RequestBody AddGroupMembersDto addMemberDto) {

        Group group = groupService.addMember(addMemberDto);
        GroupMembersResponseDto response = GroupMembersResponseDto.from(group);

        return ResponseEntity.ok(response);
    }
}
