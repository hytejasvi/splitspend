package com.tejas.splitspend.user;

import com.tejas.splitspend.user.exceptions.EmailAlreadyExistsException;
import com.tejas.splitspend.user.exceptions.PhoneNumberAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

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
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User user=i.getArgument(0);
            return user;
        });
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