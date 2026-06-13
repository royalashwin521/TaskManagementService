package com.TMP.uas.transport;

import com.TMP.uas.dto.LoginRequest;
import com.TMP.uas.dto.TokenResponse;
import com.TMP.uas.dto.UserRegistrationRequest;
import com.TMP.uas.dto.UserResponse;
import com.TMP.uas.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * Endpoint: POST /api/v1/auth/register
     * Description: Registers a new user. No JWT required.
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint: POST /api/v1/auth/login
     * Description: Authenticates a user and returns a JWT. No JWT required.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }
}