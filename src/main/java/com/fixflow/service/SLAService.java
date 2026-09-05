package com.fixflow.service;

import com.fixflow.model.Priority;
import java.time.Duration;
import java.time.LocalDateTime;

public class SLAService {

    public boolean isSlaMet(Priority priority, LocalDateTime createdAt, LocalDateTime resolvedAt) {
        if (createdAt == null || resolvedAt == null || priority == null) {
            throw new IllegalArgumentException("Inputs cannot be null");
        }

        if (resolvedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("Resolved time cannot be before created time");
        }

        // حساب الوقت المستغرق بالدقائق لضمان الدقة العالية في الـ Boundary Analysis
        long minutesTaken = Duration.between(createdAt, resolvedAt).toMinutes();
        long allowedMinutes = priority.getTargetHours() * 60L;

        return minutesTaken <= allowedMinutes;
    }
}