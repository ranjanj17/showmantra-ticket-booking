package com.showmantra.services;

import com.showmantra.dtos.UserCreateDTO;
import com.showmantra.dtos.UserResponseDTO;
import com.showmantra.entities.User;
import com.showmantra.entities.enums.Role;
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

    @Transactional
    public UserResponseDTO createUser(UserCreateDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("User with email " + dto.getEmail() + " already exists.");
        }

        User user = User.builder()
                .email(dto.getEmail())
                // Storing password as provided since no password encoder is specified
                .passwordHash(dto.getPassword()) 
                .phone(dto.getPhone())
                .role(dto.getRole() != null ? dto.getRole() : Role.USER)
                .build();

        user = userRepository.save(user);
        log.info("Created new user with email: {}", user.getEmail());

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }

    public UserResponseDTO login(com.showmantra.dtos.LoginRequest dto) {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        
        if (!user.getPasswordHash().equals(dto.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }
}
