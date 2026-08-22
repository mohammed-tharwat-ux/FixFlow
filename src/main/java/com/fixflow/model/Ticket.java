package com.fixflow.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Maintenance Ticket entity representing an incident or service request.
 */
public class Ticket {

    private Long id;
    private User reporter;
    private String title;
    private String description;
    private Category category;
    private String location;
    private Priority priority;
    private TicketStatus status;
    private User assignedTechnician;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private String resolutionNotes;

    public Ticket() {
    }

    public Ticket(Long id, User reporter, String title, String description, Category category,
                  String location, Priority priority, TicketStatus status, User assignedTechnician,
                  LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime resolvedAt,
                  LocalDateTime closedAt, String resolutionNotes) {
        this.id = id;
        this.reporter = reporter != null ? new User(reporter) : null;
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.status = status != null ? status : TicketStatus.OPEN;
        this.assignedTechnician = assignedTechnician != null ? new User(assignedTechnician) : null;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
        this.resolvedAt = resolvedAt;
        this.closedAt = closedAt;
        this.resolutionNotes = resolutionNotes;
    }

    /**
     * Copy constructor for defensive copying in repositories.
     */
    public Ticket(Ticket other) {
        if (other != null) {
            this.id = other.id;
            this.reporter = other.reporter != null ? new User(other.reporter) : null;
            this.title = other.title;
            this.description = other.description;
            this.category = other.category;
            this.location = other.location;
            this.priority = other.priority;
            this.status = other.status;
            this.assignedTechnician = other.assignedTechnician != null ? new User(other.assignedTechnician) : null;
            this.createdAt = other.createdAt;
            this.updatedAt = other.updatedAt;
            this.resolvedAt = other.resolvedAt;
            this.closedAt = other.closedAt;
            this.resolutionNotes = other.resolutionNotes;
        }
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getReporter() {
        return reporter != null ? new User(reporter) : null;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter != null ? new User(reporter) : null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public User getAssignedTechnician() {
        return assignedTechnician != null ? new User(assignedTechnician) : null;
    }

    public void setAssignedTechnician(User assignedTechnician) {
        this.assignedTechnician = assignedTechnician != null ? new User(assignedTechnician) : null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    // --- Builder ---

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private User reporter;
        private String title;
        private String description;
        private Category category;
        private String location;
        private Priority priority = Priority.MEDIUM;
        private TicketStatus status = TicketStatus.OPEN;
        private User assignedTechnician;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt = LocalDateTime.now();
        private LocalDateTime resolvedAt;
        private LocalDateTime closedAt;
        private String resolutionNotes;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder reporter(User reporter) {
            this.reporter = reporter;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder priority(Priority priority) {
            this.priority = priority;
            return this;
        }

        public Builder status(TicketStatus status) {
            this.status = status;
            return this;
        }

        public Builder assignedTechnician(User assignedTechnician) {
            this.assignedTechnician = assignedTechnician;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder resolvedAt(LocalDateTime resolvedAt) {
            this.resolvedAt = resolvedAt;
            return this;
        }

        public Builder closedAt(LocalDateTime closedAt) {
            this.closedAt = closedAt;
            return this;
        }

        public Builder resolutionNotes(String resolutionNotes) {
            this.resolutionNotes = resolutionNotes;
            return this;
        }

        public Ticket build() {
            return new Ticket(id, reporter, title, description, category, location, priority,
                    status, assignedTechnician, createdAt, updatedAt, resolvedAt, closedAt, resolutionNotes);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return Objects.equals(id, ticket.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", location='" + location + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", reporter=" + (reporter != null ? reporter.getUsername() : "null") +
                ", assignedTechnician=" + (assignedTechnician != null ? assignedTechnician.getUsername() : "Unassigned") +
                ", createdAt=" + createdAt +
                ", resolvedAt=" + resolvedAt +
                ", closedAt=" + closedAt +
                '}';
    }
}
