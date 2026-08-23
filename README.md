# FixFlow — Smart Maintenance & Incident Management System
## Digital Egypt Pioneers Initiative (DEPI)
### Ministry of Communications and Information Technology (MCIT)

---

## 🏛️ Project Supervision & Team
- **Under the Supervision of**: Team Dr/ Hacker — **Dr / Mina S. Younan**
- **Presented by (Team Dr/ Hacker)**:
  - **Mohamed Tharwat**
  - **Mariam Samy**
  - **Shahd Moaz**
  - **Salah Reda**

---

## 🌟 Executive Summary

**FixFlow** is an enterprise-grade, clean-architecture desktop and operations management platform designed to organize maintenance requests from initial reporting through technician assignment, investigation, resolution, and closure. The system combines multi-tier **SLA (Service Level Agreement) surveillance**, real-time **in-system event notifications**, **customer satisfaction feedback**, and **executive analytics** with a world-class **Software Testing & QA Center** (199 Automated Tests, 100% Pass Rate).

---

## 👥 4-Member Team Architecture & Modular Contracts

```text
                                  ┌──────────────────────────┐
                                  │      FixFlow Core        │
                                  │  Domain-Driven Design    │
                                  └─────────────┬────────────┘
                                                │
         ┌──────────────────┬───────────────────┴──────────────────┬──────────────────┐
         │                  │                                      │                  │
         ▼                  ▼                                      ▼                  ▼
   [ MEMBER 1 ]       [ MEMBER 2 ]                           [ MEMBER 3 ]       [ MEMBER 4 ]
 Core Domain & Auth   Ticket Management                      Assignment & SLA   QA, GUI & Reports
 ──────────────────   ─────────────────                      ────────────────   ─────────────────
 • User Model         • Ticket Model                         • Priority Auto-   • Notification Engine
 • Role & UserStatus  • Category & Status                      Evaluation       • Feedback Rating (1-5)
 • PBKDF2 Hasher      • State Machine Rules                  • Assignment Engine• Executive Analytics
 • UserValidator      • TicketValidator                      • Multi-tier SLA   • QA Testing Center
 • UserService        • TicketService                         (2h/8h/24h/72h)   • JavaFX Desktop GUI
 • AuthService        • TicketRepository                     • Workload Balance • 1-Click Launchers
 • UserRepository                                            • SLAService
```

| Member | Focus Area | Core Modules & Deliverables |
|---|---|---|
| **Member 1** | **Core Domain, Security & Auth** | `User`, `Role`, `UserStatus`, PBKDF2 HMAC-SHA256 Hasher (10,000 iterations + 16-byte random salt), `UserValidator`, `UserService`, `AuthenticationService`, `InMemoryUserRepository` |
| **Member 2** | **Ticket Lifecycle & State Machine** | `Ticket`, `Category`, `TicketStatus`, strict state transition rules (`OPEN` ➔ `ASSIGNED` ➔ `IN_PROGRESS` ➔ `RESOLVED` ➔ `CLOSED`), `TicketValidator`, `TicketService`, `InMemoryTicketRepository` |
| **Member 3** | **Assignment Engine & SLA Surveillance** | `Priority`, `AssignmentService` (role & active status enforcement, workload tracking), `PriorityService` (auto keyword & category evaluation), `SLAService` (2h/8h/24h/72h targets, exact boundary analysis, `MET`/`VIOLATED`/`PENDING`) |
| **Member 4** | **QA, Analytics, JavaFX Desktop & Demo** | `NotificationService`, `FeedbackService` (1–5 stars), `ReportService`, `TestingCenterService`, `FixFlowDesktopApp` (JavaFX 21), role dashboards, 1-click launchers (`.bat` / `.sh`), 199 automated tests |

---

## 🖥️ Desktop Application Features

1. **Official Brand Identity & Window Icon**:
   - Custom graphical brand asset embedded directly into the primary window and OS taskbar.
2. **Official DEPI Splash & Welcome Introduction Screen**:
   - Highlights MCIT/DEPI initiative, supervision under **Team Dr/ Hacker** & **Dr / Mina S. Younan**, presentation team, and project mission.
3. **Role-Based Portals & Dashboards**:
   - **USER Dashboard**: Report incidents, view submitted tickets, inspect ticket timeline & SLA status, view notifications, rate service satisfaction.
   - **ADMIN Dashboard**: Operational KPI cards, technician workload distribution, SLA compliance monitoring, unassigned tickets queue, registered users directory, and Testing Center.
   - **TECHNICIAN Workbench**: Assigned work queue, urgent incident alerts, Start Work (`IN_PROGRESS`), and Resolve Ticket with resolution notes (`RESOLVED`).
