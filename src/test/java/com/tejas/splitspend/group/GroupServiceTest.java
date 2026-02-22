package com.tejas.splitspend.group;

import com.tejas.splitspend.group.dto.AddGroupMembersDto;
import com.tejas.splitspend.group.dto.CreateGroupDto;
import com.tejas.splitspend.group.exceptions.GroupMemberDuplicateException;
import com.tejas.splitspend.group.exceptions.GroupNotFoundException;
import com.tejas.splitspend.user.User;
import com.tejas.splitspend.user.UserRepository;
import com.tejas.splitspend.user.exceptions.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.tejas.splitspend.common.fixtures.getValidUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GroupService groupService;

    @Test
    void createGroup_Success() {
        User creator = getValidUser();
        CreateGroupDto dto = new CreateGroupDto("Goa Trip", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(groupRepository.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));

        Group result = groupService.createGroup(dto);

        assertNotNull(result);
        assertEquals("Goa Trip", result.getGroupName());
        assertEquals(1, result.getMembers().size());
        assertEquals(MemberRole.ADMIN, result.getMembers().get(0).getRole());
    }

    @Test
    void createGroup_UserNotFound() {
        CreateGroupDto dto = new CreateGroupDto("Goa Trip", 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        UserNotFoundException expectedException = assertThrows(
                UserNotFoundException.class, () -> groupService.createGroup(dto));

        assertEquals("User with ID 1 not found", expectedException.getMessage());
    }

    @Test
    void getUserGroups_ReturnsEmptyList_WhenNoGroups() {
        when(userRepository.existsById(99L)).thenReturn(true);
        when(groupMemberRepository.findByUser_UserId(99L)).thenReturn(Collections.emptyList());

        List<Group> result = groupService.getUserGroups(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllGroups_ReturnsUserGroups() {
        User user = getValidUser();
        user.setUserId(1L);
        Group group1 = new Group("Trip1", 1L);
        Group group2 = new Group("Trip2", 2L);

        GroupMember member1 = new GroupMember(group1, user, MemberRole.ADMIN);
        GroupMember member2 = new GroupMember(group2, user, MemberRole.MEMBER);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(groupMemberRepository.findByUser_UserId(1L))
                .thenReturn(List.of(member1, member2));

        List<Group> result = groupService.getUserGroups(1L);
        assertEquals(2, result.size());
    }

    @Test
    void addMember_Success() {
        User user = getValidUser();
        Group group = new Group("Trip1", 1L);

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(2L)).thenReturn(Optional.of(group));
        when(groupRepository.save(any(Group.class))).thenAnswer(i -> i.getArgument(0));

        AddGroupMembersDto dto = new AddGroupMembersDto(2L, 3L);
        Group result = groupService.addMember(dto);

        assertEquals(1, result.getMembers().size());
        assertEquals(MemberRole.MEMBER, result.getMembers().get(0).getRole());
    }

    @Test
    void addMember_ThrowsException_WhenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        AddGroupMembersDto dto = new AddGroupMembersDto(1L, 999L);

        assertThrows(UserNotFoundException.class, () -> groupService.addMember(dto));
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void addMember_ThrowsException_WhenGroupNotFound() {
        when(userRepository.findById(3L)).thenReturn(Optional.of(getValidUser()));
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        AddGroupMembersDto dto = new AddGroupMembersDto(999L, 3L);

        assertThrows(GroupNotFoundException.class, () -> groupService.addMember(dto));
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void addMember_ThrowsException_WhenUserAlreadyMember() {
        User user = getValidUser();
        user.setUserId(2L);
        Group group = new Group("Trip1", 1L);
        GroupMember existingMember = new GroupMember(group, user, MemberRole.MEMBER);
        group.getMembers().add(existingMember);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        AddGroupMembersDto dto = new AddGroupMembersDto(1L, 2L);

        assertThrows(GroupMemberDuplicateException.class, () -> groupService.addMember(dto));
    }
}