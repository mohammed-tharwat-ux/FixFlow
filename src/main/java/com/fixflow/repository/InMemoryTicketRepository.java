package com.fixflow.repository;

import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Thread-safe In-Memory implementation of {@link TicketRepository}.
 * Uses defensive copying to prevent unwanted external mutations.
 */
public class InMemoryTicketRepository implements TicketRepository {

    private final Map<Long, Ticket> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Optional<Ticket> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        Ticket ticket = storage.get(id);
        return Optional.ofNullable(ticket != null ? new Ticket(ticket) : null);
    }

    @Override
    public synchronized Ticket save(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null");
        }
        Ticket copy = new Ticket(ticket);
        if (copy.getId() == null) {
            copy.setId(idGenerator.getAndIncrement());
        }
        if (copy.getCreatedAt() == null) {
            copy.setCreatedAt(LocalDateTime.now());
        }
        copy.setUpdatedAt(LocalDateTime.now());

        storage.put(copy.getId(), copy);
        return new Ticket(copy);
    }

    @Override
    public synchronized Ticket update(Ticket ticket) {
        if (ticket == null || ticket.getId() == null) {
            throw new IllegalArgumentException("Ticket and Ticket ID cannot be null when updating");
        }
        if (!storage.containsKey(ticket.getId())) {
            throw new IllegalArgumentException("Ticket with ID " + ticket.getId() + " does not exist");
        }
        Ticket copy = new Ticket(ticket);
        copy.setUpdatedAt(LocalDateTime.now());
        storage.put(copy.getId(), copy);
        return new Ticket(copy);
    }

    @Override
    public synchronized void delete(Long id) {
        if (id != null) {
            storage.remove(id);
        }
    }

    @Override
    public List<Ticket> findAll() {
        return storage.values().stream()
                .map(Ticket::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByReporterId(Long reporterId) {
        if (reporterId == null) {
            return new ArrayList<>();
        }
        return storage.values().stream()
                .filter(t -> t.getReporter() != null && reporterId.equals(t.getReporter().getId()))
                .map(Ticket::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByTechnicianId(Long technicianId) {
        if (technicianId == null) {
            return new ArrayList<>();
        }
        return storage.values().stream()
                .filter(t -> t.getAssignedTechnician() != null && technicianId.equals(t.getAssignedTechnician().getId()))
                .map(Ticket::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByStatus(TicketStatus status) {
        if (status == null) {
            return new ArrayList<>();
        }
        return storage.values().stream()
                .filter(t -> t.getStatus() == status)
                .map(Ticket::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByCategory(Category category) {
        if (category == null) {
            return new ArrayList<>();
        }
        return storage.values().stream()
                .filter(t -> t.getCategory() == category)
                .map(Ticket::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findByPriority(Priority priority) {
        if (priority == null) {
            return new ArrayList<>();
        }
        return storage.values().stream()
                .filter(t -> t.getPriority() == priority)
                .map(Ticket::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return findAll();
        }
        String lower = keyword.trim().toLowerCase();
        return storage.values().stream()
                .filter(t -> (t.getTitle() != null && t.getTitle().toLowerCase().contains(lower))
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(lower))
                        || (t.getLocation() != null && t.getLocation().toLowerCase().contains(lower)))
                .map(Ticket::new)
                .collect(Collectors.toList());
    }

    @Override
    public synchronized void clear() {
        storage.clear();
        idGenerator.set(1);
    }
}
