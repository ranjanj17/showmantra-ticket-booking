package com.showmantra.repositories;

import com.showmantra.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing User entities.
 */
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Checks if a user with the given email already exists in the database.
     * @param email The email to check.
     * @return true if the email exists, false otherwise.
     */
    boolean existsByEmail(String email);
    
    /**
     * Retrieves a user by their email address.
     * @param email The email address to search for.
     * @return An Optional containing the User if found, or an empty Optional if not found.
     */
    Optional<User> findByEmail(String email);
}
