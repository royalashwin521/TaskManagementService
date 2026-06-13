package com.TMP.uas.service;

import com.TMP.uas.common.Role;
import com.TMP.uas.configuration.JwtProvider;
import com.TMP.uas.dto.LoginRequest;
import com.TMP.uas.dto.TokenResponse;
import com.TMP.uas.dto.UserRegistrationRequest;
import com.TMP.uas.dto.UserResponse;
import com.TMP.uas.entity.User;
import com.TMP.uas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            // Assuming you have a similar BusinessException in this microservice
            throw new RuntimeException("Email is already in use"); 
        }

        User user = new User();
        user.setEmail(request.email());
        // 1. Hash the password! Never save request.password() directly.
        user.setPassword(passwordEncoder.encode(request.password())); 
        // 2. Default to USER if no role is provided
        user.setRole(request.role() != null ? request.role() : Role.USER);

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    public TokenResponse login(LoginRequest request) {
        // 1. Find the user
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password")); // Use your custom BusinessException

        // 2. Verify the password matches the BCrypt hash
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // 3. Generate and return the JWT
        String token = jwtProvider.generateToken(user);
        return new TokenResponse(token);
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    // Manual Mapper (keeps passwords completely isolated from the web layer)
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}