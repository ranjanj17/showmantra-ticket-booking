package com.showmantra.dtos;

import com.showmantra.entities.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String phone;
    private Role role;
    private String token; // The JWT token for authentication
}
