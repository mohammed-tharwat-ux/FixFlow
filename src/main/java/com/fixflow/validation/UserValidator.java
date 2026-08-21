package com.fixflow.validation;

import com.fixflow.exception.ValidationException;
import com.fixflow.model.Role;
import com.fixflow.model.User;
import com.fixflow.model.UserStatus;

/**
 * Validates user data against business rules and boundary constraints.
 * Throws {@link ValidationException} when validation fails.
 */
public class UserValidator {

    /**
     * Validates input fields during user registration.
     */
    public void validateRegistration(String fullName, String username, String email, String password, Role role) {
        validateFullName(fullName);
        validateUsername(username);
        validateEmail(email);
        validatePassword(password);
        validateRole(role);
    }

    /**
     * Validates input fields during user profile update.
     */
    public void validateUpdate(Long id, String fullName, String username, String email, Role role) {
        if (id == null || id <= 0) {
            throw new ValidationException("User ID must be a positive non-null number");
        }
        validateFullName(fullName);
        validateUsername(username);
        validateEmail(email);
        validateRole(role);
    }

    /**
     * Validates user credentials before processing login.
     */
    public void validateLoginCredentials(String username, String password) {
        if (ValidationUtils.isNullOrBlank(username)) {
            throw new ValidationException("Username cannot be empty");
        }
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password cannot be empty");
        }
    }

    /**
     * Validates an existing user domain entity.
     */
    public void validateUser(User user) {
        if (user == null) {
            throw new ValidationException("User cannot be null");
        }
        validateFullName(user.getFullName());
        validateUsername(user.getUsername());
        validateEmail(user.getEmail());
        validateRole(user.getRole());
        validateStatus(user.getStatus());
        if (ValidationUtils.isNullOrBlank(user.getPasswordHash())) {
            throw new ValidationException("Password hash cannot be empty");
        }
    }

    public void validateFullName(String fullName) {
        if (ValidationUtils.isNullOrBlank(fullName)) {
            throw new ValidationException("Full name cannot be null or empty");
        }
        String trimmed = fullName.trim();
        if (trimmed.length() < ValidationUtils.FULL_NAME_MIN_LENGTH) {
            throw new ValidationException("Full name must be at least " + ValidationUtils.FULL_NAME_MIN_LENGTH + " characters long");
        }
        if (trimmed.length() > ValidationUtils.FULL_NAME_MAX_LENGTH) {
            throw new ValidationException("Full name cannot exceed " + ValidationUtils.FULL_NAME_MAX_LENGTH + " characters");
        }
        if (!ValidationUtils.isValidFullName(fullName)) {
            throw new ValidationException("Full name contains invalid characters");
        }
    }

    public void validateUsername(String username) {
        if (ValidationUtils.isNullOrBlank(username)) {
            throw new ValidationException("Username cannot be null or empty");
        }
        if (!username.equals(username.trim())) {
            throw new ValidationException("Username cannot have leading or trailing whitespace");
        }
        if (username.length() < ValidationUtils.USERNAME_MIN_LENGTH) {
            throw new ValidationException("Username must be at least " + ValidationUtils.USERNAME_MIN_LENGTH + " characters long");
        }
        if (username.length() > ValidationUtils.USERNAME_MAX_LENGTH) {
            throw new ValidationException("Username cannot exceed " + ValidationUtils.USERNAME_MAX_LENGTH + " characters");
        }
        if (!ValidationUtils.isValidUsername(username)) {
            throw new ValidationException("Username must start with a letter and contain only alphanumeric characters or underscores");
        }
    }

    public void validateEmail(String email) {
        if (ValidationUtils.isNullOrBlank(email)) {
            throw new ValidationException("Email cannot be null or empty");
        }
        String trimmed = email.trim();
        if (trimmed.length() > ValidationUtils.EMAIL_MAX_LENGTH) {
            throw new ValidationException("Email cannot exceed " + ValidationUtils.EMAIL_MAX_LENGTH + " characters");
        }
        if (!ValidationUtils.isValidEmail(trimmed)) {
            throw new ValidationException("Email format is invalid: " + email);
        }
    }

    public void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password cannot be null or empty");
        }
        if (password.length() < ValidationUtils.PASSWORD_MIN_LENGTH) {
            throw new ValidationException("Password must be at least " + ValidationUtils.PASSWORD_MIN_LENGTH + " characters long");
        }
        if (password.length() > ValidationUtils.PASSWORD_MAX_LENGTH) {
            throw new ValidationException("Password cannot exceed " + ValidationUtils.PASSWORD_MAX_LENGTH + " characters");
        }
        if (!ValidationUtils.isValidPassword(password)) {
            throw new ValidationException("Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
        }
    }

    public void validateRole(Role role) {
        if (role == null) {
            throw new ValidationException("Role cannot be null");
        }
    }

    public void validateStatus(UserStatus status) {
        if (status == null) {
            throw new ValidationException("User status cannot be null");
        }
    }
}
