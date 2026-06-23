# System Architecture — Gym Management Platform

## 1. High-Level Topology

```
                         ┌─────────────────────┐
                         │   React Native App   │
                         │ (iOS / Android)       │
                         └──────────┬───────────┘
                                    │ HTTPS (REST + JWT)
                                    ▼
                    ┌───────────────────────────────┐
                    │   AWS ALB (TLS termination)   │
                    └───────────────┬───────────────┘
                                    ▼
                    ┌───────────────────────────────┐
                    │   ECS Fargate Service          │
                    │   Spring Boot 3.x API (N tasks)│
                    └───────┬───────────────┬────────┘
                            │               │
                            ▼               ▼
                  ┌──────────────┐   ┌─────────────┐
                  │ RDS PostgreSQL│   │   S3 Bucket │
                  │ (Multi-AZ)    │   │ (docs/photos│
                  └──────────────┘   │  /reports)  │
                                      └─────────────┘
                            │
                            ▼
                  ┌──────────────────┐
                  │ CloudWatch Logs/ │
                  │ Alarms + SNS     │
                  └──────────────────┘
```

Notification dispatch (push/SMS/email) is a pluggable adapter behind a `NotificationGateway` interface, invoked async via a Spring `@Async`/scheduled job — kept in-process for v1, can graduate to SQS-backed worker without API contract changes.

## 2. Backend Architecture (Spring Boot)

### 2.1 Layering — Clean Architecture / DDD-lite
```
backend/
├── gym-api/                       # Spring Boot application module
│   ├── src/main/java/com/gymplatform/
│   │   ├── GymPlatformApplication.java
│   │   ├── common/                # cross-cutting: exceptions, base entities, paging, audit
│   │   │   ├── exception/          # GlobalExceptionHandler, custom exceptions, ErrorResponse
│   │   │   ├── audit/              # AuditLog entity, AuditAspect (AOP on mutating service calls)
│   │   │   ├── security/           # JWT filter, SecurityConfig, RBAC annotations
│   │   │   └── tenancy/            # TenantContext (gym_id/branch_id resolved from JWT)
│   │   ├── config/                 # OpenAPI, CORS, Async, Scheduling config
│   │   ├── auth/                   # domain module
│   │   │   ├── domain/             # User, Role entities
│   │   │   ├── repository/
│   │   │   ├── service/            # AuthService, TokenService
│   │   │   ├── api/                # AuthController (DTO in/out)
│   │   │   └── dto/
│   │   ├── gym/                    # Gym, Branch
│   │   ├── member/                 # Member + fitness details + documents
│   │   ├── membership/             # MembershipPlan, Membership
│   │   ├── payment/                # Payment, due engine
│   │   │   ├── domain/
│   │   │   ├── service/
│   │   │   │   ├── PaymentService.java
│   │   │   │   └── DueEngineService.java   # pure-function core, 100% coverage target
│   │   │   ├── api/
│   │   │   └── dto/
│   │   ├── attendance/              # Attendance, QR check-in
│   │   ├── notification/            # Notification, NotificationGateway (push/SMS/email adapters)
│   │   └── report/                  # Report generation (PDF via OpenPDF, Excel via Apache POI)
│   └── src/test/java/...            # mirrors main, JUnit5 + Mockito + Testcontainers
├── pom.xml                          # Maven build, multi-module if split further
└── db/
    └── migration/                   # Flyway: V1__init.sql, V2__..., versioned, never edited after merge
```

### 2.2 Key Patterns
- **Repository Pattern**: Spring Data JPA repositories per aggregate root; no cross-aggregate joins in repositories — composition happens in services.
- **Service Layer**: All business rules (due calculation, status transitions, payment application order) live in services, never in controllers or entities.
- **DTO Pattern**: MapStruct mappers between entities and DTOs; entities never serialize directly to JSON.
- **Global Exception Handling**: `@RestControllerAdvice` mapping domain exceptions → typed HTTP responses (`404 NotFound`, `409 Conflict` for double-payment race, `422` for validation).
- **API Versioning**: URL-based, `/api/v1/...`; breaking changes ship as `/api/v2/...` alongside v1 until mobile clients migrate.
- **Multi-tenancy enforcement**: A `TenantContext` (request-scoped bean) is populated from JWT claims (`gymId`, `branchId`, `role`) by a servlet filter; every repository query for tenant-scoped entities is auto-filtered via Hibernate `@Filter` or explicit `WHERE gym_id = :tenantId` in custom queries — enforced at the service layer with a unit-tested guard, not left to controller discipline.
- **Auditability**: An AOP aspect around all `@Transactional` mutating service methods writes an `audit_logs` row (actor, action, entity, before/after diff, timestamp, ip).

