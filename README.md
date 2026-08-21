# FixFlow — Smart Maintenance & Incident Management System

FixFlow is a Java-based Smart Maintenance & Incident Management System developed as a four-member team project with a strong focus on **Software Testing, Clean OOP, modular architecture, and maintainable code**.

The system is designed to manage maintenance incidents from reporting and ticket creation through technician assignment, priority/SLA handling, resolution, and closure.

---

## Current Development Scope

This repository contains the implementation of:

**Member 1 — Core Domain & User Management Foundation**

The Member 1 module provides the foundation required by the other project modules.

### Implemented Responsibilities

* User domain model
* User roles and statuses
* User validation
* User repository abstraction
* In-memory user repository
* User management services
* Authentication service
* Password hashing
* Custom exceptions
* Unit and parameterized testing
* Team integration contracts

---

## Project Team Structure

The project is divided into four development responsibilities:

| Member   | Responsibility                                   |
| -------- | ------------------------------------------------ |
| Member 1 | Core Domain + User Management + Authentication   |
| Member 2 | Ticket Management                                |
| Member 3 | Assignment + Priority + SLA                      |
| Member 4 | Reports + Notifications + QA/Integration Testing |

### Important

This branch/module intentionally does **not** implement:

* TicketService
* AssignmentService
* PriorityService
* SLAService
* ReportService
* NotificationService

Those responsibilities belong to the other team members.

---

# Technology Stack

* Java 21 LTS
* Maven
* JUnit 5 / Jupiter
* Pure Java OOP
* Constructor-based Dependency Injection
* ConcurrentHashMap
* PBKDF2WithHmacSHA256
* Git / GitHub

No heavy frameworks such as Spring Boot are required for this module.

The project intentionally keeps the architecture lightweight because the main objective is Software Testing and clean object-oriented design.

---

# Architecture

The Member 1 module follows a layered and decoupled architecture:

```text
                    ┌─────────────────────────┐
                    │      User / Domain      │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │       Validation        │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │         Service          │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │      Repository API      │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │ InMemoryUserRepository  │
                    └─────────────────────────┘
```

Authentication uses a separate security flow:

```text
AuthenticationService
          │
          ▼
    UserRepository
          │
          ▼
    PasswordHasher
          │
          ▼
PBKDF2PasswordHasher
```

This separation makes the code easier to test, mock, replace, and maintain.

---

# Package Structure

```text
src/
├── main/
│   └── java/
│       └── com/
│           └── fixflow/
│               ├── model/
│               │   ├── User.java
│               │   ├── Role.java
│               │   └── UserStatus.java
│               │
│               ├── security/
│               │   ├── PasswordHasher.java
│               │   └── PBKDF2PasswordHasher.java
│               │
│               ├── exception/
│               │   ├── ValidationException.java
│               │   ├── UserNotFoundException.java
│               │   ├── UserAlreadyExistsException.java
│               │   └── AuthenticationException.java
│               │
│               ├── validation/
│               │   ├── ValidationUtils.java
│               │   └── UserValidator.java
│               │
│               ├── repository/
│               │   ├── UserRepository.java
│               │   └── InMemoryUserRepository.java
│               │
│               └── service/
│                   ├── UserService.java
│                   └── AuthenticationService.java
│
└── test/
    └── java/
        └── com/
            └── fixflow/
                ├── validation/
                │   └── UserValidatorTest.java
                │
                ├── repository/
                │   └── UserRepositoryTest.java
                │
                ├── security/
                │   └── PasswordHasherTest.java
                │
                └── service/
                    ├── UserServiceTest.java
                    └── AuthenticationServiceTest.java
```

---

# Domain Model

## User

The `User` entity contains:

```text
id
fullName
username
email
passwordHash
role
status
createdAt
updatedAt
```

Passwords are never stored as plain text.

The model uses encapsulation and protects sensitive information from being exposed through `toString()`.

---

# Roles

The system currently supports exactly three roles:

```java
USER
TECHNICIAN
ADMIN
```

Do not introduce additional roles without team agreement.

---

# User Status

Two statuses are supported:

```java
ACTIVE
INACTIVE
```

Business rule:

```text
ACTIVE   → User can authenticate
INACTIVE → User cannot authenticate
```

---

# Validation Rules

## Full Name

```text
Required
Minimum: 2 characters
Maximum: 50 characters
Letters, spaces and hyphens
```

## Username

```text
Required
Minimum: 3 characters
Maximum: 20 characters
Alphanumeric characters and underscores
Must start with a letter
Must be unique
```

## Email

```text
Required
Valid email format
Maximum: 100 characters
Must be unique
```

## Password

```text
Minimum: 8 characters
Maximum: 64 characters
At least one uppercase letter
At least one lowercase letter
At least one digit
At least one special character
```

---

# Password Security

Raw passwords must never be stored.

The security abstraction is:

```java
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
```

The current implementation uses:

```text
PBKDF2WithHmacSHA256
+
Cryptographically Secure Random Salt
```

This keeps password hashing separated from the authentication service and makes the implementation replaceable and testable.

---

# Repository Contract

The main repository abstraction is:

```java
public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User save(User user);

    User update(User user);

    void delete(Long id);

    List<User> findAll();

    List<User> findByRole(Role role);
}
```

The current implementation is:

```text
InMemoryUserRepository
```

It uses a thread-safe `ConcurrentHashMap`.