4. **Interactive Ticket Directory**:
   - Real-time search (by title, location, description).
   - Multi-criteria filter dropdowns (Status, Priority, Category, SLA).
   - Double-click or click `Inspect` to open the full ticket metadata view.
5. **Detailed Ticket Inspection Panel**:
   - Core details (ID, Title, Category, Location, Reporter, Technician, Description).
   - SLA Performance Card (Target window, elapsed time, remaining time gauge, `[SLA MET]` / `[SLA VIOLATED]` badge).
   - Visual Lifecycle Timeline (`CREATED` ➔ `ASSIGNED` ➔ `IN PROGRESS` ➔ `RESOLVED` ➔ `CLOSED`).
   - Technician resolution notes and customer satisfaction ratings.
   - Contextual role-based action buttons (Assign, Start Work, Resolve, Close, Submit Feedback).
6. **Executive Analytics & Visual Charts**:
   - Native JavaFX `PieChart` for Category and Status distributions.
   - Native JavaFX `BarChart` for Technician active assignments and workloads.
   - KPI metrics for SLA Compliance Rate and Average Resolution Time.
7. **Interactive QA & Software Testing Center**:
   - Live execution of **10 Real Negative Scenarios** with real service validation.
   - Comprehensive test methodology breakdown (Unit, Integration, Regression, Validation, Security, Boundary, Negative, Equivalence Partitioning).
   - Representative QA Test Cases Explorer (`TC-001` through `TC-008`).

---

## 🔑 Demo Persona Credentials (Safe Academic Seeds)

| Role | Username | Email | Password |
|---|---|---|---|
| **Administrator** | `admin` | `admin@fixflow.com` | `Password123!` |
| **Technician** | `tech_bob` | `tech@fixflow.com` | `Password123!` |
| **Technician** | `tech_alice` | `alice.tech@fixflow.com` | `Password123!` |
| **Standard User** | `john_user` | `user@fixflow.com` | `Password123!` |
| **Standard User** | `emily_w` | `emily@fixflow.com` | `Password123!` |

---

## ⚡ Direct 1-Click Launchers

FixFlow includes direct launcher scripts in the root project folder:

