package com.fixflow.security;

/**
 * Contract for secure password hashing and verification.
 */
public interface PasswordHasher {

    /**
     * Hashes a plain-text password using a secure algorithm and salt.
     *
     * @param rawPassword the plain-text password to hash
     * @return the encoded hashed password string
     */
    String hash(String rawPassword);

    /**
     * Verifies whether a plain-text password matches a stored hash.
     *
     * @param rawPassword  the plain-text password to test
     * @param passwordHash the stored hashed password
     * @return true if the password matches the hash, false otherwise
     */
    boolean matches(String rawPassword, String passwordHash);
}
