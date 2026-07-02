package com.showmantra.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

import com.showmantra.entities.enums.Role;

/**
 * Represents a registered user in the ShowMantra system.
 * Uses UUID as the primary key for security to prevent ID enumeration attacks.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    // Using UUID instead of sequential Long prevents attackers from guessing order numbers (ID enumeration).
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String phone;

    // Saving as STRING makes the database readable (e.g. 'ADMIN') instead of saving meaningless integer ordinals (e.g. '1').
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
