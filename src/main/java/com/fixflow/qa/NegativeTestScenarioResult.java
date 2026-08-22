package com.fixflow.qa;

/**
 * Encapsulates the execution result of an executable QA negative test scenario.
 */
public record NegativeTestScenarioResult(
        String id,
        String title,
        String description,
        String expectedResult,
        String actualResult,
        boolean passed
) {
}
