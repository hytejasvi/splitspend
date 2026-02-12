package com.tejas.splitspend.group;

import com.tejas.splitspend.group.dto.AddGroupMembersDto;
import com.tejas.splitspend.group.dto.CreateGroupDto;
import com.tejas.splitspend.group.exceptions.GroupMemberDuplicateException;
import com.tejas.splitspend.group.exceptions.GroupNotFoundException;
import com.tejas.splitspend.user.User;
import com.tejas.splitspend.user.UserRepository;
import com.tejas.splitspend.user.exceptions.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupService {

    private static final Logger log = LoggerFactory.getLogger(GroupService.class);
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository, GroupMemberRepository groupMemberRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Group createGroup(CreateGroupDto createDto) {
        log.info("Creating group '{}' for user ID: {}",
                createDto.groupName(), createDto.createdById());

        User creator = userRepository.findById(createDto.createdById())
                .orElseThrow(() -> new UserNotFoundException(
                        "User with ID " + createDto.createdById() + " not found"
                ));

        // Create group
        Group group = new Group(createDto.groupName(), createDto.createdById());

        // Add creator as admin
        GroupMember adminMember = new GroupMember(group, creator, MemberRole.ADMIN);
        group.getMembers().add(adminMember);

        Group savedGroup = groupRepository.save(group);

        log.info("Created group ID: {} with admin user ID: {}",
                savedGroup.getGroupId(), creator.getUserId());

        return savedGroup;
    }

    /*
     * Get all groups (temporary - will filter by user with JWT).
     */
    public List<Group> getAllGroups() {
        log.debug("Fetching all groups");
        return groupRepository.findAll();
    }

    /**
     * Add a new member to an existing group.
     */
    public Group addMember(AddGroupMembersDto addMemberDto) {
        log.info("Adding user ID: {} to group ID: {}",
                addMemberDto.userId(), addMemberDto.groupId());

        User user = userRepository.findById(addMemberDto.userId())
                .orElseThrow(() -> new UserNotFoundException(
                        "User with ID " + addMemberDto.userId() + " not found"
                ));

        Group group = groupRepository.findById(addMemberDto.groupId())
                .orElseThrow(() -> new GroupNotFoundException(
                        "Group with ID " + addMemberDto.groupId() + " not found"
                ));

        boolean alreadyMember = group.getMembers().stream()
                .anyMatch(member -> member.getUser().getUserId().equals(user.getUserId()));

        if (alreadyMember) {
            log.warn("User ID: {} is already a member of group ID: {}",
                    user.getUserId(), group.getGroupId());
            throw new GroupMemberDuplicateException(
                    user.getName()+" is already a member of this group"
            );
        }

        GroupMember member = new GroupMember(group, user, MemberRole.MEMBER);
        group.getMembers().add(member);

        Group savedGroup = groupRepository.save(group);
        log.info("Added user ID: {} to group ID: {}",
                user.getUserId(), group.getGroupId());

        return savedGroup;
    }
}
