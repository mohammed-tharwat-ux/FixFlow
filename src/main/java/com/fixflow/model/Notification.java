package com.fixflow.model;

import java.time.LocalDateTime;

/**
 * In-system notification for users and technicians.
 */
public class Notification {

    private Long id;
    private Long recipientId;
    private String title;
    private String message;
    private Long ticketId;
    private NotificationType type;
    private LocalDateTime createdAt;
    private boolean isRead;

    public Notification() {
    }

    public Notification(Long id, Long recipientId, String title, String message,
                        Long ticketId, NotificationType type, LocalDateTime createdAt, boolean isRead) {
        this.id = id;
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
        this.ticketId = ticketId;
        this.type = type;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.isRead = isRead;
    }

    public Notification(Notification other) {
        if (other != null) {
            this.id = other.id;
            this.recipientId = other.recipientId;
            this.title = other.title;
            this.message = other.message;
            this.ticketId = other.ticketId;
            this.type = other.type;
            this.createdAt = other.createdAt;
            this.isRead = other.isRead;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", recipientId=" + recipientId +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", ticketId=" + ticketId +
                ", isRead=" + isRead +
                '}';
    }
}
