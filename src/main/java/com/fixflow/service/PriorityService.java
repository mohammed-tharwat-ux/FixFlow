package com.fixflow.service;

import com.fixflow.exception.ValidationException;
import com.fixflow.model.Category;
import com.fixflow.model.Priority;

import java.util.List;
import java.util.Set;

/**
 * Service managing priority determination, intelligent suggestion rules, and validation.
 */
public class PriorityService {

    private static final Set<String> CRITICAL_KEYWORDS = Set.of(
            "emergency", "danger", "hazard", "fire", "outage", "spark", "explosion", "blackout", "smoke"
    );

    private static final Set<String> HIGH_KEYWORDS = Set.of(
            "broken", "leak", "failure", "urgent", "blocked", "stopped", "security", "server"
    );

    /**
     * Determines an appropriate priority based on category and textual description.
     */
    public Priority evaluatePriority(Category category, String title, String description, Priority explicitPriority) {
        if (explicitPriority != null) {
            return explicitPriority;
        }

        if (category == null) {
            throw new ValidationException("Category cannot be null for priority evaluation");
        }

        String combinedText = ((title != null ? title : "") + " " + (description != null ? description : "")).toLowerCase();

        for (String keyword : CRITICAL_KEYWORDS) {
            if (combinedText.contains(keyword)) {
                return Priority.CRITICAL;
            }
        }

        return switch (category) {
            case ELECTRICAL -> Priority.HIGH;
            case NETWORK, HARDWARE -> Priority.MEDIUM;
            case SOFTWARE, FACILITY, OTHER -> Priority.LOW;
        };
    }

    public List<Priority> getAllPriorities() {
        return List.of(Priority.values());
    }
}
