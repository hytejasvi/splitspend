package com.tejas.splitspend.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tejas.splitspend.common.GlobalExceptionHandler;
import com.tejas.splitspend.common.SecurityConfig;
import com.tejas.splitspend.user.exceptions.EmailAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void signup_Returns500_WhenUnexpectedExceptionOccurs() throws Exception {

        UserSignupDto dto = new UserSignupDto(
                "test1",
                "test12@example.com",
                "9776543210",
                "password1234"
        );

        when(userService.createUser(any(UserSignupDto.class)))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/v1/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message")
                        .value("An unexpected error occurred"));
    }

    @Test
    void signup_Returns409_WhenUserExceptionThrown() throws Exception {

        UserSignupDto dto = new UserSignupDto(
                "test1",
                "test@example.com",
                "9876543210",
                "password1234"
        );

        when(userService.createUser(any(UserSignupDto.class)))
                .thenThrow(new EmailAlreadyExistsException(
                        "User with email test@example.com already exists"));

        mockMvc.perform(post("/v1/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("User with email test@example.com already exists"));
    }
}
