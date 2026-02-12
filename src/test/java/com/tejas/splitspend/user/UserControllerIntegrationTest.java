package com.tejas.splitspend.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tejas.splitspend.user.dto.LoginRequestDto;
import com.tejas.splitspend.user.dto.UserSignupDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.save(
                new User("test11", "test11@example.com", "9876543222", "password")
        );
    }

    @Test
    void signup_Success() throws Exception {

        UserSignupDto dto = new UserSignupDto(
                "Tejas",
                "tejas11@example.com",
                "9876543250",
                "password123"
        );

        mockMvc.perform(post("/v1/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.name").value("Tejas"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.email").value("tejas11@example.com"));
    }

    @Test
    void signup_Returns409_WhenEmailAlreadyExists() throws Exception {

        UserSignupDto dto = new UserSignupDto(
                "test1",
                "test11@example.com",
                "9876543210",
                "password1234"
        );

        mockMvc.perform(post("/v1/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("User with email test11@example.com already exists"));
    }

    @Test
    void signup_Returns409_WhenPhoneNumberAlreadyExists() throws Exception {

        UserSignupDto dto = new UserSignupDto(
                "test1",
                "newemail@example.com",
                "9876543222",
                "password1234"
        );

        mockMvc.perform(post("/v1/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("User with phone number 9876543222 already exists"));
    }

    @Test
    void signup_Returns400_WhenValidationFails() throws Exception {

        UserSignupDto dto = new UserSignupDto(
                "Tejas",
                "invalid-email",
                "9876543210",
                "password123"
        );

        mockMvc.perform(post("/v1/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation Failed"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void login_Success() throws Exception {
        UserSignupDto signupDto = new UserSignupDto(
                "Tejas",
                "login@example.com",
                "9876543211",
                "password123"
        );

        mockMvc.perform(post("/v1/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDto)))
                .andExpect(status().isCreated());

        LoginRequestDto loginDto = new LoginRequestDto(
                "login@example.com",
                "password123"
        );

        mockMvc.perform(post("/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.email").value("login@example.com"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.name").value("Tejas"));
    }

    @Test
    void login_Returns401_WhenEmailNotFound() throws Exception {
        LoginRequestDto loginDto = new LoginRequestDto(
                "notfound@example.com",
                "password123"
        );

        mockMvc.perform(post("/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_Returns401_WhenPasswordIncorrect() throws Exception {
        UserSignupDto signupDto = new UserSignupDto(
                "Tejas",
                "wrongpass@example.com",
                "9876543212",
                "password123"
        );

        mockMvc.perform(post("/v1/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupDto)))
                .andExpect(status().isCreated());

        LoginRequestDto loginDto = new LoginRequestDto(
                "wrongpass@example.com",
                "wrongpassword"
        );

        mockMvc.perform(post("/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}