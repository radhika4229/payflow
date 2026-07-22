# PayFlow — Digital Wallet & Transaction Service

A digital wallet backend built with Spring Boot, supporting user authentication, wallet balance management, and money transfers with concurrency-safe, idempotent transaction processing.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3, Spring Security 6, Spring Data JPA, Spring AOP
- **Database:** PostgreSQL
- **Cache:** Redis
- **Messaging:** Apache Kafka
- **Containerization:** Docker, Docker Compose
- **Testing:** JUnit 5, Mockito
- **Auth:** JWT (JSON Web Tokens)

## Features

- **JWT Authentication** — Register/login with BCrypt password hashing and role-based access control
- **Wallet Management** — Balance check, add money, with account-scoped Redis caching (cache-aside pattern with invalidation on balance change)
- **Money Transfer** — Transfer between accounts with:
    - `SERIALIZABLE` transaction isolation + pessimistic row locking to prevent race conditions on concurrent balance updates
    - Consistent lock-ordering (by account UUID) to prevent deadlocks on simultaneous opposite-direction transfers
    - Idempotency keys (DB-enforced via unique constraint) to prevent duplicate processing on client retries
- **Event Publishing** — Transaction events published to Kafka after successful DB commit, decoupling downstream processing (e.g. notifications) from the core transfer flow
- **Audit Logging** — Automated via Spring AOP (`@Auditable` annotation + `AuditAspect`) — no manual logging code duplicated across service methods
- **Automated Tests** — Unit tests (JUnit 5 + Mockito) covering success paths and edge cases: insufficient balance, self-transfer, duplicate idempotency key, invalid receiver

## Architecture

Modular monolith — each concern (`auth`, `wallet`, `transaction`, `audit`) lives in its own package with its own service, repository, and DTO layer, keeping module boundaries clean without the operational overhead of separate services.

## Getting Started

### Prerequisites
- Docker Desktop installed and running

### Run the Full Stack

```bash
docker-compose up -d
```

This starts PostgreSQL, Redis, Zookeeper, Kafka, and the PayFlow application together. The app will be available at `http://localhost:8080`.

On first run, connect to the Postgres container and create the schema:

```bash
docker exec -it payflow-postgres psql -U postgres -d payflow_db
```

Then run the table creation scripts (see `/docs/schema.sql` or the entity classes under `src/main/java/.../*/entity`).

### Stop the Stack

```bash
docker-compose down
```

Data persists in a named Docker volume across restarts. Use `docker-compose down -v` to reset all data.

## API Overview

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user (creates a linked wallet account) |
| POST | `/api/auth/login` | Login, returns JWT |
| GET | `/api/wallet/balance` | Get current user's balance (cached) |
| POST | `/api/wallet/add-money` | Add money to current user's wallet |
| POST | `/api/transactions/transfer` | Transfer money to another account |

All endpoints except `/api/auth/**` require a `Authorization: Bearer <token>` header.

## Running Tests

```bash
mvn test
```

Runs the unit test suite (Mockito-based) for `AuthService`, `WalletService`, and `TransactionService`.

## Project Structure

```
src/main/java/com/radhika/payflow/
├── auth/            # Register, login, JWT
├── wallet/           # Account, balance, add-money
├── transaction/      # Transfer, idempotency, Kafka events
├── audit/            # AOP-based audit logging
├── security/         # JWT filter, security config
└── common/           # Shared exceptions, repositories
```

## Notes

This project intentionally does not include CI/CD or a live deployment — the focus was on getting core backend concerns right: concurrency safety, idempotency, caching, event-driven design, and testing.
