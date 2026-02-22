package com.tejas.splitspend.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.tejas.splitspend.group.dto.AddGroupMembersDto;
import com.tejas.splitspend.group.dto.CreateGroupDto;
import com.tejas.splitspend.group.dto.GroupResponseDto;
import com.tejas.splitspend.user.User;
import com.tejas.splitspend.common.SecurityConfig;
import com.tejas.splitspend.user.UserRepository;
import io.swagger.v3.core.util.Json;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static com.tejas.splitspend.common.fixtures.getValidUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@Transactional
class GroupControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupService groupService;

    @Test
    void createGroup_Success() throws Exception {
        // Create user first
        User user = userRepository.save(getValidUser());

        System.out.println(user.getUserId()+" : "+user.getName());

        CreateGroupDto dto = new CreateGroupDto("Goa Trip 2026", user.getUserId());

        mockMvc.perform(post("/v1/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupId").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupName").value("Goa Trip 2026"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.memberCount").value(1));
    }

    @Test
    void createGroup_Returns400_WhenValidationFails_NameIsNull() throws Exception {
        CreateGroupDto dto = new CreateGroupDto("", 2L);

        mockMvc.perform(post("/v1/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Validation Failed"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").exists());
    }

    @Test
    void createGroup_Returns400_WhenValidationFails_IdIsNull() throws Exception {
        CreateGroupDto dto = new CreateGroupDto("Paris Trip 2026", null);

        mockMvc.perform(post("/v1/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Validation Failed"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors").exists());
    }

    @Test
    void getUserGroups_ReturnsMultipleGroups() throws Exception {
        // Create a user
        User user1 = userRepository.save(new User("User1", "u1@e.com", "1111111111", "pass"));
        Long userId = user1.getUserId();


        // Create 2 groups
        mockMvc.perform(post("/v1/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateGroupDto("Group 1", userId))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateGroupDto("Group 2", userId))))
                .andExpect(status().isCreated());

        // Get all groups for user1
        mockMvc.perform(get("/v1/groups")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].groupName").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].groupName").exists());
    }

    @Test
    void getUserGroups_ThrowsUserNotFoundException() throws Exception {
        Long userId = 99L;
        mockMvc.perform(get("/v1/groups")
                        .param("userId", userId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void addMember_Success() throws Exception {

        User user1 = userRepository.save(new User("User1", "u1@e.com", "1111111111", "pass"));
        User user2 = userRepository.save(new User("User2", "u2@e.com", "1111111112", "pass"));

        CreateGroupDto groupDto = new CreateGroupDto("Group1", user1.getUserId());
        MvcResult createGroupResult = mockMvc.perform(post("/v1/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isCreated())
                .andReturn();


        String responseBody = createGroupResult.getResponse().getContentAsString();
        GroupResponseDto dto = objectMapper.readValue(responseBody, GroupResponseDto.class);
        Long groupId = dto.groupId();
        String expectedGroupName = dto.groupName();

        AddGroupMembersDto addGroupMembersDto = new AddGroupMembersDto(groupId, user2.getUserId());
        mockMvc.perform(post("/v1/groups/member/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addGroupMembersDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupName").value(expectedGroupName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.memberCount").value(2));
    }

    @Test
    void addMember_ThrowsUserNotFoundException() throws Exception {

        Long createdById = 1L;
        CreateGroupDto groupDto = new CreateGroupDto("Group1", createdById);
        mockMvc.perform(post("/v1/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addMember_ThrowsGroupNotFoundException() throws Exception {

        User user1 = userRepository.save(new User("User1", "u1@e.com", "1111111111", "pass"));

        Long groupId = 100L;

        AddGroupMembersDto addGroupMembersDto = new AddGroupMembersDto(groupId, user1.getUserId());
        mockMvc.perform(post("/v1/groups/member/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addGroupMembersDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addMember_ThrowsGroupMemberDuplicateException() throws Exception {

        User user1 = userRepository.save(new User("User1", "u1@e.com", "1111111111", "pass"));
        User user2 = userRepository.save(new User("User2", "u2@e.com", "1111111112", "pass"));

        CreateGroupDto groupDto = new CreateGroupDto("Group1", user1.getUserId());
        MvcResult createGroupResult = mockMvc.perform(post("/v1/groups/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupDto)))
                .andExpect(status().isCreated())
                .andReturn();


        String responseBody = createGroupResult.getResponse().getContentAsString();
        GroupResponseDto dto = objectMapper.readValue(responseBody, GroupResponseDto.class);
        Long groupId = dto.groupId();
        String expectedGroupName = dto.groupName();

        AddGroupMembersDto addGroupMembersDto = new AddGroupMembersDto(groupId, user2.getUserId());
        mockMvc.perform(post("/v1/groups/member/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addGroupMembersDto)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.groupName").value(expectedGroupName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.memberCount").value(2));

        //Trying to add the same user again
        mockMvc.perform(post("/v1/groups/member/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addGroupMembersDto)))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(user2.getName()+" is already a member of this group"));
    }
}