The repository is intentionally abstracted behind an interface so it can later be replaced with a database implementation without changing the services.

---

# UserService

`UserService` is responsible for user lifecycle operations.

Main operations include:

```text
registerUser
getUserById
getUserByUsername
updateUser
activateUser
deactivateUser
deleteUser
listUsers
```

The service does not directly manipulate the underlying `HashMap` or repository implementation.

---

# AuthenticationService

Authentication is separated from general user management.

Main operations:

```text
login
logout
```

Authentication flow:

```text
Username
   ↓
Find User
   ↓
Check Existence
   ↓
Check Account Status
   ↓
Verify Password
   ↓
Authentication Success
```

Authentication fails when:

```text
Unknown username
Wrong password
Inactive account
Invalid credentials
```

Passwords and password hashes must never be returned as part of a public response.

---

# Custom Exceptions

The module provides specific exceptions instead of relying on generic `Exception`:

```text
ValidationException
UserNotFoundException
UserAlreadyExistsException
AuthenticationException
```

This makes business failures explicit and easier to test.

---

# Testing Strategy

Testing is a core requirement of FixFlow.

The Member 1 test suite covers:

### User Validation

* Valid inputs
* Null inputs
* Empty inputs
* Invalid usernames
* Invalid emails
* Weak passwords
* Invalid roles
* Boundary values
* Equivalence partitions
* Parameterized tests

### Repository

* Save
* Find
* Update
* Delete
* Duplicate handling
* Role queries
* Defensive copying
* Thread-safety

### Password Security

* Successful hashing
* Different hashes for the same password
* Correct password verification
* Incorrect password rejection
* Tampered hash rejection

### User Service

* Registration
* Duplicate username
* Duplicate email
* User lookup
* Update
* Activation
* Deactivation
* Deletion
* Missing users

### Authentication

* Successful login
* Wrong password
* Unknown username
* Inactive account
* Empty username
* Empty password
* Invalid credentials

---

# Running the Project

Make sure Java 21 and Maven are installed.

Check Java:

```bash
java -version
```

Check Maven:

```bash
mvn -version
```

Run all tests:

```bash
mvn test
```

Run a specific test class:

```bash
mvn -Dtest=UserServiceTest test
```

Run validation tests:

```bash
mvn -Dtest=UserValidatorTest test
```

Run authentication tests:

```bash
mvn -Dtest=AuthenticationServiceTest test
```

---

# Integration Guide for Other Team Members

## Member 2 — Ticket Management

You can directly reference the existing `User` model.

Example:

```java
private User reporter;
```

Do not create another user model.

Use the existing:

```text
com.fixflow.model.User
```

---

## Member 3 — Assignment / Priority / SLA

Technicians should use the existing:

```java
User technician;
```

The role can be checked through:

```java
user.getRole()
```

Do not duplicate the `Role` enum.

---

## Member 4 — Reports / Notifications

Reports can use:

```java
List<User>
```

and the existing user attributes.

Do not access the internal storage of `InMemoryUserRepository` directly.

---

# Integration Rules

Before modifying shared classes:

1. Check the existing implementation.
2. Do not create duplicate models.
3. Do not rename shared public methods without team agreement.
4. Reuse existing interfaces.
5. Keep Java 21 compatibility.
6. Keep Maven configuration consistent.
7. Do not introduce unnecessary frameworks.
8. Do not modify Member 1 business logic without coordination.

---

# Team Contract

The detailed public API and integration contract is available at:

```text
docs/team-contracts/member-1-contract.md
```

This document should be treated as the integration reference for Members 2, 3, and 4.

---

# Git Workflow

Recommended branch:

```text
feature/member1-user-management
```

Suggested commits:

```text
feat: add user domain model
feat: add user validation
feat: add user repository
feat: implement user service
feat: implement authentication
test: add user service tests
test: add authentication tests
docs: add member 1 API contract
```

Avoid large commits containing unrelated changes.

---

# Important Scope Boundary

This module intentionally stops at:

```text
Core Domain
User Management
Authentication
Validation
Repository
Security
Testing
Contracts
```

The following modules are outside Member 1 scope:

```text
Ticket Management
Assignment
Priority
SLA
Reports
Notifications
```

These should be implemented by the other team members.

---

# Development Philosophy

FixFlow follows these principles:

* Clean OOP
* Low coupling
* High cohesion
* Dependency inversion
* Constructor-based dependency injection
* Testability
* Encapsulation
* Explicit contracts
* Secure password handling
* Minimal dependencies
* No unnecessary frameworks

The project is intentionally designed as a lightweight academic software-testing project rather than an over-engineered production framework.

---

# Status

**Member 1 — Core Domain & User Management**

```text
Foundation:        Ready
Domain Models:     Ready
Validation:        Ready
Repository:        Ready
Authentication:    Ready
Security:          Ready
Testing:           Ready
Documentation:     Ready
Integration:       Ready for team continuation
```

---

## Next Development Phase

The repository is now prepared for the remaining team members.

### Member 2

Continue with:

```text
Ticket Management
Ticket Lifecycle
Ticket Validation
Ticket Tests
```

### Member 3

Continue with:

```text
Technician Assignment
Priority Calculation
SLA Management
Related Tests
```

### Member 4

Continue with:

```text
Reports
Notifications
Integration Testing
QA
Final Test Suite
```

**Do not duplicate the Member 1 User domain or authentication implementation. Reuse the existing public contracts.**
