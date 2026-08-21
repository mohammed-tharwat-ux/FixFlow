package com.fixflow.service;

import com.fixflow.exception.AuthenticationException;
import com.fixflow.model.Role;
import com.fixflow.model.User;
import com.fixflow.model.UserStatus;
import com.fixflow.repository.InMemoryUserRepository;
import com.fixflow.repository.UserRepository;
import com.fixflow.security.PBKDF2PasswordHasher;
import com.fixflow.security.PasswordHasher;
import com.fixflow.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthenticationService Tests")
class AuthenticationServiceTest {

    private UserRepository userRepository;
    private PasswordHasher passwordHasher;
    private UserValidator userValidator;
    private UserService userService;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        passwordHasher = new PBKDF2PasswordHasher();
        userValidator = new UserValidator();
        userService = new UserService(userRepository, userValidator, passwordHasher);
        authenticationService = new AuthenticationService(userRepository, passwordHasher, userValidator);
    }

    @Test
    @DisplayName("Valid credentials should authenticate active user successfully")
    void shouldLoginSuccessfullyWithValidCredentials() {
        userService.registerUser("Sara Connor", "sara_c", "sara@fixflow.com", "P@ssw0rd123", Role.USER);

        User authenticated = authenticationService.login("sara_c", "P@ssw0rd123");

        assertNotNull(authenticated);
        assertEquals("sara_c", authenticated.getUsername());
        assertEquals("sara@fixflow.com", authenticated.getEmail());
        assertEquals(UserStatus.ACTIVE, authenticated.getStatus());
    }

    @Test
    @DisplayName("Authentication with wrong password throws AuthenticationException")
    void shouldFailLoginWithIncorrectPassword() {
        userService.registerUser("Sara Connor", "sara_c", "sara@fixflow.com", "P@ssw0rd123", Role.USER);

        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                authenticationService.login("sara_c", "WrongPassword123!"));

        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    @DisplayName("Authentication with unknown username throws AuthenticationException")
    void shouldFailLoginWithUnknownUsername() {
        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                authenticationService.login("unknown_user", "P@ssw0rd123"));

        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    @DisplayName("Authentication for inactive user throws AuthenticationException")
    void shouldFailLoginForInactiveAccount() {
        User user = userService.registerUser("Sara Connor", "sara_c", "sara@fixflow.com", "P@ssw0rd123", Role.USER);
        userService.deactivateUser(user.getId());

        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                authenticationService.login("sara_c", "P@ssw0rd123"));

        assertTrue(ex.getMessage().contains("inactive"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Login with empty username throws AuthenticationException")
    void shouldFailLoginWithBlankUsername(String invalidUsername) {
        assertThrows(AuthenticationException.class, () ->
                authenticationService.login(invalidUsername, "P@ssw0rd123"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Login with empty password throws AuthenticationException")
    void shouldFailLoginWithBlankPassword(String invalidPassword) {
        assertThrows(AuthenticationException.class, () ->
                authenticationService.login("sara_c", invalidPassword));
    }

    @Test
    @DisplayName("Logout valid and invalid usernames")
    void shouldHandleLogout() {
        assertDoesNotThrow(() -> authenticationService.logout("sara_c"));
        assertThrows(AuthenticationException.class, () -> authenticationService.logout(""));
        assertThrows(AuthenticationException.class, () -> authenticationService.logout(null));
    }
}
