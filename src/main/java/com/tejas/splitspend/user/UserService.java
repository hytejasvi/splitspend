package com.tejas.splitspend.user;

import com.tejas.splitspend.user.dto.LoginRequestDto;
import com.tejas.splitspend.user.dto.UserSignupDto;
import com.tejas.splitspend.user.exceptions.EmailAlreadyExistsException;
import com.tejas.splitspend.user.exceptions.InvalidCredentialsException;
import com.tejas.splitspend.user.exceptions.PhoneNumberAlreadyExistsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(UserSignupDto userSignupDto) {
        if (userRepository.existsByEmail(userSignupDto.email())) {
            throw new EmailAlreadyExistsException("User with email " + userSignupDto.email() + " already exists");
        }

        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(userSignupDto.phoneNumber())) {
            throw new PhoneNumberAlreadyExistsException("User with phone number " + userSignupDto.phoneNumber() + " already exists");
        }

        User user = mapDtoToEntity(userSignupDto);
        return userRepository.save(user);
    }

    public User userLogin(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(loginRequestDto.password(), user.getPassword())) {
            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }
        return user;
    }

    private String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    private User mapDtoToEntity(UserSignupDto signupDto) {
        return new User(
                signupDto.name(),
                signupDto.email(),
                signupDto.phoneNumber(),
                hashPassword(signupDto.password())
        );
    }
}
