package com.fixflow.repository;

import com.fixflow.model.Role;
import com.fixflow.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data access abstraction interface for User persistence.
 * Allows decoupling business services from physical storage implementation.
 */
public interface UserRepository {

    /**
     * Finds a user by unique identifier.
     *
     * @param id the user ID
     * @return an Optional containing the User if found, or empty Optional
     */
    Optional<User> findById(Long id);

    /**
     * Finds a user by unique username (case-insensitive search recommended).
     *
     * @param username the username
     * @return an Optional containing the User if found, or empty Optional
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by unique email address (case-insensitive search).
     *
     * @param email the email address
     * @return an Optional containing the User if found, or empty Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user with the given username already exists.
     *
     * @param username the username to check
     * @return true if exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user with the given email already exists.
     *
     * @param email the email to check
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Persists a new user in the repository and generates an ID.
     *
     * @param user the user to persist
     * @return the saved user with populated ID and timestamps
     */
    User save(User user);

    /**
     * Updates an existing user record.
     *
     * @param user the user containing updated fields
     * @return the updated user
     */
    User update(User user);

    /**
     * Deletes a user by ID.
     *
     * @param id the user ID to delete
     */
    void delete(Long id);

    /**
     * Retrieves all users currently in the repository.
     *
     * @return list of all users
     */
    List<User> findAll();

    /**
     * Retrieves all users with a specific role.
     *
     * @param role the role to filter by (e.g. TECHNICIAN)
     * @return list of matching users
     */
    List<User> findByRole(Role role);

    /**
     * Clears all users from the repository (useful for testing).
     */
    void clear();
}
