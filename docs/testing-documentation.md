# FixFlow — Software Testing & Quality Assurance Documentation

## 1. Test Plan Overview
The testing strategy for FixFlow is designed to guarantee high reliability, data integrity, and strict adherence to maintenance business rules through multiple layers:
1. **Unit Testing**: Testing individual service methods, validators, security hashers, repositories, UI styling components, and testing center services.
2. **Boundary Value Analysis (BVA)**: Evaluating minimum, maximum, below-min, above-max boundaries for user inputs, SLA timelines, and rating scales.
3. **Equivalence Partitioning (EP)**: Valid vs. invalid partitions for usernames, full names, emails, passwords, categories, roles, and status transitions.
4. **Negative & Exception Testing**: Explicit verification that invalid operations throw designated domain exceptions (`ValidationException`, `UserNotFoundException`, `InvalidTicketStatusException`, `UnauthorizedOperationException`, `AuthenticationException`).
5. **End-to-End Integration Testing**: Verifying the complete multi-role workflow from registration to ticket closure, feedback, and executive reporting.
6. **Regression Testing**: Ensuring identified edge cases (e.g. direct reference memory mutation, case-insensitive collisions, closed ticket assignments) remain permanently safeguarded.
7. **Interactive Testing Center**: Console-driven interface for running categorized suites, inspecting QA matrices, and executing 10 live negative test scenarios.

---

## 2. Key Test Cases Specification Matrix

| Test ID | Test Suite | Title | Precondition | Input Data | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|---|---|
| **TC-USR-01** | `UserValidatorTest` | Validate full name boundary (min=2, max=50) | None | Name with 1 char, 2 chars, 50 chars, 51 chars | 1 & 51 rejected; 2 & 50 accepted | Matched expectations | **PASS** |
| **TC-USR-02** | `UserValidatorTest` | Parameterized email format validation | None | RFC compliant and malformed email strings | Valid emails pass, malformed emails throw `ValidationException` | Matched expectations | **PASS** |
| **TC-SEC-01** | `PasswordHasherTest` | PBKDF2 random salt uniqueness | Plain-text password | "Password123!" hashed twice | Hashes are different but both match original password | Matched expectations | **PASS** |
| **TC-AUT-01** | `AuthenticationServiceTest`| Reject login for inactive user account | User registered with `UserStatus.INACTIVE` | Valid username & password | Throws `AuthenticationException` ("User account is inactive") | Matched expectations | **PASS** |
| **TC-TCK-01** | `TicketValidatorTest` | Title boundary analysis (min=3, max=100) | None | Titles of 2, 3, 4, 99, 100, 101 characters | 2 & 101 fail; 3, 4, 99, 100 pass | Matched expectations | **PASS** |
| **TC-TCK-02** | `TicketValidatorTest` | Enforce valid lifecycle transitions | Ticket in `OPEN` status | Transition to `ASSIGNED`, `CANCELLED`, `CLOSED` | `ASSIGNED` & `CANCELLED` pass; `CLOSED` throws `InvalidTicketStatusException` | Matched expectations | **PASS** |
| **TC-ASN-01** | `AssignmentServiceTest`| Reject assigning ticket to inactive technician | Technician status is `INACTIVE` | Valid ticket ID and inactive tech ID | Throws `ValidationException` | Matched expectations | **PASS** |
| **TC-ASN-02** | `AssignmentServiceTest`| Reject assigning closed ticket | Ticket in `CLOSED` status | Closed ticket ID and active tech ID | Throws `InvalidTicketStatusException` | Matched expectations | **PASS** |
| **TC-SLA-01** | `SLAServiceTest` | BVA for 8h HIGH priority SLA deadline | Created at 08:00:00 (Deadline 16:00:00) | Resolved at 15:59:00, 16:00:00, 16:01:00 | 15:59 & 16:00 are `SlaStatus.MET`; 16:01 is `SlaStatus.VIOLATED` | Matched expectations | **PASS** |
| **TC-FDB-01** | `FeedbackServiceTest`| Rating scale boundary analysis (1 to 5) | Ticket is `CLOSED` | Ratings of 0, 1, 5, 6 | 0 & 6 rejected; 1 & 5 accepted | Matched expectations | **PASS** |
| **TC-FDB-02** | `FeedbackServiceTest`| Reject feedback on open/in-progress ticket | Ticket is `OPEN` | Rating 5 | Throws `ValidationException` | Matched expectations | **PASS** |
| **TC-REP-01** | `ReportServiceTest` | Accurate metric aggregation & compliance rate | 3 tickets (1 Met, 1 Violated, 1 Open) | Report generation request | Total=3, Met=1, Violated=1, Compliance=50.0% | Matched expectations | **PASS** |
| **TC-INT-01** | `FixFlowEndToEndIntegrationTest` | Full 10-step multi-actor lifecycle | Clean in-memory environment | Complete end-to-end scenario | All 10 phases succeed without state corruption | Matched expectations | **PASS** |
| **TC-QA-01** | `TestingCenterTest` | Real executable 10 negative scenarios | Isolated service instances | 10 live failure injections | All 10 scenarios throw exact expected exceptions | Matched expectations | **PASS** |
| **TC-UI-01** | `ConsoleThemeTest` | Status and Priority Badge Rendering | None | Enum values | Correct formatted badges rendered | Matched expectations | **PASS** |

---

## 3. Boundary Value Analysis (BVA) Summary

| Dimension | Min - 1 | Min | Min + 1 | Max - 1 | Max | Max + 1 | Test Status |
|---|---|---|---|---|---|---|---|
| **User Full Name** | 1 (Fail) | 2 (Pass) | 3 (Pass) | 49 (Pass) | 50 (Pass) | 51 (Fail) | **PASS** |
| **Username** | 2 (Fail) | 3 (Pass) | 4 (Pass) | 19 (Pass) | 20 (Pass) | 21 (Fail) | **PASS** |
| **Password** | 7 (Fail) | 8 (Pass) | 9 (Pass) | 63 (Pass) | 64 (Pass) | 65 (Fail) | **PASS** |
| **Ticket Title** | 2 (Fail) | 3 (Pass) | 4 (Pass) | 99 (Pass) | 100 (Pass) | 101 (Fail) | **PASS** |
| **Ticket Description** | 4 (Fail) | 5 (Pass) | 6 (Pass) | 999 (Pass) | 1000 (Pass) | 1001 (Fail) | **PASS** |
| **Feedback Rating** | 0 (Fail) | 1 (Pass) | 2 (Pass) | 4 (Pass) | 5 (Pass) | 6 (Fail) | **PASS** |
| **SLA Resolution (8h)** | 7h 59m (MET) | 8h 00m (MET) | - | - | 8h 00m (MET) | 8h 01m (VIOLATED) | **PASS** |

---

## 4. Test Suite Execution Summary
- **Total Test Classes**: 12
- **Total Test Cases**: 199
- **Passed**: 199 (100%)
- **Failed**: 0 (0%)
- **Skipped**: 0 (0%)
- **Execution Time**: ~3.6 seconds
