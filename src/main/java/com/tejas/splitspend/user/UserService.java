package com.tejas.splitspend.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(UserSignupDto userSignupDto) {
        if (userRepository.existsByEmail(userSignupDto.email())) {
            throw new RuntimeException("User with email " + userSignupDto.email() + " already exists"); // TODO: Implement specific error
        }

        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(userSignupDto.phoneNumber())) {
            throw new RuntimeException("User with phone number " + userSignupDto.phoneNumber() + " already exists"); // TODO: Implement specific error

        }

        User user = mapDtoToEntity(userSignupDto);
        return userRepository.save(user);
    }

    public boolean checkIfUserExists(String emailId) {
        return userRepository.existsByEmail(emailId);
    }

    private String hashPassword(String plainPassword) {
        // BCrypt hashing
        return plainPassword; // TODO: Implement BCrypt
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
