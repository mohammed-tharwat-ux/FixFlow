package com.fixflow.service;

import com.fixflow.exception.ValidationException;
import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PriorityService Tests")
class PriorityServiceTest {

    private PriorityService priorityService;

    @BeforeEach
    void setUp() {
        priorityService = new PriorityService();
    }

    @Test
    @DisplayName("Explicit priority overrides automatic calculation")
    void shouldRespectExplicitPriority() {
        Priority evaluated = priorityService.evaluatePriority(Category.FACILITY, "Minor paint scratch", "Normal wall scratch", Priority.HIGH);
        assertEquals(Priority.HIGH, evaluated);
    }

    @ParameterizedTest(name = "Keyword ''{0}'' in text triggers CRITICAL priority")
    @CsvSource({
            "fire, Fire in battery backup unit",
            "outage, Campus wide network outage",
            "emergency, Emergency evacuation alarm sounding",
            "hazard, Chemical spill hazard in chemistry lab"
    })
    void shouldAssignCriticalForUrgentKeywords(String keyword, String title) {
        Priority p = priorityService.evaluatePriority(Category.FACILITY, title, "Urgent issue", null);
        assertEquals(Priority.CRITICAL, p);
    }

    @Test
    @DisplayName("Electrical category defaults to HIGH")
    void shouldDefaultElectricalToHigh() {
        Priority p = priorityService.evaluatePriority(Category.ELECTRICAL, "Routine light check", "Checking bulbs", null);
        assertEquals(Priority.HIGH, p);
    }

    @Test
    @DisplayName("Category cannot be null")
    void shouldRejectNullCategory() {
        assertThrows(ValidationException.class, () ->
                priorityService.evaluatePriority(null, "Title", "Desc", null));
    }
}
