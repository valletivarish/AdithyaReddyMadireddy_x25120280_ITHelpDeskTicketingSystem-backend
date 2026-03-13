package com.helpdesk.repository;

import com.helpdesk.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity database operations.
 * Provides custom query methods for authentication and user lookup.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Find a user by their email address - used for login authentication */
    Optional<User> findByEmail(String email);

    /** Check if a user with the given email already exists - used during registration */
    boolean existsByEmail(String email);
}
