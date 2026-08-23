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

## 🖥️ Application Architecture

FixFlow is a native **JavaFX Desktop Application** backed by clean Domain-Driven Design (DDD) service and in-memory repository layers:

```text
    JavaFX Desktop GUI (OpenJFX 21)
                ↓
        Application Services
    (User, Ticket, SLA, Assignment, Notification, Feedback, Report, TestingCenter)
                ↓
    Thread-Safe In-Memory Repositories (Defensive Copying)
                ↓
        Domain Entity Models
```

---

## 🚀 Key GUI Desktop Features

1. **DEPI Official Welcome & Splash Screen**:
   - Executive title screen introducing DEPI supervision, team members, mission, and Software Testing foundation.
2. **Interactive Authentication & Registration**:
   - Clean login card with quick-fill credentials for demo personas (`admin`, `tech_bob`, `john_user`).
   - Validated registration modal enforcing username, email, and password complexity constraints.
3. **Master Application Shell & Sidebar Navigation**:
   - Left sidebar with real-time user status, online indicator, and instant view switching:
     - **Dashboard Overview**: Role-tailored metrics and KPI summaries.
     - **Ticket Directory**: Multi-criteria filters (Status, Priority, Category), full-text search, and double-click inspection.
     - **Incident Creation**: Validated modal form.
     - **Detailed Ticket Inspection**: SLA performance metrics, lifecycle timeline, resolution notes, feedback ratings, and role actions (Assign Technician, Start Work, Resolve Ticket, Close Ticket, Submit Feedback).
     - **Technician Workbench**: Active assignments queue, urgent incident alerts, and quick actions.
     - **SLA Real-time Monitoring**: Multi-tier deadline surveillance (2h / 8h / 24h / 72h), elapsed and remaining time gauges, live `[MET]`, `[AT RISK]`, `[VIOLATED]` indicators.
     - **Executive Reports & Analytics**: Interactive JavaFX `PieChart` and `BarChart` visualizations for statuses, categories, and technician workloads.
     - **Notification Center**: In-system notifications with type badges and mark-as-read functionality.
     - **QA & Software Testing Center**: Testing summary (199 Tests, 100% Pass Rate), live executable 10 real negative scenarios runner, methodology breakdown, and Test Case Explorer (`TC-001` through `TC-008`).
     - **Profile & System Settings**: Account parameters and runtime environment info.

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

### 1. Launch the JavaFX Desktop GUI Application:
```bash
mvn javafx:run
```
*Or using the Java runtime:*
```bash
java -cp "target/classes;target/dependency/*" com.fixflow.ui.gui.FixFlowDesktopApp
```

### 2. Run the Automated 15-Step Live Console Demonstration:
```bash
java -cp target/classes com.fixflow.ui.FixFlowApp --demo
```

### 3. Run the Full Automated Test Suite (199 Tests):
```bash
mvn clean test
```

### Test Suite Execution Status:
```text
[INFO] Results:
[INFO] Tests run: 199, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
**Pass Rate: 100% (199 / 199 Passed, 0 Failures, 0 Errors, 0 Skipped)**
