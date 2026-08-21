package com.fixflow.repository;

import com.fixflow.model.Role;
import com.fixflow.model.User;
import com.fixflow.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryUserRepository Tests")
class UserRepositoryTest {

    private UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        repository.clear();
    }

    private User createSampleUser(String username, String email, Role role) {
        return User.builder()
                .fullName("Test User")
                .username(username)
                .email(email)
                .passwordHash("10000:salt:hash")
                .role(role)
                .status(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("Save user assigns ID and timestamps")
    void shouldSaveUserAndAssignId() {
        User user = createSampleUser("john_doe", "john@example.com", Role.USER);
        User saved = repository.save(user);

        assertNotNull(saved.getId());
        assertEquals("john_doe", saved.getUsername());
        assertEquals("john@example.com", saved.getEmail());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }

    @Test
    @DisplayName("Find user by ID - existing and non-existing")
    void shouldFindUserById() {
        User saved = repository.save(createSampleUser("john_doe", "john@example.com", Role.USER));

        Optional<User> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());

        Optional<User> notFound = repository.findById(999L);
        assertFalse(notFound.isPresent());

        assertFalse(repository.findById(null).isPresent());
    }

    @Test
    @DisplayName("Find user by username (case-insensitive)")
    void shouldFindUserByUsernameCaseInsensitive() {
        repository.save(createSampleUser("john_doe", "john@example.com", Role.USER));

        Optional<User> found = repository.findByUsername("JOHN_DOE");
        assertTrue(found.isPresent());
        assertEquals("john_doe", found.get().getUsername());

        Optional<User> notFound = repository.findByUsername("non_existent");
        assertFalse(notFound.isPresent());

        assertFalse(repository.findByUsername(null).isPresent());
    }

    @Test
    @DisplayName("Find user by email (case-insensitive)")
    void shouldFindUserByEmailCaseInsensitive() {
        repository.save(createSampleUser("john_doe", "john@example.com", Role.USER));

        Optional<User> found = repository.findByEmail("JOHN@EXAMPLE.COM");
        assertTrue(found.isPresent());
        assertEquals("john@example.com", found.get().getEmail());

        Optional<User> notFound = repository.findByEmail("other@example.com");
        assertFalse(notFound.isPresent());

        assertFalse(repository.findByEmail(null).isPresent());
    }

    @Test
    @DisplayName("Check existence by username and email")
    void shouldCheckExistsByUsernameAndEmail() {
        repository.save(createSampleUser("john_doe", "john@example.com", Role.USER));

        assertTrue(repository.existsByUsername("john_doe"));
        assertTrue(repository.existsByUsername("JOHN_DOE"));
        assertFalse(repository.existsByUsername("unknown_user"));
        assertFalse(repository.existsByUsername(null));

        assertTrue(repository.existsByEmail("john@example.com"));
        assertTrue(repository.existsByEmail("JOHN@EXAMPLE.COM"));
        assertFalse(repository.existsByEmail("unknown@example.com"));
        assertFalse(repository.existsByEmail(null));
    }

    @Test
    @DisplayName("Update existing user")
    void shouldUpdateExistingUser() {
        User saved = repository.save(createSampleUser("john_doe", "john@example.com", Role.USER));
        LocalDateTime originalUpdatedAt = saved.getUpdatedAt();

        saved.setFullName("John Updated");
        saved.setRole(Role.ADMIN);
        User updated = repository.update(saved);

        assertEquals("John Updated", updated.getFullName());
        assertEquals(Role.ADMIN, updated.getRole());
        assertTrue(updated.getUpdatedAt().isEqual(originalUpdatedAt) || updated.getUpdatedAt().isAfter(originalUpdatedAt));

        Optional<User> fetched = repository.findById(saved.getId());
        assertTrue(fetched.isPresent());
        assertEquals("John Updated", fetched.get().getFullName());
    }

    @Test
    @DisplayName("Update non-existing user throws IllegalArgumentException")
    void shouldThrowExceptionWhenUpdatingNonExistingUser() {
        User nonExisting = createSampleUser("ghost", "ghost@example.com", Role.USER);
        nonExisting.setId(9999L);

        assertThrows(IllegalArgumentException.class, () -> repository.update(nonExisting));
        assertThrows(IllegalArgumentException.class, () -> repository.update(null));
    }

    @Test
    @DisplayName("Delete user by ID")
    void shouldDeleteUser() {
        User saved = repository.save(createSampleUser("john_doe", "john@example.com", Role.USER));

        repository.delete(saved.getId());
        Optional<User> found = repository.findById(saved.getId());
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Find all users and find by role")
    void shouldFindAllAndFindByRole() {
        repository.save(createSampleUser("user1", "user1@example.com", Role.USER));
        repository.save(createSampleUser("tech1", "tech1@fixflow.com", Role.TECHNICIAN));
        repository.save(createSampleUser("tech2", "tech2@fixflow.com", Role.TECHNICIAN));
        repository.save(createSampleUser("admin1", "admin1@fixflow.com", Role.ADMIN));

        List<User> all = repository.findAll();
        assertEquals(4, all.size());

        List<User> technicians = repository.findByRole(Role.TECHNICIAN);
        assertEquals(2, technicians.size());

        List<User> admins = repository.findByRole(Role.ADMIN);
        assertEquals(1, admins.size());

        List<User> users = repository.findByRole(Role.USER);
        assertEquals(1, users.size());

        assertTrue(repository.findByRole(null).isEmpty());
    }

    @Test
    @DisplayName("Defensive copying ensures repository internal state cannot be mutated externally")
    void shouldEnsureDefensiveCopying() {
        User original = createSampleUser("john_doe", "john@example.com", Role.USER);
        User saved = repository.save(original);

        // Mutating the returned object should not affect the stored object
        saved.setFullName("Mutated Name");
        Optional<User> fetched = repository.findById(saved.getId());
        assertTrue(fetched.isPresent());
        assertEquals("Test User", fetched.get().getFullName());
    }
}
