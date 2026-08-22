# FixFlow — Smart Maintenance & Incident Management System

**FixFlow** is an enterprise-grade, clean-architecture Java application designed for reporting, tracking, assigning, resolving, and reporting facilities and IT maintenance incidents with automated SLA tracking, feedback, and in-system notifications.

---

## 👥 4-Member Team Architecture

| Member | Focus Area | Core Responsibilities |
|---|---|---|
| **Member 1** | **Core Domain & Security** | `User`, `Role`, `UserStatus`, PBKDF2 Password Hashing, `UserService`, `AuthenticationService`, `UserRepository` |
| **Member 2** | **Ticket Management** | `Ticket`, `Category`, `TicketStatus`, `TicketService`, `TicketRepository`, Lifecycle State Transitions |
| **Member 3** | **Assignment & SLA** | `Priority`, `AssignmentService`, `SLAService`, `PriorityService`, Workload Tracking, SLA Boundary Metrics |
| **Member 4** | **QA & Reporting** | `ReportService`, `NotificationService`, `FeedbackService`, Integration Tests, Regression Tests, `FixFlowApp` UI |

---

## 🚀 Key Features

1. **User Authentication & Role-Based Access Control**:
   - Secure PBKDF2 with HMAC-SHA256 password hashing with per-user cryptographic salts.
   - Distinct roles: `USER` (reporter), `TECHNICIAN` (worker), `ADMIN` (supervisor).
   - Inactive accounts prevented from logging in.
2. **Controlled Ticket Lifecycle**:
   - Strict valid flow: `OPEN` ➔ `ASSIGNED` ➔ `IN_PROGRESS` ➔ `RESOLVED` ➔ `CLOSED`.
   - Rejection of invalid transitions (e.g. `CLOSED` ➔ `OPEN`, `OPEN` ➔ `CLOSED`).
3. **Automated Priority & SLA Engine**:
   - Priority levels: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` with automatic category/keyword detection.
   - Exact SLA Target Windows:
     - `CRITICAL`: **2 hours**
     - `HIGH`: **8 hours**
     - `MEDIUM`: **24 hours**
     - `LOW`: **72 hours**
   - Precise SLA status tracking: `MET`, `VIOLATED`, `PENDING`.
4. **Technician Assignment & Workload Tracking**:
   - Validates technician existence, `Role.TECHNICIAN`, and `ACTIVE` account status.
5. **In-System Notifications & User Feedback**:
   - Real-time notifications on ticket events.
   - User satisfaction ratings (1 to 5 stars) and comments on completed tickets.
6. **Executive Health & Analytics Reports**:
   - Total volume, open/in-progress/resolved/closed counts, SLA compliance percentage, average resolution duration, breakdowns by category and technician.

---

## 🛠️ Technology Stack
- **Language**: Java 21 LTS
- **Testing Framework**: JUnit 5 (Jupiter 5.10.2) + Parameterized Tests
- **Architecture**: Domain-Driven Design (DDD), Clean Architecture, Thread-Safe In-Memory Repositories with Defensive Copying

---

## 📂 Project Structure

```text
FixFlow/
├── pom.xml
├── README.md
├── docs/
│   ├── team-contracts/
│   │   └── member-1-contract.md
│   └── testing-documentation.md
├── lib/
│   └── junit-platform-console-standalone-1.10.2.jar
└── src/
    ├── main/java/com/fixflow/
    │   ├── data/
    │   │   └── DemoDataLoader.java
    │   ├── exception/
    │   │   ├── AuthenticationException.java
    │   │   ├── InvalidTicketStatusException.java
    │   │   ├── TicketNotFoundException.java
    │   │   ├── UnauthorizedOperationException.java
    │   │   ├── UserAlreadyExistsException.java
    │   │   ├── UserNotFoundException.java
    │   │   └── ValidationException.java
    │   ├── model/
    │   │   ├── Category.java
    │   │   ├── Feedback.java
    │   │   ├── Notification.java
    │   │   ├── NotificationType.java
    │   │   ├── Priority.java
    │   │   ├── Role.java
    │   │   ├── SlaStatus.java
    │   │   ├── SystemSummaryReport.java
    │   │   ├── Ticket.java
    │   │   ├── TicketStatus.java
    │   │   ├── User.java
    │   │   └── UserStatus.java
    │   ├── repository/
    │   │   ├── FeedbackRepository.java
    │   │   ├── InMemoryFeedbackRepository.java
    │   │   ├── InMemoryNotificationRepository.java
    │   │   ├── InMemoryTicketRepository.java
    │   │   ├── InMemoryUserRepository.java
    │   │   ├── NotificationRepository.java
    │   │   ├── TicketRepository.java
    │   │   └── UserRepository.java
    │   ├── security/
    │   │   ├── PasswordHasher.java
    │   │   └── PBKDF2PasswordHasher.java
    │   ├── service/
    │   │   ├── AssignmentService.java
    │   │   ├── AuthenticationService.java
    │   │   ├── FeedbackService.java
    │   │   ├── NotificationService.java
    │   │   ├── PriorityService.java
    │   │   ├── ReportService.java
    │   │   ├── SLAService.java
    │   │   ├── TicketService.java
    │   │   └── UserService.java
    │   ├── ui/
    │   │   └── FixFlowApp.java
    │   └── validation/
    │       ├── TicketValidator.java
    │       ├── UserValidator.java
    │       └── ValidationUtils.java
    └── test/java/com/fixflow/
        ├── integration/
        │   └── FixFlowEndToEndIntegrationTest.java
        ├── regression/
        │   └── RegressionTest.java
        ├── repository/
        │   └── UserRepositoryTest.java
        ├── security/
        │   └── PasswordHasherTest.java
        ├── service/
        │   ├── AssignmentServiceTest.java
        │   ├── AuthenticationServiceTest.java
        │   ├── FeedbackServiceTest.java
        │   ├── NotificationServiceTest.java
        │   ├── PriorityServiceTest.java
        │   ├── ReportServiceTest.java
        │   ├── SLAServiceTest.java
        │   ├── TicketServiceTest.java
        │   └── UserServiceTest.java
        └── validation/
            ├── TicketValidatorTest.java
            └── UserValidatorTest.java
