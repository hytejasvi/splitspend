package com.tejas.splitspend.group;

import com.tejas.splitspend.group.dto.CreateGroupDto;
import com.tejas.splitspend.user.User;
import com.tejas.splitspend.user.UserRepository;
import com.tejas.splitspend.user.exceptions.InvalidCredentialsException;
import com.tejas.splitspend.user.exceptions.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.tejas.splitspend.common.fixtures.getValidUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

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

        when(userRepository.findById(1L)).thenThrow(new UserNotFoundException("User with ID " + dto.createdById() + " not found"));
        assertThrows(UserNotFoundException.class,
                () -> groupService.createGroup(dto));
    }

    @Test
    void getAllGroups() {
    }

    @Test
    void addMember() {
    }
}