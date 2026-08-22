# FixFlow — Smart Maintenance & Incident Management System
## Interactive Application + QA Testing Center & Dashboard

**FixFlow** is an enterprise-grade, clean-architecture Java application designed for reporting, tracking, assigning, resolving, and reporting facilities and IT maintenance incidents with automated SLA tracking, feedback, in-system notifications, and a dedicated **Interactive QA Testing Center**.

---

## 👥 4-Member Team Architecture

| Member | Focus Area | Core Responsibilities |
|---|---|---|
| **Member 1** | **Core Domain & Security** | `User`, `Role`, `UserStatus`, PBKDF2 Password Hashing, `UserService`, `AuthenticationService`, `UserRepository` |
| **Member 2** | **Ticket Management** | `Ticket`, `Category`, `TicketStatus`, `TicketService`, `TicketRepository`, Lifecycle State Transitions |
| **Member 3** | **Assignment & SLA** | `Priority`, `AssignmentService`, `SLAService`, `PriorityService`, Workload Tracking, SLA Boundary Metrics |
| **Member 4** | **QA & Reporting** | `ReportService`, `NotificationService`, `FeedbackService`, `TestingCenterService`, Integration Tests, Regression Tests, `FixFlowApp` UI |

---

## 🚀 Key Features

1. **Role-Based Interactive Dashboards**:
   - **USER Dashboard**: Create Ticket, View Tickets, View Notifications, Submit Feedback, View Reports, Logout.
   - **ADMIN Dashboard**: View All Tickets, Assign Technician, View Users, View Reports, Testing Center, Logout.
   - **TECHNICIAN Dashboard**: View Assigned Tickets, Start Ticket (`IN_PROGRESS`), Resolve Ticket (`RESOLVED` + Notes), View Notifications, Logout.
2. **Interactive QA Testing Center**:
   - Live automated test suite execution (All Tests, User, Auth, Ticket, Assignment, Priority, SLA, Validation, Integration, Regression).
   - **10 Real Executable Negative Scenarios** with real service validation.
   - Boundary Value Analysis (BVA) & Equivalence Partitioning (EP) QA matrix viewer.
   - Representative QA Test Cases Viewer (`TC-001` through `TC-008`).
3. **Controlled Ticket Lifecycle & State Machine**:
   - Strict valid flow: `OPEN` ➔ `ASSIGNED` ➔ `IN_PROGRESS` ➔ `RESOLVED` ➔ `CLOSED`.
   - Rejection of invalid transitions (e.g. `CLOSED` ➔ `OPEN`, `OPEN` ➔ `CLOSED`).
4. **Automated Priority & SLA Engine**:
   - Exact SLA Target Windows:
     - `CRITICAL`: **2 hours**
     - `HIGH`: **8 hours**
     - `MEDIUM`: **24 hours**
     - `LOW`: **72 hours**
   - Precise SLA status tracking: `MET`, `VIOLATED`, `PENDING`.
5. **Technician Assignment & Workload Balancing**:
   - Validates technician existence, `Role.TECHNICIAN`, and `ACTIVE` account status.
6. **In-System Event Notifications & User Feedback**:
   - Real-time event notifications for users and technicians.
   - 1–5 star ratings and comments on completed tickets.
7. **Executive Health & Analytics Reports**:
   - SLA compliance percentage, average resolution duration, breakdowns by category and technician.

---

## 🛠️ Technology Stack
- **Language**: Java 21 LTS
- **Testing Framework**: JUnit 5 (Jupiter 5.10.2) + Parameterized Tests
- **Architecture**: Clean Architecture, Domain-Driven Design (DDD), Thread-Safe In-Memory Repositories with Defensive Copying

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

### 1. Run the Interactive Console Application:
```bash
java -cp target/classes com.fixflow.ui.FixFlowApp
```

### 2. Run the Automated 15-Step Live Demonstration:
```bash
java -cp target/classes com.fixflow.ui.FixFlowApp --demo
```

---

## 🧪 How to Run Automated Tests (195 Tests)

### Running with Maven:
```bash
mvn clean test
```

### Running with JUnit 5 Standalone Runner:
```powershell
javac -d target/classes (Get-ChildItem -Path src/main/java -Filter *.java -Recurse | Select-Object -ExpandProperty FullName)
javac -cp "target/classes;lib/junit-platform-console-standalone-1.10.2.jar" -d target/test-classes (Get-ChildItem -Path src/test/java -Filter *.java -Recurse | Select-Object -ExpandProperty FullName)
java -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path "target/classes;target/test-classes" --scan-class-path --details=summary
```

### Test Suite Execution Status:
```text
[       195 tests found           ]
[         0 tests skipped         ]
[       195 tests started         ]
[         0 tests aborted         ]
[       195 tests successful      ]
[         0 tests failed          ]
```
**Pass Rate: 100% (195 / 195 Passed, 0 Failures, 0 Errors, 0 Skipped)**
