package com.fixflow.service;

import com.fixflow.exception.UserAlreadyExistsException;
import com.fixflow.exception.UserNotFoundException;
import com.fixflow.exception.ValidationException;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserService Unit & Business Logic Tests")
class UserServiceTest {

    private UserRepository userRepository;
    private UserValidator userValidator;
    private PasswordHasher passwordHasher;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userValidator = new UserValidator();
        passwordHasher = new PBKDF2PasswordHasher();
        userService = new UserService(userRepository, userValidator, passwordHasher);
    }

    @Nested
    @DisplayName("User Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Successfully register a standard user")
        void shouldRegisterValidUser() {
            User user = userService.registerUser(
                    "Ahmed Ali", "ahmed_ali", "ahmed@example.com", "P@ssw0rd123", Role.USER);

            assertNotNull(user.getId());
            assertEquals("Ahmed Ali", user.getFullName());
            assertEquals("ahmed_ali", user.getUsername());
            assertEquals("ahmed@example.com", user.getEmail());
            assertEquals(Role.USER, user.getRole());
            assertEquals(UserStatus.ACTIVE, user.getStatus());
            assertNotNull(user.getPasswordHash());
            assertTrue(passwordHasher.matches("P@ssw0rd123", user.getPasswordHash()));
        }

        @Test
        @DisplayName("Successfully register technician and admin roles")
        void shouldRegisterDifferentRoles() {
            User tech = userService.registerUser(
                    "Tech Bob", "tech_bob", "bob@fixflow.com", "P@ssw0rd123", Role.TECHNICIAN);
            User admin = userService.registerUser(
                    "Admin Alice", "admin_alice", "alice@fixflow.com", "P@ssw0rd123", Role.ADMIN);

            assertEquals(Role.TECHNICIAN, tech.getRole());
            assertEquals(Role.ADMIN, admin.getRole());
        }

        @Test
        @DisplayName("Register with duplicate username throws UserAlreadyExistsException")
        void shouldThrowExceptionOnDuplicateUsername() {
            userService.registerUser("User One", "unique_user", "user1@example.com", "P@ssw0rd123", Role.USER);

            UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class, () ->
                    userService.registerUser("User Two", "unique_user", "user2@example.com", "P@ssw0rd123", Role.USER));

            assertTrue(ex.getMessage().contains("already exists"));
        }

        @Test
        @DisplayName("Register with duplicate email throws UserAlreadyExistsException")
        void shouldThrowExceptionOnDuplicateEmail() {
            userService.registerUser("User One", "user_one", "duplicate@example.com", "P@ssw0rd123", Role.USER);

            UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class, () ->
                    userService.registerUser("User Two", "user_two", "DUPLICATE@example.com", "P@ssw0rd123", Role.USER));

            assertTrue(ex.getMessage().contains("already exists"));
        }

        @Test
        @DisplayName("Register with invalid payload throws ValidationException")
        void shouldThrowValidationExceptionOnInvalidData() {
            assertThrows(ValidationException.class, () ->
                    userService.registerUser("", "user_one", "user@example.com", "P@ssw0rd123", Role.USER));

            assertThrows(ValidationException.class, () ->
                    userService.registerUser("User One", "u", "user@example.com", "P@ssw0rd123", Role.USER));

            assertThrows(ValidationException.class, () ->
                    userService.registerUser("User One", "user_one", "invalid-email", "P@ssw0rd123", Role.USER));

            assertThrows(ValidationException.class, () ->
                    userService.registerUser("User One", "user_one", "user@example.com", "weak", Role.USER));

            assertThrows(ValidationException.class, () ->
                    userService.registerUser("User One", "user_one", "user@example.com", "P@ssw0rd123", null));
        }
    }

    @Nested
    @DisplayName("User Retrieval Tests")
    class RetrievalTests {

        private User registeredUser;

        @BeforeEach
        void registerInitialUser() {
            registeredUser = userService.registerUser(
                    "Test User", "test_user", "test@fixflow.com", "P@ssw0rd123", Role.USER);
        }

        @Test
        @DisplayName("Get user by existing ID")
        void shouldGetUserByExistingId() {
            User found = userService.getUserById(registeredUser.getId());
            assertEquals(registeredUser.getId(), found.getId());
            assertEquals("test_user", found.getUsername());
        }

        @Test
        @DisplayName("Get user by non-existing ID throws UserNotFoundException")
        void shouldThrowExceptionOnNonExistingId() {
            assertThrows(UserNotFoundException.class, () -> userService.getUserById(9999L));
            assertThrows(ValidationException.class, () -> userService.getUserById(null));
        }

        @Test
        @DisplayName("Get user by username")
        void shouldGetUserByUsername() {
            User found = userService.getUserByUsername("test_user");
            assertEquals(registeredUser.getId(), found.getId());

            assertThrows(UserNotFoundException.class, () -> userService.getUserByUsername("unknown"));
            assertThrows(ValidationException.class, () -> userService.getUserByUsername(""));
        }

        @Test
        @DisplayName("Get user by email")
        void shouldGetUserByEmail() {
            User found = userService.getUserByEmail("test@fixflow.com");
            assertEquals(registeredUser.getId(), found.getId());

            assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail("unknown@fixflow.com"));
            assertThrows(ValidationException.class, () -> userService.getUserByEmail(""));
        }
    }

    @Nested
    @DisplayName("User Update Tests")
    class UpdateTests {

        private User user1;
        private User user2;

        @BeforeEach
        void prepareUsers() {
            user1 = userService.registerUser("User One", "user_one", "user1@fixflow.com", "P@ssw0rd123", Role.USER);
            user2 = userService.registerUser("User Two", "user_two", "user2@fixflow.com", "P@ssw0rd123", Role.TECHNICIAN);
        }

        @Test
        @DisplayName("Successfully update profile information")
        void shouldUpdateUserProfile() {
            User updated = userService.updateUser(
                    user1.getId(), "User One Updated", "user_one_new", "user1_new@fixflow.com", Role.ADMIN);

            assertEquals("User One Updated", updated.getFullName());
            assertEquals("user_one_new", updated.getUsername());
            assertEquals("user1_new@fixflow.com", updated.getEmail());
            assertEquals(Role.ADMIN, updated.getRole());
        }

        @Test
        @DisplayName("Update with conflicting username throws UserAlreadyExistsException")
        void shouldThrowExceptionWhenUpdatingToExistingUsername() {
            assertThrows(UserAlreadyExistsException.class, () ->
                    userService.updateUser(user1.getId(), "User One", "user_two", "user1@fixflow.com", Role.USER));
        }

        @Test
        @DisplayName("Update with conflicting email throws UserAlreadyExistsException")
        void shouldThrowExceptionWhenUpdatingToExistingEmail() {
            assertThrows(UserAlreadyExistsException.class, () ->
                    userService.updateUser(user1.getId(), "User One", "user_one", "user2@fixflow.com", Role.USER));
        }

        @Test
        @DisplayName("Update non-existing user throws UserNotFoundException")
        void shouldThrowExceptionWhenUpdatingNonExistingUser() {
            assertThrows(UserNotFoundException.class, () ->
                    userService.updateUser(8888L, "Ghost", "ghost_user", "ghost@fixflow.com", Role.USER));
        }
    }

    @Nested
    @DisplayName("Activation, Deactivation & Deletion Tests")
    class StatusAndLifecycleTests {

        private User user;

        @BeforeEach
        void prepareUser() {
            user = userService.registerUser("Active User", "active_user", "active@fixflow.com", "P@ssw0rd123", Role.USER);
        }

        @Test
        @DisplayName("Deactivate and reactivate existing user")
        void shouldDeactivateAndActivateUser() {
            assertEquals(UserStatus.ACTIVE, user.getStatus());

            userService.deactivateUser(user.getId());
            User deactivated = userService.getUserById(user.getId());
            assertEquals(UserStatus.INACTIVE, deactivated.getStatus());
            assertFalse(deactivated.isActive());

            userService.activateUser(user.getId());
            User reactivated = userService.getUserById(user.getId());
            assertEquals(UserStatus.ACTIVE, reactivated.getStatus());
            assertTrue(reactivated.isActive());
        }

        @Test
        @DisplayName("Deactivate or activate non-existing user throws UserNotFoundException")
        void shouldThrowExceptionWhenActivatingNonExistingUser() {
            assertThrows(UserNotFoundException.class, () -> userService.deactivateUser(9999L));
            assertThrows(UserNotFoundException.class, () -> userService.activateUser(9999L));
        }

        @Test
        @DisplayName("Delete user removes them completely")
        void shouldDeleteUser() {
            userService.deleteUser(user.getId());
            assertThrows(UserNotFoundException.class, () -> userService.getUserById(user.getId()));
        }

        @Test
        @DisplayName("Delete non-existing user throws UserNotFoundException")
        void shouldThrowExceptionWhenDeletingNonExistingUser() {
            assertThrows(UserNotFoundException.class, () -> userService.deleteUser(9999L));
        }
    }

    @Nested
    @DisplayName("Query & Role Listing Tests")
    class QueryTests {

        @Test
        @DisplayName("List all users and filter by role")
        void shouldListAllAndFilterByRole() {
            userService.registerUser("User One", "user_1", "user1@fixflow.com", "P@ssw0rd123", Role.USER);
            userService.registerUser("User Two", "user_2", "user2@fixflow.com", "P@ssw0rd123", Role.USER);
            userService.registerUser("Tech Bob", "tech_1", "tech1@fixflow.com", "P@ssw0rd123", Role.TECHNICIAN);
            userService.registerUser("Admin Alice", "admin_1", "admin1@fixflow.com", "P@ssw0rd123", Role.ADMIN);

            List<User> all = userService.listUsers();
            assertEquals(4, all.size());

            List<User> techs = userService.getUsersByRole(Role.TECHNICIAN);
            assertEquals(1, techs.size());
            assertEquals("tech_1", techs.get(0).getUsername());

            List<User> admins = userService.getUsersByRole(Role.ADMIN);
            assertEquals(1, admins.size());

            assertThrows(ValidationException.class, () -> userService.getUsersByRole(null));
        }
    }
}
