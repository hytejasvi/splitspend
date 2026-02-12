package com.tejas.splitspend.user;

import com.tejas.splitspend.user.dto.LoginRequestDto;
import com.tejas.splitspend.user.dto.LoginResponseDto;
import com.tejas.splitspend.user.dto.UserResponseDto;
import com.tejas.splitspend.user.dto.UserSignupDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> userSignup(@Valid @RequestBody UserSignupDto signupDto) {
        User user = userService.createUser(signupDto);
        UserResponseDto response = UserResponseDto.from(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> userLogin(@Valid @RequestBody LoginRequestDto requestDto) {
        User user = userService.userLogin(requestDto);
        LoginResponseDto response = LoginResponseDto.From(user);
        return ResponseEntity.ok(response);
    }
}
