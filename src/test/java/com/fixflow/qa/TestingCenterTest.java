package com.fixflow.qa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TestingCenter & QA Execution Tests")
class TestingCenterTest {

    private TestingCenterService testingCenterService;

    @BeforeEach
    void setUp() {
        testingCenterService = new TestingCenterService();
    }

    @Test
    @DisplayName("Execute all 10 live negative test scenarios and ensure 100% pass rate")
    void shouldExecuteAllNegativeScenariosSuccessfully() {
        List<NegativeTestScenarioResult> results = testingCenterService.runNegativeScenarios();

        assertNotNull(results);
        assertEquals(10, results.size(), "There should be exactly 10 real negative test scenarios");

        for (NegativeTestScenarioResult result : results) {
            assertTrue(result.passed(), "Scenario " + result.id() + " (" + result.title() + ") failed: " + result.actualResult());
            assertNotNull(result.id());
            assertNotNull(result.title());
            assertNotNull(result.expectedResult());
            assertNotNull(result.actualResult());
        }
    }

    @Test
    @DisplayName("Verify that specific negative scenarios identify exact exceptions")
    void shouldVerifySpecificNegativeScenarioExceptions() {
        List<NegativeTestScenarioResult> results = testingCenterService.runNegativeScenarios();

        NegativeTestScenarioResult loginFailure = results.stream()
                .filter(r -> "TC-NEG-001".equals(r.id()))
                .findFirst()
                .orElseThrow();
        assertTrue(loginFailure.actualResult().contains("AuthenticationException"));

        NegativeTestScenarioResult duplicateUser = results.stream()
                .filter(r -> "TC-NEG-002".equals(r.id()))
                .findFirst()
                .orElseThrow();
        assertTrue(duplicateUser.actualResult().contains("UserAlreadyExistsException"));

        NegativeTestScenarioResult invalidTransition = results.stream()
                .filter(r -> "TC-NEG-008".equals(r.id()))
                .findFirst()
                .orElseThrow();
        assertTrue(invalidTransition.actualResult().contains("InvalidTicketStatusException"));
    }
}
