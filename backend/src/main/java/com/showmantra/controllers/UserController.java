package com.showmantra.controllers;

import com.showmantra.dtos.UserCreateDTO;
import com.showmantra.dtos.UserResponseDTO;
import com.showmantra.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserCreateDTO dto) {
        UserResponseDTO response = userService.createUser(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@Valid @RequestBody com.showmantra.dtos.LoginRequest dto) {
        UserResponseDTO response = userService.login(dto);
        return ResponseEntity.ok(response);
    }
}