| File | Target Platform | Description |
|---|---|---|
| [`run-gui.bat`](file:///e:/IEEE/DEPI/java/New%20folder/run-gui.bat) | Windows Desktop | **1-Click Launch**: Launches the complete JavaFX Desktop Application with brand icon and full GUI. |
| [`run-demo.bat`](file:///e:/IEEE/DEPI/java/New%20folder/run-demo.bat) | Windows Desktop | **1-Click Demo**: Runs the automated 15-step demonstration scenario. |
| [`run-fixflow.bat`](file:///e:/IEEE/DEPI/java/New%20folder/run-fixflow.bat) | Windows Desktop | **1-Click Console**: Launches the interactive console system with the DEPI welcome screen. |
| [`run-fixflow.sh`](file:///e:/IEEE/DEPI/java/New%20folder/run-fixflow.sh) | iOS (iSH / a-Shell), macOS, Linux | **Direct Shell Runner**: Cross-platform script to run FixFlow in Unix or mobile terminal environments. |

---

## 💻 Manual Launch & Execution Commands

### 1. Launch JavaFX Desktop GUI:
```bash
mvn javafx:run
```

### 2. Run Automated Live Demonstration:
```bash
java -cp target/classes com.fixflow.ui.FixFlowApp --demo
```

### 3. Run Full Automated Test Suite (199 Tests):
```bash
mvn clean test
```

---

## 🧪 Comprehensive Software Testing & QA Report

### Test Execution Summary:
```text
[INFO] ------------------------------------------------------------------------
[INFO] Results:
[INFO] Tests run: 199, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Test Suite Distribution across 12 Test Classes:

| Test Suite Class | Tests | Focus Area |
|---|---|---|
| `UserServiceTest` | 23 | User registration, password hashing, updates, queries, deactivation |
| `UserRepositoryTest` | 10 | In-memory thread safety, defensive copying, collision prevention |
| `AuthenticationServiceTest` | 9 | Password authentication, inactive account rejection, timing resistance |
| `PasswordHasherTest` | 7 | PBKDF2 HMAC-SHA256 salting, unique salt verification |
| `UserValidatorTest` | 66 | Boundary value analysis (names 2-50, usernames 3-20, passwords 8-64), parameterized email regex |
| `TicketServiceTest` | 18 | Ticket creation, lifecycle state machine transitions, search, closure |
| `TicketValidatorTest` | 36 | Title (3-100), description (5-1000), location boundaries, valid lifecycle rules |
| `AssignmentServiceTest` | 6 | Technician role enforcement, active account check, closed ticket rejection |
| `PriorityServiceTest` | 7 | Category defaults, emergency keyword rules, manual override |
| `SLAServiceTest` | 10 | Target durations (2h, 8h, 24h, 72h), BVA (7h59m MET vs 8h01m VIOLATED) |
| `FeedbackServiceTest` | 8 | Rating boundaries (1–5 stars), feedback submission on resolved/closed tickets |
| `NotificationServiceTest` | 2 | Real-time event notifications, mark as read |
| `ReportServiceTest` | 1 | Executive KPI metric aggregation, SLA compliance rate calculation |
| `FixFlowEndToEndIntegrationTest` | 1 | Complete 10-step multi-actor lifecycle workflow from report to close |
| `RegressionTest` | 4 | Mutation protection, case collisions, closed ticket assignment safeguards |
| `TestingCenterTest` | 2 | Execution and exception verification of all 10 real negative scenarios |
| `ConsoleThemeTest` | 4 | Badge formatting, priority chips, SLA indicator strings |

---

## 📐 Boundary Value Analysis (BVA) Matrix

| Dimension | Min - 1 | Min | Min + 1 | Max - 1 | Max | Max + 1 | Test Status |
|---|---|---|---|---|---|---|---|
| **User Full Name** | 1 (Fail) | 2 (Pass) | 3 (Pass) | 49 (Pass) | 50 (Pass) | 51 (Fail) | **PASS** |
| **Username** | 2 (Fail) | 3 (Pass) | 4 (Pass) | 19 (Pass) | 20 (Pass) | 21 (Fail) | **PASS** |
| **Password** | 7 (Fail) | 8 (Pass) | 9 (Pass) | 63 (Pass) | 64 (Pass) | 65 (Fail) | **PASS** |
| **Ticket Title** | 2 (Fail) | 3 (Pass) | 4 (Pass) | 99 (Pass) | 100 (Pass) | 101 (Fail) | **PASS** |
| **Ticket Description** | 4 (Fail) | 5 (Pass) | 6 (Pass) | 999 (Pass) | 1000 (Pass) | 1001 (Fail) | **PASS** |
| **Feedback Rating** | 0 (Fail) | 1 (Pass) | 2 (Pass) | 4 (Pass) | 5 (Pass) | 6 (Fail) | **PASS** |
| **SLA High (8 Hours)** | 7h 59m (MET) | 8h 00m (MET) | — | — | 8h 00m (MET) | 8h 01m (VIOLATED) | **PASS** |

---

## 🚫 10 Real Executable Negative Scenarios (100% Pass Rate)

1. `TC-NEG-001`: Login With Incorrect Password ➔ Throws `AuthenticationException` (**PASS**)
2. `TC-NEG-002`: Register Duplicate Username ➔ Throws `UserAlreadyExistsException` (**PASS**)
3. `TC-NEG-003`: Create Ticket With Empty/Blank Title ➔ Throws `ValidationException` (**PASS**)
4. `TC-NEG-004`: Create Ticket With Short Description (< 5 chars) ➔ Throws `ValidationException` (**PASS**)
5. `TC-NEG-005`: Request Non-Existent Ticket ID ➔ Throws `TicketNotFoundException` (**PASS**)
6. `TC-NEG-006`: Assign Inactive Technician Account ➔ Throws `ValidationException` (**PASS**)
7. `TC-NEG-007`: Assign Non-Existent Technician ID ➔ Throws `UserNotFoundException` (**PASS**)
8. `TC-NEG-008`: Invalid Status Transition (`CLOSED` -> `IN_PROGRESS`) ➔ Throws `InvalidTicketStatusException` (**PASS**)
9. `TC-NEG-009`: Close Ticket Before Resolution (`OPEN` -> `CLOSED`) ➔ Throws `InvalidTicketStatusException` (**PASS**)
10. `TC-NEG-010`: Submit Feedback With Invalid Rating (> 5 Stars) ➔ Throws `ValidationException` (**PASS**)

---

## 🛠️ Technology Stack
- **Language**: Java 21 LTS (Temurin / Adoptium Hotspot)
- **GUI Framework**: OpenJFX (JavaFX 21.0.2 Controls, FXML, Graphics)
- **Build Tool**: Apache Maven 3.9+
- **Testing Framework**: JUnit Jupiter 5.10.2
- **Design Pattern**: Clean Architecture, Domain-Driven Design (DDD), In-Memory Thread-Safe Repositories
