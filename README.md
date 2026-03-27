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

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker (optional)

### Run Locally

```bash
git clone https://github.com/YOUR_USERNAME/expenseflow.git
cd expenseflow
mvn spring-boot:run
```

App starts at `http://localhost:8080`

**Seeded credentials:**

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | ADMIN + FINANCE_MANAGER |
| `manager` | `manager123` | MANAGER |
| `employee` | `employee123` | EMPLOYEE |

### Run with Docker

```bash
docker build -t expenseflow .
docker run -p 8080:8080 expenseflow
```

### Run Tests

```bash
mvn test        # unit tests
mvn verify      # unit + integration tests
```

---

## Example Usage

### 1. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9...", "username": "admin" }
```

### 2. Submit an expense report

```bash
curl -X POST http://localhost:8080/api/expenses/submit/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Team Lunch",
    "description": "Q4 planning lunch",
    "category": "MEALS",
    "amount": 200.00,
    "expenseDate": "2024-11-10",
    "receiptUrl": "https://receipts.company.com/lunch.pdf"
  }'
```

Response includes `flagged: true` with `flagReason` if policy violations are detected.

### 3. Get audit summary

```bash
curl http://localhost:8080/api/audit/summary \
  -H "Authorization: Bearer YOUR_TOKEN"
```

```json
{
  "totalReports": 10,
  "flaggedReports": 3,
  "openViolations": 2,
  "criticalViolations": 1,
  "flagRate": 30.0,
  "violationsByType": {
    "OVER_LIMIT": 1,
    "MISSING_RECEIPT": 1,
    "DUPLICATE_CLAIM": 1
  }
}
```

### 4. Approve a report

```bash
curl -X PUT "http://localhost:8080/api/expenses/1/approve?comment=Looks+good" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Screenshots

> Run the app → use Postman or H2 console at `http://localhost:8080/h2-console`

**Suggested screenshots:**
1. `POST /api/auth/login` — JWT response
2. `POST /api/expenses/submit/{id}` — response showing `flagged: true` with `flagReason`
3. `GET /api/audit/summary` — metrics JSON
4. `GET /api/audit/violations` — list with severity levels
5. H2 console — `POLICY_VIOLATIONS` table with real seeded data

---

## Database Schema

```
employees              expense_reports           policy_violations
----------             ---------------           -----------------
id (PK)                id (PK)                   id (PK)
first_name             employee_id (FK)          expense_report_id (FK)
last_name              title                     employee_id (FK)
email                  description               violation_type
department             category                  severity
role                   amount                    description
monthly_expense_limit  expense_date              resolution_status
manager_id (FK)        receipt_url               detected_at
status                 submitted_at              resolved_by
                       status                    resolved_at
                       is_flagged
                       flag_reason
                       reviewed_by
                       reviewed_at
```

---

## Production Database Swap (Oracle SQL)

```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect
spring.jpa.hibernate.ddl-auto=validate
```

---

## Resume Bullet Points

**Use these directly on your resume:**

- Engineered a Spring Boot REST API implementing a 7-rule policy engine that automatically evaluates every expense submission for over-limit claims, missing receipts, duplicate entries, and budget breaches — classifying violations by severity (LOW to CRITICAL) and triggering a multi-level approval workflow (PENDING → APPROVED / REJECTED / ESCALATED) with a full finance audit trail
- Secured all endpoints using JWT authentication and role-based access control (EMPLOYEE, MANAGER, FINANCE_MANAGER, ADMIN) via Spring Security with BCrypt hashing and stateless session management; designed custom JPQL queries in Spring Data JPA to aggregate monthly spend per employee and detect budget threshold breaches in real time
- Built JUnit unit tests with Mockito covering all 7 policy violation scenarios and Spring MockMvc integration tests validating the full approval workflow; containerized with Docker and exposed Prometheus metrics via Spring Boot Actuator for Grafana observability with a GitHub Actions CI/CD pipeline running tests on every push

---

## License

MIT
