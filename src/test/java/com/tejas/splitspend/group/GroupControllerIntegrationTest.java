package com.tejas.splitspend.group;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tejas.splitspend.group.dto.CreateGroupDto;
import com.tejas.splitspend.user.User;
import com.tejas.splitspend.common.SecurityConfig;
import com.tejas.splitspend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static com.tejas.splitspend.common.fixtures.getValidUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
}
