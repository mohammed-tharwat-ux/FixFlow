package com.fixflow.model;

import java.time.LocalDateTime;

/**
 * Feedback and rating submitted by a user for a resolved/closed maintenance ticket.
 */
public class Feedback {

    private Long id;
    private Long ticketId;
    private Long userId;
    private int rating; // 1 to 5
    private String comment;
    private LocalDateTime createdAt;

    public Feedback() {
    }

    public Feedback(Long id, Long ticketId, Long userId, int rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Feedback(Feedback other) {
        if (other != null) {
            this.id = other.id;
            this.ticketId = other.ticketId;
            this.userId = other.userId;
            this.rating = other.rating;
            this.comment = other.comment;
            this.createdAt = other.createdAt;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "id=" + id +
                ", ticketId=" + ticketId +
                ", userId=" + userId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
