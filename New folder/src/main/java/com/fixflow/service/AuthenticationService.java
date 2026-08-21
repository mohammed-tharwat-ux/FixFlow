package com.fixflow.service;

import com.fixflow.exception.AuthenticationException;
import com.fixflow.model.User;
import com.fixflow.model.UserStatus;
import com.fixflow.repository.UserRepository;
import com.fixflow.security.PasswordHasher;
import com.fixflow.validation.UserValidator;

import java.util.Objects;

/**
 * Service handling user authentication and credential verification.
 */
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final UserValidator userValidator;

    public AuthenticationService(UserRepository userRepository, PasswordHasher passwordHasher, UserValidator userValidator) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository cannot be null");
        this.passwordHasher = Objects.requireNonNull(passwordHasher, "passwordHasher cannot be null");
        this.userValidator = Objects.requireNonNull(userValidator, "userValidator cannot be null");
    }

    /**
     * Authenticates a user given their username and plain-text password.
     *
     * @param username    the username
     * @param rawPassword the raw password
     * @return authenticated User object
     * @throws AuthenticationException if authentication fails due to invalid credentials or inactive status
     */
    public User login(String username, String rawPassword) {
        try {
            userValidator.validateLoginCredentials(username, rawPassword);
        } catch (Exception e) {
            throw new AuthenticationException("Invalid login credentials: " + e.getMessage(), e);
        }

        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationException("User account is inactive. Please contact system administrator.");
        }

        if (!passwordHasher.matches(rawPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password");
        }

        return user;
    }

    /**
     * Performs logout cleanup if needed.
     */
    public void logout(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username cannot be empty for logout");
        }
        // In stateless/in-memory authentication, logout acknowledges the session termination
    }
}
