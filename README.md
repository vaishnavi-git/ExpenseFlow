# ExpenseFlow — Expense Reporting & Multi-Level Approval Pipeline

> A production-grade Spring Boot REST API that automates enterprise expense reporting from submission and policy violation detection to multi-level approval workflows with a full finance audit trail.

Built around real HR/finance compliance requirements: the same problems handled at scale by platforms like ADP, Workday, and Concur.

---

## Why This Exists

Manual expense review is slow, inconsistent, and leaks money. A single undetected duplicate claim or missed receipt policy can cost an organization thousands. ExpenseFlow automates the entire pipeline employees submit, the policy engine flags violations instantly, managers approve or escalate, and finance gets a clean audit summary all through a secured, role-based API.

---

## Features

### Policy Engine (Core)
Runs automatically on every submission and checks for:

| Violation | Severity | Trigger |
|---|---|---|
| `OVER_LIMIT` | CRITICAL | Single claim exceeds $5,000 |
| `MISSING_RECEIPT` | HIGH | Amount over $25 with no receipt attached |
| `MONTHLY_BUDGET_EXCEEDED` | HIGH | Claim would push employee over monthly limit |
| `DUPLICATE_CLAIM` | CRITICAL | Same amount + category within ±1 day |
| `INVALID_CATEGORY` | MEDIUM | Meal expense exceeds $150 per-meal cap |
| `LATE_SUBMISSION` | MEDIUM | Submitted more than 30 days after expense date |
| `SUSPICIOUS_AMOUNT` | LOW | Round-number claim ≥ $1,000 |

### Approval Workflow
- **PENDING** → **UNDER_REVIEW** → **APPROVED** / **REJECTED** / **ESCALATED**
- Managers can approve, reject with comments, or escalate to finance
- Every action is timestamped and attributed to the authenticated user

### Security
- JWT-based authentication with configurable expiration
- Role-based access control: `EMPLOYEE`, `MANAGER`, `FINANCE_MANAGER`, `ADMIN`
- Method-level security via `@PreAuthorize`
- Stateless session, BCrypt password hashing

### Observability
- Prometheus metrics via Spring Boot Actuator (`/actuator/prometheus`)
- Health endpoint at `/actuator/health`
- Structured for Grafana dashboard integration

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2, Spring MVC, Spring Security |
| API | RESTful API, JSON |
| Auth | JWT (JJWT), BCrypt, Role-Based Access Control |
| ORM | Spring Data JPA, Hibernate, JPQL |
| Database | H2 (dev) — swap to Oracle SQL / PostgreSQL for prod |
| Build | Maven |
| Testing | JUnit 5, Mockito, Spring MockMvc |
| CI/CD | GitHub Actions |
| Containerization | Docker |
| Monitoring | Prometheus, Spring Boot Actuator |

---

## Architecture

```
src/
├── controller/        # REST endpoints (Auth, Expense, Audit)
├── service/
│   ├── PolicyEngineService.java   # Core violation detection — 7 policy checks
│   ├── ExpenseService.java        # Submission + approval workflow
│   └── AuditService.java         # Violation tracking + finance summary
├── repository/        # Spring Data JPA with custom JPQL queries
├── model/             # JPA entities: Employee, ExpenseReport, PolicyViolation, User
├── security/          # JWT filter, UserDetailsService, JwtUtil
├── config/            # SecurityConfig, DataSeeder
└── exception/         # GlobalExceptionHandler, ResourceNotFoundException
```

---

## API Endpoints

### Auth
| Method | Endpoint | Access |
|---|---|---|
| POST | `/api/auth/login` | Public |
| POST | `/api/auth/register` | Public |

### Expense Reports
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/expenses` | Authenticated | All reports |
| GET | `/api/expenses/{id}` | Authenticated | Single report |
| GET | `/api/expenses/pending` | Authenticated | Awaiting review |
| GET | `/api/expenses/flagged` | Authenticated | Policy violations flagged |
| GET | `/api/expenses/employee/{id}` | Authenticated | By employee |
| POST | `/api/expenses/submit/{employeeId}` | Authenticated | Submit + auto-evaluate |
| PUT | `/api/expenses/{id}/approve` | MANAGER, ADMIN | Approve with comment |
| PUT | `/api/expenses/{id}/reject` | MANAGER, ADMIN | Reject with comment |
| PUT | `/api/expenses/{id}/escalate` | MANAGER, ADMIN | Escalate to finance |

### Audit
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/audit/summary` | FINANCE_MANAGER, ADMIN | Aggregate metrics |
| GET | `/api/audit/violations` | FINANCE_MANAGER, ADMIN | Open violations |
| GET | `/api/audit/violations/employee/{id}` | FINANCE_MANAGER, ADMIN | By employee |
| PUT | `/api/audit/violations/{id}/resolve` | FINANCE_MANAGER, ADMIN | Resolve violation |

---


