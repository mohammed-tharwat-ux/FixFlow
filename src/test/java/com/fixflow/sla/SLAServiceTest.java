package com.fixflow.sla;

import com.fixflow.model.Priority;
import com.fixflow.service.SLAService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SLAServiceTest {

    private SLAService slaService;

    @BeforeEach
    void setUp() {
        slaService = new SLAService();
    }

    @Test
    void testSlaMet_BoundaryJustBelowLimit() {
        LocalDateTime created = LocalDateTime.of(2026, 9, 1, 10, 0);
        LocalDateTime resolved = created.plusHours(7).plusMinutes(59);

        assertTrue(slaService.isSlaMet(Priority.HIGH, created, resolved));
    }

    @Test
    void testSlaMet_BoundaryExactLimit() {
        LocalDateTime created = LocalDateTime.of(2026, 9, 1, 10, 0);
        LocalDateTime resolved = created.plusHours(8);

        assertTrue(slaService.isSlaMet(Priority.HIGH, created, resolved));
    }

    @Test
    void testSlaMet_BoundaryJustOverLimit() {
        LocalDateTime created = LocalDateTime.of(2026, 9, 1, 10, 0);
        LocalDateTime resolved = created.plusHours(8).plusMinutes(1);

        assertFalse(slaService.isSlaMet(Priority.HIGH, created, resolved));
    }
}