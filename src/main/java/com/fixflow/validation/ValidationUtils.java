package com.fixflow.validation;

import java.util.regex.Pattern;

/**
 * Utility helper class containing validation logic and regular expressions.
 */
public final class ValidationUtils {

    public static final int FULL_NAME_MIN_LENGTH = 2;
    public static final int FULL_NAME_MAX_LENGTH = 50;

    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 20;

    public static final int EMAIL_MAX_LENGTH = 100;

    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 64;

    // Full name allows unicode letters, spaces, hyphens, and apostrophes
    private static final Pattern FULL_NAME_PATTERN = Pattern.compile("^[a-zA-Z\\p{L}][a-zA-Z\\p{L}\\s'-]*[a-zA-Z\\p{L}]$");

    // Username: 3-20 chars, starts with letter, contains letters, digits, underscores, no spaces
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{2,19}$");

    // Email standard regex
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    // Password components
    private static final Pattern PASSWORD_UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern PASSWORD_LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");
    private static final Pattern PASSWORD_DIGIT_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern PASSWORD_SPECIAL_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");

    private ValidationUtils() {
        // Prevent instantiation
    }

    public static boolean isNullOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidFullName(String fullName) {
        if (isNullOrBlank(fullName)) {
            return false;
        }
        String trimmed = fullName.trim();
        if (trimmed.length() < FULL_NAME_MIN_LENGTH || trimmed.length() > FULL_NAME_MAX_LENGTH) {
            return false;
        }
        return FULL_NAME_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidUsername(String username) {
        if (isNullOrBlank(username)) {
            return false;
        }
        // Strict: no leading or trailing whitespace allowed
        if (!username.equals(username.trim())) {
            return false;
        }
        if (username.length() < USERNAME_MIN_LENGTH || username.length() > USERNAME_MAX_LENGTH) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidEmail(String email) {
        if (isNullOrBlank(email)) {
            return false;
        }
        String trimmed = email.trim();
        if (trimmed.length() > EMAIL_MAX_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null) {
            return false;
        }
        if (password.length() < PASSWORD_MIN_LENGTH || password.length() > PASSWORD_MAX_LENGTH) {
            return false;
        }
        return PASSWORD_UPPERCASE_PATTERN.matcher(password).matches()
                && PASSWORD_LOWERCASE_PATTERN.matcher(password).matches()
                && PASSWORD_DIGIT_PATTERN.matcher(password).matches()
                && PASSWORD_SPECIAL_PATTERN.matcher(password).matches();
    }
}
