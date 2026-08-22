package com.fixflow.repository;

import com.fixflow.model.Feedback;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Thread-safe In-Memory implementation of {@link FeedbackRepository}.
 */
public class InMemoryFeedbackRepository implements FeedbackRepository {

    private final Map<Long, Feedback> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Optional<Feedback> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        Feedback f = storage.get(id);
        return Optional.ofNullable(f != null ? new Feedback(f) : null);
    }

    @Override
    public Optional<Feedback> findByTicketId(Long ticketId) {
        if (ticketId == null) {
            return Optional.empty();
        }
        return storage.values().stream()
                .filter(f -> ticketId.equals(f.getTicketId()))
                .findFirst()
                .map(Feedback::new);
    }

    @Override
    public synchronized Feedback save(Feedback feedback) {
        if (feedback == null) {
            throw new IllegalArgumentException("Feedback cannot be null");
        }
        Feedback copy = new Feedback(feedback);
        if (copy.getId() == null) {
            copy.setId(idGenerator.getAndIncrement());
        }
        if (copy.getCreatedAt() == null) {
            copy.setCreatedAt(LocalDateTime.now());
        }
        storage.put(copy.getId(), copy);
        return new Feedback(copy);
    }

    @Override
    public List<Feedback> findAll() {
        return storage.values().stream()
                .map(Feedback::new)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized void clear() {
        storage.clear();
        idGenerator.set(1);
    }
}
