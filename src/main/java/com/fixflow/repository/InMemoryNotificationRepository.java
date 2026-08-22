package com.fixflow.repository;

import com.fixflow.model.Notification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Thread-safe In-Memory implementation of {@link NotificationRepository}.
 */
public class InMemoryNotificationRepository implements NotificationRepository {

    private final Map<Long, Notification> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Optional<Notification> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        Notification n = storage.get(id);
        return Optional.ofNullable(n != null ? new Notification(n) : null);
    }

    @Override
    public synchronized Notification save(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null");
        }
        Notification copy = new Notification(notification);
        if (copy.getId() == null) {
            copy.setId(idGenerator.getAndIncrement());
        }
        if (copy.getCreatedAt() == null) {
            copy.setCreatedAt(LocalDateTime.now());
        }
        storage.put(copy.getId(), copy);
        return new Notification(copy);
    }

    @Override
    public synchronized Notification update(Notification notification) {
        if (notification == null || notification.getId() == null) {
            throw new IllegalArgumentException("Notification and ID cannot be null");
        }
        if (!storage.containsKey(notification.getId())) {
            throw new IllegalArgumentException("Notification does not exist");
        }
        Notification copy = new Notification(notification);
        storage.put(copy.getId(), copy);
        return new Notification(copy);
    }

    @Override
    public List<Notification> findByRecipientId(Long recipientId) {
        if (recipientId == null) {
            return new ArrayList<>();
        }
        return storage.values().stream()
                .filter(n -> recipientId.equals(n.getRecipientId()))
                .map(Notification::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findUnreadByRecipientId(Long recipientId) {
        if (recipientId == null) {
            return new ArrayList<>();
        }
        return storage.values().stream()
                .filter(n -> recipientId.equals(n.getRecipientId()) && !n.isRead())
                .map(Notification::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findAll() {
        return storage.values().stream()
                .map(Notification::new)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized void clear() {
        storage.clear();
        idGenerator.set(1);
    }
}
