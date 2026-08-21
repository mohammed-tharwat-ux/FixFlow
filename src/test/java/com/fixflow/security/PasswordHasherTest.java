package com.fixflow.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordHasher Tests")
class PasswordHasherTest {

    private PasswordHasher hasher;

    @BeforeEach
    void setUp() {
        hasher = new PBKDF2PasswordHasher();
    }

    @Test
    @DisplayName("Hash password successfully and return valid encoded format")
    void shouldHashPasswordSuccessfully() {
        String rawPassword = "StrongPassword123!";
        String hash = hasher.hash(rawPassword);

        assertNotNull(hash);
        assertNotEquals(rawPassword, hash);
        assertTrue(hash.contains(":"), "Hash format should contain delimiter");
        assertEquals(3, hash.split(":").length, "Hash should contain iterations:salt:hash");
    }

    @Test
    @DisplayName("Hashes for the same password must produce different salts and hashes")
    void shouldGenerateUniqueSaltsForSamePassword() {
        String rawPassword = "SamePassword123!";
        String hash1 = hasher.hash(rawPassword);
        String hash2 = hasher.hash(rawPassword);

        assertNotEquals(hash1, hash2, "Salts must be randomly generated so hashes differ");
        assertTrue(hasher.matches(rawPassword, hash1));
        assertTrue(hasher.matches(rawPassword, hash2));
    }

    @Test
    @DisplayName("Matches returns true for correct raw password")
    void shouldMatchCorrectPassword() {
        String rawPassword = "MySecretPassword99#";
        String hash = hasher.hash(rawPassword);

        assertTrue(hasher.matches(rawPassword, hash));
    }

    @Test
    @DisplayName("Matches returns false for incorrect raw password")
    void shouldNotMatchWrongPassword() {
        String correctPassword = "CorrectPassword1!";
        String wrongPassword = "WrongPassword1!";
        String hash = hasher.hash(correctPassword);

        assertFalse(hasher.matches(wrongPassword, hash));
    }

    @Test
    @DisplayName("Matches returns false for malformed or null hashes")
    void shouldReturnFalseForMalformedHashes() {
        assertFalse(hasher.matches("Password123!", null));
        assertFalse(hasher.matches(null, "someHash"));
        assertFalse(hasher.matches("Password123!", "invalidFormat"));
        assertFalse(hasher.matches("Password123!", "10000:invalidsalt:invalidhash"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Hash throws IllegalArgumentException on null or empty input")
    void shouldThrowExceptionWhenHashingNullOrEmpty(String invalidPassword) {
        assertThrows(IllegalArgumentException.class, () -> hasher.hash(invalidPassword));
    }
}
