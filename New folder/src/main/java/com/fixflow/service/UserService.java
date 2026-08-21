package com.fixflow.service;

import com.fixflow.exception.UserAlreadyExistsException;
import com.fixflow.exception.UserNotFoundException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.Role;
import com.fixflow.model.User;
import com.fixflow.model.UserStatus;
import com.fixflow.repository.UserRepository;
import com.fixflow.security.PasswordHasher;
import com.fixflow.validation.UserValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Service orchestrating User domain operations including registration,
 * profile updates, status management, and querying.
 */
public class UserService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepository userRepository, UserValidator userValidator, PasswordHasher passwordHasher) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
        this.userValidator = Objects.requireNonNull(userValidator, "userValidator cannot be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher cannot be null");
    }

    /**
     * Registers a new user in the system after validating inputs and checking uniqueness.
     */
    public User registerUser(String fullName, String username, String email, String rawPassword, Role role) {
        userValidator.validateRegistration(fullName, username, email, rawPassword, role);

        String normalizedUsername = username.trim();
        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new UserAlreadyExistsException("User with username '" + normalizedUsername + "' already exists");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("User with email '" + normalizedEmail + "' already exists");
        }

        String passwordHash = passwordHasher.hash(rawPassword);

        User newUser = User.builder()
                .fullName(fullName.trim())
                .username(normalizedUsername)
                .email(normalizedEmail)
                .passwordHash(passwordHash)
                .role(role)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(newUser);
    }

    /**
     * Retrieves a user by their unique database ID.
     */
    public User getUserById(Long id) {
        if (id == null) {
            throw new ValidationException("User ID cannot be null");
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
    }

    /**
     * Retrieves a user by their unique username.
     */
    public User getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }
        return userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new UserNotFoundException("User with username '" + username + "' not found"));
    }

    /**
     * Retrieves a user by their email address.
     */
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email cannot be empty");
        }
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("User with email '" + email + "' not found"));
    }

    /**
     * Updates an existing user's profile information.
     */
    public User updateUser(Long id, String fullName, String username, String email, Role role) {
        userValidator.validateUpdate(id, fullName, username, email, role);

        User existing = getUserById(id);

        String normalizedUsername = username.trim();
        String normalizedEmail = email.trim().toLowerCase();

        // Check if new username conflicts with another user
        if (!existing.getUsername().equalsIgnoreCase(normalizedUsername) &&
                userRepository.existsByUsername(normalizedUsername)) {
            throw new UserAlreadyExistsException("Username '" + normalizedUsername + "' is already taken");
        }

        // Check if new email conflicts with another user
        if (!existing.getEmail().equalsIgnoreCase(normalizedEmail) &&
                userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException("Email '" + normalizedEmail + "' is already taken");
        }

        existing.setFullName(fullName.trim());
        existing.setUsername(normalizedUsername);
        existing.setEmail(normalizedEmail);
        existing.setRole(role);
        existing.setUpdatedAt(LocalDateTime.now());

        return userRepository.update(existing);
    }

    /**
     * Activates an inactive user account.
     */
    public void activateUser(Long id) {
        User user = getUserById(id);
        user.setStatus(UserStatus.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.update(user);
    }

    /**
     * Deactivates an active user account.
     */
    public void deactivateUser(Long id) {
        User user = getUserById(id);
        user.setStatus(UserStatus.INACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.update(user);
    }

    /**
     * Deletes a user by ID.
     */
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user.getId());
    }

    /**
     * Lists all registered users.
     */
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    /**
     * Lists all users belonging to a specific role (e.g. TECHNICIAN).
     */
    public List<User> getUsersByRole(Role role) {
        if (role == null) {
            throw new ValidationException("Role cannot be null");
        }
        return userRepository.findByRole(role);
    }
}
