package com.fixflow.validation;

import com.fixflow.exception.InvalidTicketStatusException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.Category;
import com.fixflow.model.Priority;
import com.fixflow.model.Role;
import com.fixflow.model.Ticket;
import com.fixflow.model.TicketStatus;
import com.fixflow.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TicketValidator Unit & Boundary Tests")
class TicketValidatorTest {

    private TicketValidator validator;
    private User reporter;

    @BeforeEach
    void setUp() {
        validator = new TicketValidator();
        reporter = User.builder().id(1L).fullName("Alice Smith").username("alice").email("alice@fixflow.com").role(Role.USER).build();
    }

    @Nested
    @DisplayName("Title Validation & Boundary Analysis")
    class TitleBoundaryTests {

        @Test
        @DisplayName("Boundary: min-1 (2 chars) -> invalid")
        void shouldRejectTitleBelowMin() {
            ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateTitle("AB"));
            assertTrue(ex.getMessage().contains("at least 3 characters"));
        }

        @Test
        @DisplayName("Boundary: min (3 chars) -> valid")
        void shouldAcceptTitleAtMin() {
            assertDoesNotThrow(() -> validator.validateTitle("ABC"));
        }

        @Test
        @DisplayName("Boundary: min+1 (4 chars) -> valid")
        void shouldAcceptTitleAboveMin() {
            assertDoesNotThrow(() -> validator.validateTitle("ABCD"));
        }

        @Test
        @DisplayName("Boundary: max-1 (99 chars) -> valid")
        void shouldAcceptTitleBelowMax() {
            String title99 = "T" + "a".repeat(98);
            assertEquals(99, title99.length());
            assertDoesNotThrow(() -> validator.validateTitle(title99));
        }

        @Test
        @DisplayName("Boundary: max (100 chars) -> valid")
        void shouldAcceptTitleAtMax() {
            String title100 = "T" + "a".repeat(99);
            assertEquals(100, title100.length());
            assertDoesNotThrow(() -> validator.validateTitle(title100));
        }

        @Test
        @DisplayName("Boundary: max+1 (101 chars) -> invalid")
        void shouldRejectTitleAboveMax() {
            String title101 = "T" + "a".repeat(100);
            assertEquals(101, title101.length());
            ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateTitle(title101));
            assertTrue(ex.getMessage().contains("cannot exceed 100 characters"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t"})
        void shouldRejectNullOrBlankTitle(String invalidTitle) {
            assertThrows(ValidationException.class, () -> validator.validateTitle(invalidTitle));
        }
    }

    @Nested
    @DisplayName("Description Validation & Boundary Analysis")
    class DescriptionBoundaryTests {

        @Test
        @DisplayName("Boundary: min-1 (4 chars) -> invalid")
        void shouldRejectDescriptionBelowMin() {
            ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateDescription("Four"));
            assertTrue(ex.getMessage().contains("at least 5 characters"));
        }

        @Test
        @DisplayName("Boundary: min (5 chars) -> valid")
        void shouldAcceptDescriptionAtMin() {
            assertDoesNotThrow(() -> validator.validateDescription("Five5"));
        }

        @Test
        @DisplayName("Boundary: max (1000 chars) -> valid")
        void shouldAcceptDescriptionAtMax() {
            String desc1000 = "D" + "a".repeat(999);
            assertEquals(1000, desc1000.length());
            assertDoesNotThrow(() -> validator.validateDescription(desc1000));
        }

        @Test
        @DisplayName("Boundary: max+1 (1001 chars) -> invalid")
        void shouldRejectDescriptionAboveMax() {
            String desc1001 = "D" + "a".repeat(1000);
            assertEquals(1001, desc1001.length());
            ValidationException ex = assertThrows(ValidationException.class, () -> validator.validateDescription(desc1001));
            assertTrue(ex.getMessage().contains("cannot exceed 1000 characters"));
        }
    }

    @Nested
    @DisplayName("Location Validation")
    class LocationValidationTests {

        @Test
        void shouldValidateLocationLengths() {
            assertThrows(ValidationException.class, () -> validator.validateLocation("A"));
            assertDoesNotThrow(() -> validator.validateLocation("Building A"));
            assertThrows(ValidationException.class, () -> validator.validateLocation("L".repeat(101)));
            assertThrows(ValidationException.class, () -> validator.validateLocation(""));
            assertThrows(ValidationException.class, () -> validator.validateLocation(null));
        }
    }

    @Nested
    @DisplayName("Lifecycle Status Transitions")
    class StatusTransitionTests {

        private Ticket createTicketWithStatus(TicketStatus status) {
            return Ticket.builder()
                    .id(10L)
                    .title("Sample")
                    .description("Sample description")
                    .category(Category.HARDWARE)
                    .location("Room 101")
                    .status(status)
                    .build();
        }

        @ParameterizedTest(name = "Valid transition from {0} to {1}")
        @CsvSource({
                "OPEN, ASSIGNED",
                "OPEN, CANCELLED",
                "ASSIGNED, IN_PROGRESS",
                "ASSIGNED, CANCELLED",
                "IN_PROGRESS, RESOLVED",
                "RESOLVED, CLOSED"
        })
        void shouldAllowValidTransitions(TicketStatus current, TicketStatus target) {
            Ticket t = createTicketWithStatus(current);
            assertDoesNotThrow(() -> validator.validateStatusTransition(t, target));
        }

        @ParameterizedTest(name = "Invalid transition from {0} to {1}")
        @CsvSource({
                "CLOSED, IN_PROGRESS",
                "CLOSED, OPEN",
                "CLOSED, RESOLVED",
                "CANCELLED, IN_PROGRESS",
                "CANCELLED, OPEN",
                "OPEN, CLOSED",
                "OPEN, RESOLVED",
                "ASSIGNED, CLOSED",
                "RESOLVED, OPEN",
                "RESOLVED, IN_PROGRESS"
        })
        void shouldRejectInvalidTransitions(TicketStatus current, TicketStatus target) {
            Ticket t = createTicketWithStatus(current);
            assertThrows(InvalidTicketStatusException.class, () -> validator.validateStatusTransition(t, target));
        }

        @Test
        void shouldRejectNullInputs() {
            assertThrows(ValidationException.class, () -> validator.validateStatusTransition(null, TicketStatus.ASSIGNED));
            assertThrows(ValidationException.class, () -> validator.validateStatusTransition(createTicketWithStatus(TicketStatus.OPEN), null));
        }
    }
}