### 2.3 Smart Due Engine — design note
`DueEngineService.computeStatus(Membership membership, List<Payment> payments, LocalDate today)` is a **pure function**: given the same inputs it always returns the same `MembershipStatus` + `pendingAmount` + `remainingDays`/`overdueDays`. No hidden clock reads, no DB calls inside — `today` and data are passed in. This makes it trivially 100%-coverage unit-testable and safe to call both on-read (dashboard) and from a nightly scheduled job that refreshes a cached `status` column for fast filtering/search.

## 3. Frontend Architecture (React Native + JavaScript, Expo)

Built with Expo (SDK 56) rather than bare React Native CLI — faster scaffolding, no local Android Studio/Xcode toolchain required to iterate, and every library in the stated tech stack (React Navigation, React Query, Zustand, React Native Paper, Reanimated, React Hook Form) works unmodified under Expo. Plain JavaScript per project decision (not TypeScript, despite the original tech-stack list).

```
mobile/
├── src/
│   ├── app/                     # navigation root, providers (QueryClient, Theme, Auth)
│   ├── navigation/               # React Navigation stacks/tabs per role
│   ├── screens/
│   │   ├── auth/
│   │   ├── dashboard/
│   │   ├── members/
│   │   ├── memberships/
│   │   ├── payments/
│   │   ├── attendance/
│   │   ├── notifications/
│   │   └── reports/
│   ├── components/               # shared UI: GlassCard, GradientHeader, StatCard, Chart wrappers
│   ├── features/                 # feature-sliced: api hooks (React Query) + zustand slices per domain
│   │   ├── auth/
│   │   ├── members/
│   │   ├── memberships/
│   │   ├── payments/
│   │   ├── attendance/
│   │   └── reports/
│   ├── api/                      # axios instance, interceptors (JWT refresh)
│   ├── theme/                     # colors, typography, spacing, dark/light theme objects
│   ├── store/                     # zustand root (session, ui prefs)
│   └── utils/                     # date/currency formatting, BMI calc (mirrors backend for optimistic UI)
├── __tests__/                     # Jest + RNTL, mirrors src/
├── app.json / metro.config.js / babel.config.js
└── package.json
```

- **State**: React Query for all server state (caching, refetch, optimistic updates on payment/check-in); Zustand for client-only UI state (theme, active branch filter, session).
- **Forms**: React Hook Form + Zod schema validation, shared validation messages with backend DTO constraints where feasible.
- **Theming**: Centralized theme tokens matching the spec palette; React Native Paper `MD3Theme` extended with custom gradient/glassmorphism tokens; dark mode via system-preference + manual toggle persisted in Zustand.
- **Charts**: `react-native-gifted-charts` or `victory-native` for line/bar/donut; Reanimated for animated stat counters.

## 4. Security Architecture
- **AuthN**: Username/password (bcrypt, cost 12) → JWT access token (15 min TTL) + refresh token (7 days, rotating, stored hashed in DB, revocable).
- **AuthZ**: Role-based (`SUPER_ADMIN`, `GYM_ADMIN`, `TRAINER`-reserved) via `@PreAuthorize`, plus tenant-scope check (gym/branch) per request.
- **Transport**: TLS everywhere (ALB termination, internal traffic in VPC).
- **Secrets**: AWS Secrets Manager for DB creds/JWT signing key, injected as ECS task env vars — never committed.
- **Audit Logs**: immutable, append-only `audit_logs` table; queryable by Super Admin only.
- **Session Management**: refresh tokens tracked per device; "log out all devices" revokes all refresh tokens for a user.

## 5. CI/CD Pipeline (GitHub Actions) — overview
1. **Lint** (ESLint/Prettier for mobile, Checkstyle/Spotless for backend) — parallel.
2. **Unit Tests** (Jest w/ coverage gate 85%; JUnit5/Mockito w/ coverage gate 90%, 100% on payment/due/attendance/revenue packages via JaCoCo rule).
3. **Integration Tests** (Spring Boot Test + Testcontainers PostgreSQL).
4. **Playwright E2E** (against a docker-compose'd backend + seeded DB).
5. **Security Scan** (`npm audit`/`OWASP Dependency-Check`, CodeQL).
6. **Docker Build** (backend image) + push to ECR.
7. **AWS Deploy** (ECS service update) — only if all prior stages green, manual approval gate for prod environment.

Full workflow YAML ships in Phase 5 alongside the E2E suite.

## 6. Open Decisions Deferred to Implementation
- Build tool for backend: **Maven** (confirmed — matches local toolchain, Maven 3.8.4 + Java 21 already installed).
- Chart library final pick (`victory-native` vs `react-native-gifted-charts`) — decided during Phase 4 based on Reanimated 3 compatibility check.