```

---

## 🔑 Demo Credentials (Safe Academic Seeds)

| Role | Username | Email | Password |
|---|---|---|---|
| **Administrator** | `admin` | `admin@fixflow.com` | `Password123!` |
| **Technician** | `tech_bob` | `tech@fixflow.com` | `Password123!` |
| **Technician** | `tech_alice` | `alice.tech@fixflow.com` | `Password123!` |
| **Standard User** | `john_user` | `user@fixflow.com` | `Password123!` |
| **Standard User** | `emily_w` | `emily@fixflow.com` | `Password123!` |

---

## 💻 How to Run the Application

### 1. Run Automated 15-Step Demo Scenario:
```bash
javac -d target/classes src/main/java/com/fixflow/**/*.java
java -cp target/classes com.fixflow.ui.FixFlowApp --demo
```

### 2. Run Interactive Console UI:
```bash
java -cp target/classes com.fixflow.ui.FixFlowApp
```

---

## 🧪 How to Run the Test Suite (193 Tests)

### Running with JUnit 5 Standalone Runner:
```powershell
javac -d target/classes (Get-ChildItem -Path src/main/java -Filter *.java -Recurse | Select-Object -ExpandProperty FullName)
javac -cp "target/classes;lib/junit-platform-console-standalone-1.10.2.jar" -d target/test-classes (Get-ChildItem -Path src/test/java -Filter *.java -Recurse | Select-Object -ExpandProperty FullName)
java -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path "target/classes;target/test-classes" --scan-class-path --details=tree
```

### Running with Maven:
```bash
mvn clean test
```

### Test Suite Results:
```text
[       193 tests found           ]
[         0 tests skipped         ]
[       193 tests started         ]
[         0 tests aborted         ]
[       193 tests successful      ]
[         0 tests failed          ]
```
**Pass Rate: 100% (193 / 193 Passed)**
