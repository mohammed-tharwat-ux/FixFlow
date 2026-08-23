# FixFlow — Smart Maintenance & Incident Management System
## Digital Egypt Pioneers Initiative (DEPI)
### Ministry of Communications and Information Technology (MCIT)

---

## 🏛️ Project Supervision & Team
- **Under the Supervision of**: Team Dr/ Hacker — **Dr / Mina S. Younan**
- **Presented by (Team Dr/ Hacker)**:
  - Mohamed Tharwat
  - Mariam Samy
  - Shahd Moaz
  - Salah Reda

---

## 👥 4-Member Team Architecture

| Member | Focus Area | Core Responsibilities |
|---|---|---|
| **Member 1** | **Core Domain & Security** | `User`, `Role`, `UserStatus`, PBKDF2 Password Hashing, `UserService`, `AuthenticationService`, `UserRepository` |
| **Member 2** | **Ticket Management** | `Ticket`, `Category`, `TicketStatus`, `TicketService`, `TicketRepository`, Lifecycle State Transitions |
| **Member 3** | **Assignment & SLA** | `Priority`, `AssignmentService`, `SLAService`, `PriorityService`, Workload Tracking, SLA Boundary Metrics |
| **Member 4** | **QA & Reporting** | `ReportService`, `NotificationService`, `FeedbackService`, `TestingCenterService`, Integration Tests, Regression Tests, `FixFlowApp` UI, Guided Presentation Mode |

---

## 🚀 Key Features

1. **DEPI Official Splash & Project Presentation Intro**:
   - Clean professional splash screen introducing supervision, team members, mission, and software testing philosophy.
2. **Role-Based Interactive Dashboards**:
   - **USER Dashboard**: Create Incident Report, View My Tickets, Inspect Ticket details with timeline & SLA panel, View Notifications, Submit Feedback, View Reports, Profile & Settings.
   - **ADMIN Dashboard**: Operational KPIs overview, View All Tickets with filtering, Inspect Ticket, Assign Technician, SLA Compliance Monitoring, View Users directory, Executive Analytics, Testing Center.
   - **TECHNICIAN Dashboard**: Assigned Work Queue, Start Work (`IN_PROGRESS`), Resolve with Notes (`RESOLVED`), View Notifications.
3. **Interactive QA Testing Center**:
   - Live automated test suite execution (All Tests, User, Auth, Ticket, Assignment, Priority, SLA, Validation, Integration, Regression).
   - **10 Real Executable Negative Scenarios** with live service execution.
   - Boundary Value Analysis (BVA) & Equivalence Partitioning (EP) QA matrix viewer.
   - Representative QA Test Cases Viewer (`TC-001` through `TC-008`).
4. **Guided Presentation Mode (11 Steps)**:
   - Live 11-step interactive presentation walkthrough for defense and grading.
5. **Controlled Ticket Lifecycle & State Machine**:
   - Strict valid flow: `OPEN` ➔ `ASSIGNED` ➔ `IN_PROGRESS` ➔ `RESOLVED` ➔ `CLOSED`.
   - Rejection of invalid transitions (e.g. `CLOSED` ➔ `OPEN`, `OPEN` ➔ `CLOSED`).
6. **Automated Priority & SLA Engine**:
   - Exact SLA Target Windows:
     - `CRITICAL`: **2 hours**
     - `HIGH`: **8 hours**
     - `MEDIUM`: **24 hours**
     - `LOW`: **72 hours**
   - Precise SLA status tracking: `MET`, `VIOLATED`, `PENDING`.
7. **Technician Assignment & Workload Balancing**:
   - Validates technician existence, `Role.TECHNICIAN`, and `ACTIVE` account status.
8. **In-System Event Notifications & User Feedback**:
   - Real-time event notifications for users and technicians.
   - 1–5 star ratings and comments on completed tickets.
9. **Executive Health & Analytics Reports**:
   - Visual ASCII distribution charts, SLA compliance percentage, average resolution duration, breakdowns by category and technician.

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

## 🧪 How to Run Automated Tests (199 Tests)

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
[       199 tests found           ]
[         0 tests skipped         ]
[       199 tests started         ]
[         0 tests aborted         ]
[       199 tests successful      ]
[         0 tests failed          ]
```
**Pass Rate: 100% (199 / 199 Passed, 0 Failures, 0 Errors, 0 Skipped)**
