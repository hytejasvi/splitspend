package com.tejas.splitspend.user;

import com.tejas.splitspend.user.dto.LoginRequestDto;
import com.tejas.splitspend.user.dto.UserSignupDto;
import com.tejas.splitspend.user.exceptions.EmailAlreadyExistsException;
import com.tejas.splitspend.user.exceptions.InvalidCredentialsException;
import com.tejas.splitspend.user.exceptions.PhoneNumberAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_Success() {

        UserSignupDto signupDto = new UserSignupDto(
                "Tejas",
                "tejas@example.com",
                "9876543210",
                "password123"
        );

        when(userRepository.existsByEmail(signupDto.email())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(signupDto.phoneNumber())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User result = userService.createUser(signupDto);

        assertNotNull(result);
        assertEquals("Tejas", result.getName());
        assertEquals("tejas@example.com", result.getEmail());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_ThrowsException_WhenEmailExists() {

        UserSignupDto dto = new UserSignupDto(
                "Tejas",
                "existing@example.com",
                "9876543210",
                "password123"
        );

        when(userRepository.existsByEmail(dto.email())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.createUser(dto);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        LoginRequestDto request = new LoginRequestDto(
                "tejas@example.com",
                "password123"
        );

        User user = new User("Tejas", "tejas@example.com", "9876543210", "$2a$10$hashedPassword");

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword()))
                .thenReturn(true);

        User result = userService.userLogin(request);

        assertNotNull(result);
        assertEquals("Tejas", result.getName());
        verify(userRepository, times(1)).findByEmail(request.email());
    }

    @Test
    void login_ThrowsException_WhenEmailNotFound() {
        LoginRequestDto request = new LoginRequestDto(
                "notfound@example.com",
                "password123"
        );

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> userService.userLogin(request));

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_ThrowsException_WhenPasswordIncorrect() {
        LoginRequestDto request = new LoginRequestDto(
                "tejas@example.com",
                "wrongpassword"
        );

        User user = new User("Tejas", "tejas@example.com", "9876543210", "$2a$10$hashedPassword");

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword()))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> userService.userLogin(request));
    }

    @Test
    void createUser_ThrowsException_WhenPhoneNumberExists() {

        UserSignupDto dto = new UserSignupDto(
                "Tejas",
                "Tejas@example.com",
                "9876543210",
                "password123"
        );

        when(userRepository.existsByEmail(dto.email())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(dto.phoneNumber())).thenReturn(true);

        assertThrows(PhoneNumberAlreadyExistsException.class, () -> {
            userService.createUser(dto);
        });

        verify(userRepository, never()).save(any(User.class));
    }
}