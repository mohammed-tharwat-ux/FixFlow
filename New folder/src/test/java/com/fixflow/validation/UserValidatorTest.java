package com.fixflow.validation;

import com.fixflow.exception.ValidationException;
import com.fixflow.model.Role;
import com.fixflow.model.User;
import com.fixflow.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserValidator Unit & Boundary Tests")
class UserValidatorTest {

    private UserValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UserValidator();
    }

    @Nested
    @DisplayName("Full Name Validation & Boundary Tests")
    class FullNameValidationTests {

        @Test
        @DisplayName("Valid standard full names")
        void shouldAcceptValidFullNames() {
            assertDoesNotThrow(() -> validator.validateFullName("John Doe"));
            assertDoesNotThrow(() -> validator.validateFullName("Jane O'Connor"));
            assertDoesNotThrow(() -> validator.validateFullName("Jean-Luc Picard"));
        }

        @Test
        @DisplayName("Boundary: min-1 (1 char) -> invalid")
        void shouldRejectFullNameBelowMin() {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> validator.validateFullName("A"));
            assertTrue(ex.getMessage().contains("at least 2 characters"));
        }

        @Test
        @DisplayName("Boundary: min (2 chars) -> valid")
        void shouldAcceptFullNameAtMin() {
            assertDoesNotThrow(() -> validator.validateFullName("Al"));
        }

        @Test
        @DisplayName("Boundary: min+1 (3 chars) -> valid")
        void shouldAcceptFullNameAboveMin() {
            assertDoesNotThrow(() -> validator.validateFullName("Ali"));
        }

        @Test
        @DisplayName("Boundary: max-1 (49 chars) -> valid")
        void shouldAcceptFullNameBelowMax() {
            String name49 = "A" + "b".repeat(47) + "c";
            assertEquals(49, name49.length());
            assertDoesNotThrow(() -> validator.validateFullName(name49));
        }

        @Test
        @DisplayName("Boundary: max (50 chars) -> valid")
        void shouldAcceptFullNameAtMax() {
            String name50 = "A" + "b".repeat(48) + "c";
            assertEquals(50, name50.length());
            assertDoesNotThrow(() -> validator.validateFullName(name50));
        }

        @Test
        @DisplayName("Boundary: max+1 (51 chars) -> invalid")
        void shouldRejectFullNameAboveMax() {
            String name51 = "A" + "b".repeat(49) + "c";
            assertEquals(51, name51.length());
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> validator.validateFullName(name51));
            assertTrue(ex.getMessage().contains("cannot exceed 50 characters"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Null, empty or blank full names -> invalid")
        void shouldRejectNullOrBlankFullName(String invalidName) {
            assertThrows(ValidationException.class, () -> validator.validateFullName(invalidName));
        }

        @ParameterizedTest
        @ValueSource(strings = {"John123", "John@Doe", "John#Doe", "12345"})
        @DisplayName("Full names with numbers and invalid symbols -> invalid")
        void shouldRejectFullNameWithInvalidCharacters(String invalidName) {
            assertThrows(ValidationException.class, () -> validator.validateFullName(invalidName));
        }
    }

    @Nested
    @DisplayName("Username Validation & Boundary Tests")
    class UsernameValidationTests {

        @Test
        @DisplayName("Valid standard usernames")
        void shouldAcceptValidUsernames() {
            assertDoesNotThrow(() -> validator.validateUsername("john_doe"));
            assertDoesNotThrow(() -> validator.validateUsername("user123"));
            assertDoesNotThrow(() -> validator.validateUsername("admin_user_01"));
        }

        @Test
        @DisplayName("Boundary: min-1 (2 chars) -> invalid")
        void shouldRejectUsernameBelowMin() {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> validator.validateUsername("ab"));
            assertTrue(ex.getMessage().contains("at least 3 characters"));
        }

        @Test
        @DisplayName("Boundary: min (3 chars) -> valid")
        void shouldAcceptUsernameAtMin() {
            assertDoesNotThrow(() -> validator.validateUsername("abc"));
        }

        @Test
        @DisplayName("Boundary: min+1 (4 chars) -> valid")
        void shouldAcceptUsernameAboveMin() {
            assertDoesNotThrow(() -> validator.validateUsername("abcd"));
        }

        @Test
        @DisplayName("Boundary: max-1 (19 chars) -> valid")
        void shouldAcceptUsernameBelowMax() {
            String user19 = "u" + "a".repeat(18);
            assertEquals(19, user19.length());
            assertDoesNotThrow(() -> validator.validateUsername(user19));
        }

        @Test
        @DisplayName("Boundary: max (20 chars) -> valid")
        void shouldAcceptUsernameAtMax() {
            String user20 = "u" + "a".repeat(19);
            assertEquals(20, user20.length());
            assertDoesNotThrow(() -> validator.validateUsername(user20));
        }

        @Test
        @DisplayName("Boundary: max+1 (21 chars) -> invalid")
        void shouldRejectUsernameAboveMax() {
            String user21 = "u" + "a".repeat(20);
            assertEquals(21, user21.length());
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> validator.validateUsername(user21));
            assertTrue(ex.getMessage().contains("cannot exceed 20 characters"));
        }

        @ParameterizedTest
        @ValueSource(strings = {" john", "john ", " john ", "user name"})
        @DisplayName("Usernames with whitespace -> invalid")
        void shouldRejectUsernameWithWhitespace(String invalidUsername) {
            assertThrows(ValidationException.class, () -> validator.validateUsername(invalidUsername));
        }

        @ParameterizedTest
        @ValueSource(strings = {"123user", "_admin", "@user", "user-name", "user.name"})
        @DisplayName("Usernames not starting with a letter or with illegal chars -> invalid")
        void shouldRejectInvalidUsernameFormat(String invalidUsername) {
            assertThrows(ValidationException.class, () -> validator.validateUsername(invalidUsername));
        }
    }

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "user@example.com",
                "john.doe@domain.co.uk",
                "tech_user+1@fixflow.org",
                "a.b.c@company.io"
        })
        @DisplayName("Valid email formats")
        void shouldAcceptValidEmails(String validEmail) {
            assertDoesNotThrow(() -> validator.validateEmail(validEmail));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
                "plainaddress",
                "@missingusername.com",
                "username@.com",
                "username@domain",
                "username@domain..com",
                "user space@domain.com"
        })
        @DisplayName("Invalid email formats")
        void shouldRejectInvalidEmails(String invalidEmail) {
            assertThrows(ValidationException.class, () -> validator.validateEmail(invalidEmail));
        }

        @Test
        @DisplayName("Email exceeding 100 characters -> invalid")
        void shouldRejectEmailExceedingMaxLength() {
            String longEmail = "a".repeat(95) + "@test.com";
            assertTrue(longEmail.length() > 100);
            assertThrows(ValidationException.class, () -> validator.validateEmail(longEmail));
        }
    }

    @Nested
    @DisplayName("Password Policy & Boundary Tests")
    class PasswordValidationTests {

        @Test
        @DisplayName("Valid passwords complying with security policy")
        void shouldAcceptValidPasswords() {
            assertDoesNotThrow(() -> validator.validatePassword("Passw0rd!"));
            assertDoesNotThrow(() -> validator.validatePassword("Strong#Pass123"));
            assertDoesNotThrow(() -> validator.validatePassword("P@ssw0rd2026"));
        }

        @Test
        @DisplayName("Boundary: min-1 (7 chars) -> invalid")
        void shouldRejectPasswordBelowMin() {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> validator.validatePassword("Pass1!a"));
            assertEquals(7, "Pass1!a".length());
            assertTrue(ex.getMessage().contains("at least 8 characters"));
        }

        @Test
        @DisplayName("Boundary: min (8 chars) -> valid")
        void shouldAcceptPasswordAtMin() {
            assertEquals(8, "Pass1!aa".length());
            assertDoesNotThrow(() -> validator.validatePassword("Pass1!aa"));
        }

        @Test
        @DisplayName("Boundary: min+1 (9 chars) -> valid")
        void shouldAcceptPasswordAboveMin() {
            assertEquals(9, "Pass1!aaa".length());
            assertDoesNotThrow(() -> validator.validatePassword("Pass1!aaa"));
        }

        @Test
        @DisplayName("Boundary: max (64 chars) -> valid")
        void shouldAcceptPasswordAtMax() {
            String pass64 = "Aa1!" + "b".repeat(60);
            assertEquals(64, pass64.length());
            assertDoesNotThrow(() -> validator.validatePassword(pass64));
        }

        @Test
        @DisplayName("Boundary: max+1 (65 chars) -> invalid")
        void shouldRejectPasswordAboveMax() {
            String pass65 = "Aa1!" + "b".repeat(61);
            assertEquals(65, pass65.length());
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> validator.validatePassword(pass65));
            assertTrue(ex.getMessage().contains("cannot exceed 64 characters"));
        }

        @ParameterizedTest(name = "Missing policy rule: {0}")
        @CsvSource({
                "password1!, Missing uppercase",
                "PASSWORD1!, Missing lowercase",
                "Password!!, Missing digit",
                "Password12, Missing special character"
        })
        void shouldRejectPasswordMissingPolicyRequirements(String password, String reason) {
            ValidationException ex = assertThrows(ValidationException.class,
                    () -> validator.validatePassword(password));
            assertTrue(ex.getMessage().contains("must contain at least one uppercase"));
        }
    }

    @Nested
    @DisplayName("Role and Status Validation")
    class RoleAndStatusValidationTests {

        @Test
        void shouldRejectNullRole() {
            assertThrows(ValidationException.class, () -> validator.validateRole(null));
        }

        @Test
        void shouldAcceptValidRoles() {
            assertDoesNotThrow(() -> validator.validateRole(Role.USER));
            assertDoesNotThrow(() -> validator.validateRole(Role.TECHNICIAN));
            assertDoesNotThrow(() -> validator.validateRole(Role.ADMIN));
        }

        @Test
        void shouldRejectNullStatus() {
            assertThrows(ValidationException.class, () -> validator.validateStatus(null));
        }

        @Test
        void shouldAcceptValidStatuses() {
            assertDoesNotThrow(() -> validator.validateStatus(UserStatus.ACTIVE));
            assertDoesNotThrow(() -> validator.validateStatus(UserStatus.INACTIVE));
        }
    }

    @Nested
    @DisplayName("Registration, Update & Entity Validation")
    class CompositeValidationTests {

        @Test
        void shouldValidateValidRegistrationPayload() {
            assertDoesNotThrow(() -> validator.validateRegistration(
                    "Ahmed Ali", "ahmed_ali", "ahmed@fixflow.com", "P@ssword1", Role.USER));
        }

        @Test
        void shouldRejectInvalidRegistrationPayload() {
            assertThrows(ValidationException.class, () -> validator.validateRegistration(
                    "", "ahmed_ali", "ahmed@fixflow.com", "P@ssword1", Role.USER));
        }

        @Test
        void shouldValidateValidUpdatePayload() {
            assertDoesNotThrow(() -> validator.validateUpdate(
                    1L, "Ahmed Ali", "ahmed_ali", "ahmed@fixflow.com", Role.ADMIN));
        }

        @Test
        void shouldRejectUpdateWithInvalidId() {
            assertThrows(ValidationException.class, () -> validator.validateUpdate(
                    null, "Ahmed Ali", "ahmed_ali", "ahmed@fixflow.com", Role.ADMIN));
            assertThrows(ValidationException.class, () -> validator.validateUpdate(
                    -1L, "Ahmed Ali", "ahmed_ali", "ahmed@fixflow.com", Role.ADMIN));
            assertThrows(ValidationException.class, () -> validator.validateUpdate(
                    0L, "Ahmed Ali", "ahmed_ali", "ahmed@fixflow.com", Role.ADMIN));
        }

        @Test
        void shouldValidateLoginCredentials() {
            assertDoesNotThrow(() -> validator.validateLoginCredentials("validUser", "ValidPass1!"));
            assertThrows(ValidationException.class, () -> validator.validateLoginCredentials("", "ValidPass1!"));
            assertThrows(ValidationException.class, () -> validator.validateLoginCredentials("validUser", ""));
        }

        @Test
        void shouldValidateCompleteUserEntity() {
            User validUser = User.builder()
                    .id(1L)
                    .fullName("Sara Connor")
                    .username("sara_c")
                    .email("sara@fixflow.com")
                    .passwordHash("10000:salt:hash")
                    .role(Role.TECHNICIAN)
                    .status(UserStatus.ACTIVE)
                    .build();

            assertDoesNotThrow(() -> validator.validateUser(validUser));
        }

        @Test
        void shouldRejectNullUserEntity() {
            assertThrows(ValidationException.class, () -> validator.validateUser(null));
        }
    }
}
