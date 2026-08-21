package com.fixflow.repository;

import com.fixflow.model.Role;
import com.fixflow.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Thread-safe In-Memory implementation of {@link UserRepository}.
 * Employs defensive copying on read and write to prevent unwanted mutations.
 */
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        User user = storage.get(id);
        return Optional.ofNullable(user != null ? new User(user) : null);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username.trim()))
                .findFirst()
                .map(User::new);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email.trim()))
                .findFirst()
                .map(User::new);
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null) {
            return false;
        }
        return storage.values().stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username.trim()));
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        return storage.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email.trim()));
    }

    @Override
    public synchronized User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        User copy = new User(user);
        if (copy.getId() == null) {
            copy.setId(idGenerator.getAndIncrement());
        }
        if (copy.getCreatedAt() == null) {
            copy.setCreatedAt(LocalDateTime.now());
        }
        copy.setUpdatedAt(LocalDateTime.now());

        storage.put(copy.getId(), copy);
        return new User(copy);
    }

    @Override
    public synchronized User update(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and User ID cannot be null when updating");
        }
        if (!storage.containsKey(user.getId())) {
            throw new IllegalArgumentException("User with ID " + user.getId() + " does not exist");
        }

        User copy = new User(user);
        copy.setUpdatedAt(LocalDateTime.now());
        storage.put(copy.getId(), copy);
        return new User(copy);
    }

    @Override
    public synchronized void delete(Long id) {
        if (id != null) {
            storage.remove(id);
        }
    }

    @Override
    public List<User> findAll() {
        return storage.values().stream()
                .map(User::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findByRole(Role role) {
        if (role == null) {
            return new ArrayList<>();
        }
        return storage.values().stream()
                .filter(u -> u.getRole() == role)
                .map(User::new)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized void clear() {
        storage.clear();
        idGenerator.set(1);
    }
}
