# Product Requirements Document (PRD) — Gym Management Platform

## 1. Vision
A production-grade, multi-tenant SaaS platform for gym owners and admins to manage members, memberships, fee collection, dues/renewals, attendance, notifications, and business analytics — accessible via a premium React Native mobile app backed by a Spring Boot API.

## 2. Goals
- Eliminate manual fee/due tracking via a Smart Due Engine.
- Give gym admins real-time visibility into revenue, renewals, and overdue members.
- Support multi-gym, multi-branch operation from day one (Super Admin tier).
- Ship with enterprise-grade security, auditability, and test coverage.

## 3. Non-Goals (v1)
- Trainer workout/diet planning (explicitly future scope — schema should not block it, but no UI/API in v1).
- Public member-facing self-service app (members are managed by admins only in v1).
- Online payment gateway integration (v1 records payments collected offline/manually; gateway webhook integration is a fast-follow).

## 4. Personas & Roles
| Role | Scope | Key Jobs |
|---|---|---|
| Super Admin | All gyms | Onboard gyms/branches, manage gym admins, view global analytics, manage subscription/billing tier of each gym |
| Gym Admin | Single gym (all branches) or single branch | Manage members, memberships, payments, attendance, notifications, reports |
| Trainer | Single gym (all branches) or single branch | Record manual payments and view payment history for members (v1); workout/diet plans, progress tracking remain future scope |

## 5. Core Modules & Requirements

### 5.1 Dashboard
- KPI cards: Total Members, Active, Expired, Due Today, Due This Week, Overdue, New Registrations (period), Revenue (period).
- Charts: Revenue trend (line), Membership plan distribution (donut), Attendance trend (bar), Renewals vs New (stacked bar).
- Filters: date range, branch (if multi-branch).

### 5.2 Member Management
- CRUD on member profile: personal details, contact info, fitness details (height, weight, BMI auto-calculated, goal), profile photo, documents (ID proof, medical note — stored in S3).
- Membership summary on profile: current plan, status, outstanding balance, next due date, payment history.
- Soft-delete (deactivate) members; never hard-delete (audit/history retention).

### 5.3 Membership Plan Management
- Plan types: Monthly, Quarterly, Half-Yearly, Annual, Custom (admin-defined duration in days).
- Fields: name, duration_days, price, benefits (text/list), status (active/inactive).
- Plans are versioned implicitly — editing a plan's price does not retroactively change existing member memberships (memberships snapshot price at purchase time).

### 5.4 Payment & Fee Management (P0 — highest priority)
- Entirely manual entry by staff at the front desk — no payment gateway integration in v1. Both **Gym Admin** and **Trainer** roles can record a payment and view payment history; only Gym Admin can manage plans/memberships.
- Payment types: Full, Partial, Advance.
- Multiple payment methods: Cash, Card, UPI, Bank Transfer, Other.
- Every payment is linked to a membership (and transitively a member) and contributes to that membership's `amount_paid`.
- Outstanding amount = membership total price − sum(payments for that membership).
- Payment history is immutable (no edits; corrections via a linked reversal/adjustment record) — required for audit integrity.
- Receipt generation (PDF) per payment.

### 5.5 Smart Due Engine (P0)
Derived (not manually entered) per active membership:
- `next_due_date`, `pending_amount`, `remaining_days`, `overdue_days`, `membership_expiry_date`.
- Status machine: `ACTIVE → DUE_SOON (≤7 days to expiry/due) → OVERDUE (past due, unpaid balance) → EXPIRED (past expiry, no renewal) → RENEWED (new membership created from this one)`.
- Computation must be a pure function of (plan duration, start date, payments, today) — covered by 100%-coverage unit tests; recalculated on read, not stored as a separate mutable status column to avoid drift (a `status` cache column may exist but is recomputed by a scheduled job + on every payment/read).

### 5.6 Attendance
- Manual check-in/check-out by admin, or self check-in via member-scoped QR code scanned at front desk.
- One open session per member per day; check-out optional (auto-close at midnight if missed).
- Attendance reports: daily/weekly/monthly counts, per-member history.

### 5.7 Notifications
- Triggers: due reminder (T-3 days), renewal reminder (T-7/T-1 days to expiry), payment confirmation (on payment create), attendance alert (optional, e.g. missed N days), promotional (admin-broadcast).
- Channel v1: in-app + push (FCM/APNs via a notification service); SMS/email adapters reserved as pluggable interfaces.
- Delivery is logged (notification_log) for audit/debugging.

### 5.8 Reports
- Revenue, Membership, Payment, Attendance reports with date-range and branch filters.
- Export: PDF and Excel (XLSX).

### 5.9 Search & Filters
- Members searchable by name, phone, member ID (human-readable code, not UUID).
- Filterable by membership status, payment status, plan.

## 6. Non-Functional Requirements
- **Security**: JWT access + refresh tokens, RBAC, bcrypt password hashing, audit log of all mutating actions, session/device tracking, rate-limiting on auth endpoints.
- **Performance**: Dashboard queries return in <500ms for gyms up to 10k members (requires proper indexing — see DATABASE.md).
- **Availability**: Stateless API (horizontally scalable on ECS), RDS Multi-AZ for prod.
- **Testability**: 90% backend coverage, 100% on payment/due/expiry/attendance/revenue logic, 85% frontend coverage, full Playwright E2E suite gating deploys.
- **Multi-tenancy**: Row-level isolation by `gym_id` (and `branch_id` where applicable) enforced at the service layer; every query scoped by tenant from JWT claims.

## 7. Success Metrics
- Admin can fully onboard a gym (plans, first 20 members, first payments) in under 15 minutes.
- Zero manual due-date calculation — 100% derived by the Smart Due Engine.
- <1% variance between dashboard revenue figures and ledger (payments table) sum.

## 8. Release Phases (delivery plan, mirrors implementation phases)
1. **Phase 1 (this doc)**: PRD, Architecture, DB Schema + ER diagram.
2. **Phase 2**: Backend — auth/RBAC, gyms/branches, members, membership plans/memberships.
3. **Phase 3**: Backend — payments, Smart Due Engine, attendance, notifications, reports.
4. **Phase 4**: React Native app — auth, dashboard, member/membership/payment/attendance screens.
5. **Phase 5**: Playwright E2E + GitHub Actions CI/CD + AWS deployment.
