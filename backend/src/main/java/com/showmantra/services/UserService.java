package com.showmantra.services;

import com.showmantra.dtos.LoginRequest;
import com.showmantra.dtos.UserCreateRequest;
import com.showmantra.dtos.UserResponse;
import com.showmantra.entities.User;
import com.showmantra.entities.enums.Role;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.showmantra.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with email " + request.getEmail() + " already exists.");
        }

        User user = User.builder()
                .email(request.getEmail())
                // Storing password as provided since no password encoder is specified
                .passwordHash(request.getPassword()) 
                .phone(request.getPhone())
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .build();

        user = userRepository.save(user);
        log.info("Created new user with email: {}", user.getEmail());

        String token = jwtService.generateToken(user.getId().toString());

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .token(token)
                .build();
    }

    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid email or password."));
        
        if (!user.getPasswordHash().equals(request.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        String token = jwtService.generateToken(user.getId().toString());

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .token(token)
                .build();
    }
}
