# PaySync — Payment Transaction Platform

A production-grade Java microservices platform for payment transaction processing.
Built with Spring Boot 3.2, Apache Kafka, PostgreSQL, Redis, and Kubernetes.

---

## Architecture

```
                        ┌─────────────────────────────────────────────────┐
                        │                 PaySync Platform                 │
                        │                                                   │
  Client Request        │  ┌─────────────────────────────────────────────┐ │
  POST /api/transactions│  │          transaction-service (:8080)         │ │
 ─────────────────────► │  │                                               │ │
                        │  │  TransactionController                        │ │
                        │  │       │                                        │ │
                        │  │       ▼                                        │ │
                        │  │  TransactionService ──► Redis (cache TTL 300s)│ │
                        │  │       │                                        │ │
                        │  │       ├──► PostgreSQL (JPA + optimistic lock)  │ │
                        │  │       │                                        │ │
                        │  │       └──► TransactionProducer                 │ │
                        │  └──────────────────┬──────────────────────────┘ │
                        │                     │ Kafka topic:                │
                        │                     │ transaction-events          │
                        │                     ▼                             │
                        │  ┌──────────────────────────────────────────────┐ │
                        │  │        notification-service (:8081)           │ │
                        │  │                                               │ │
                        │  │  NotificationListener (Kafka consumer)        │ │
                        │  │       │                                        │ │
                        │  │       ▼                                        │ │
                        │  │  NotificationService.processTransactionEvent  │ │
                        │  └──────────────────────────────────────────────┘ │
                        └─────────────────────────────────────────────────┘
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| transaction-service | 8080 | REST API — create/query transactions, publishes Kafka events |
| notification-service | 8081 | Kafka consumer — processes transaction events asynchronously |
| PostgreSQL | 5432 | Persistent transaction storage |
| Redis | 6379 | Transaction cache (TTL 300s) |
| Kafka | 9092 | Event streaming (topic: `transaction-events`) |

## Tech Stack

- **Java 25** (compiled to Java 21 bytecode for Spring Boot 3.2 compatibility)
- **Spring Boot 3.2** — REST, JPA, Security, Actuator
- **Spring Kafka** — producer/consumer with `JsonSerializer`/`JsonDeserializer`
- **Spring Data JPA + Hibernate** — PostgreSQL ORM with optimistic locking (`@Version`)
- **Spring Data Redis** — response caching with 300s TTL
- **Spring Security + JWT** — `jjwt` filter extending `OncePerRequestFilter`
- **CompletableFuture + @Async** — thread pool (core=5, max=10, queue=100)
- **JUnit 5 + Mockito + MockMvc** — 6 tests, 0 failures

## Quick Start

### Run locally (requires PostgreSQL, Redis, Kafka running)

```bash
cd transaction-service
mvn spring-boot:run

# Health check
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

### Run with Docker Compose (recommended)

```bash
docker-compose up --build

# Health check
curl http://localhost:8080/actuator/health
```

### Create a transaction

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"amount": 150.00, "currency": "USD", "description": "Test payment"}'
# HTTP 201 Created
```

### Query a transaction

```bash
curl http://localhost:8080/api/transactions/{id}
```

## Environment Variables

Create a `.env` file at the project root (excluded from git):

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/paysync
SPRING_DATASOURCE_USERNAME=paysync
SPRING_DATASOURCE_PASSWORD=paysync123
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
JWT_SECRET=paysync-secret-key-for-jwt-token-signing
```

## Kubernetes Deployment

```bash
# Apply all manifests (dry run)
kubectl apply --dry-run=client -f k8s/

# Deploy
kubectl apply -f k8s/

# Check rollout
kubectl rollout status deployment/transaction-service
kubectl rollout status deployment/notification-service
```

Manifests:
- `k8s/configmap.yaml` — environment config (DB_URL, Kafka, Redis)
- `k8s/deployment.yaml` — transaction-service (2 replicas) + notification-service (1 replica)
- `k8s/service.yaml` — ClusterIP (internal) + LoadBalancer (external port 80)

## Build & Test

```bash
# Build and test transaction-service
cd transaction-service && mvn clean install
# Tests run: 6, Failures: 0, Errors: 0

# Build notification-service
cd notification-service && mvn clean install -DskipTests
```

## Test Results (Phase 2)

| Test Class | Test | Status |
|-----------|------|--------|
| TransactionControllerTest | createTransaction_validInput_returns201 | PASS |
| TransactionControllerTest | createTransaction_invalidAmount_returns400 | PASS |
| TransactionControllerTest | getTransaction_notFound_returns404 | PASS |
| TransactionServiceTest | createTransaction_savesAndPublishesToKafka | PASS |
| TransactionServiceTest | processAsync_completesWithCompletableFuture | PASS |
| TransactionServiceTest | getTransaction_notFound_throwsException | PASS |

## CI/CD

GitHub Actions pipeline (`.github/workflows/ci.yml`) triggers on push to `dev`/`main` and PRs to `main`:

1. Build transaction-service (`mvn clean install`)
2. Build notification-service (`mvn clean install -DskipTests`)
3. Docker build check for both images

## Key Design Decisions

| Decision | Choice | Reason |
|----------|--------|--------|
| Optimistic locking | `@Version` on Transaction | Prevents lost updates under concurrent writes |
| Async processing | `CompletableFuture` + `@Async` | Non-blocking Kafka publish |
| Cache strategy | Redis with 300s TTL | Read-heavy GET /transactions/{id} path |
| Kafka serialization | `JsonDeserializer` with trusted packages | Type-safe event deserialization |
| JWT filter | `OncePerRequestFilter` | Guaranteed single execution per request |
