CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============ ROLES ============
CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- ============ GYMS ============
CREATE TABLE gyms (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(150) NOT NULL,
    subscription_tier   VARCHAR(30) NOT NULL DEFAULT 'STANDARD',
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                          CHECK (status IN ('ACTIVE','SUSPENDED','DELETED')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============ BRANCHES ============
CREATE TABLE branches (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gym_id      UUID NOT NULL REFERENCES gyms(id) ON DELETE CASCADE,
    name        VARCHAR(150) NOT NULL,
    address     VARCHAR(300),
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                  CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (gym_id, name)
);
CREATE INDEX idx_branches_gym_id ON branches(gym_id);

-- ============ USERS ============
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gym_id          UUID REFERENCES gyms(id) ON DELETE CASCADE,
    branch_id       UUID REFERENCES branches(id) ON DELETE SET NULL,
    role_id         UUID NOT NULL REFERENCES roles(id),
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    full_name       VARCHAR(150) NOT NULL,
    phone           VARCHAR(20),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                      CHECK (status IN ('ACTIVE','INACTIVE','LOCKED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_gym_branch ON users(gym_id, branch_id);

-- ============ REFRESH TOKENS ============
CREATE TABLE refresh_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash      VARCHAR(255) NOT NULL UNIQUE,
    device_info     VARCHAR(255),
    expires_at      TIMESTAMPTZ NOT NULL,
    revoked_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- ============ MEMBERS ============
CREATE TABLE members (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gym_id              UUID NOT NULL REFERENCES gyms(id) ON DELETE CASCADE,
    branch_id           UUID NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
    member_code         VARCHAR(30) NOT NULL,
    full_name           VARCHAR(150) NOT NULL,
    phone               VARCHAR(20) NOT NULL,
    email               VARCHAR(150),
    date_of_birth       DATE,
    gender              VARCHAR(10) CHECK (gender IN ('MALE','FEMALE','OTHER')),
    height_cm           NUMERIC(5,2),
    weight_kg           NUMERIC(5,2),
    fitness_goal        VARCHAR(100),
    profile_photo_url   VARCHAR(500),
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                          CHECK (status IN ('ACTIVE','INACTIVE','DELETED')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (gym_id, member_code)
);
CREATE INDEX idx_members_gym_branch ON members(gym_id, branch_id);
CREATE INDEX idx_members_phone ON members(phone);
CREATE INDEX idx_members_name_search ON members USING GIN (to_tsvector('simple', full_name));

-- ============ MEMBER DOCUMENTS ============
CREATE TABLE member_documents (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id   UUID NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    doc_type    VARCHAR(50) NOT NULL,
    file_url    VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_member_documents_member_id ON member_documents(member_id);

-- ============ MEMBERSHIP PLANS ============
CREATE TABLE membership_plans (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gym_id          UUID NOT NULL REFERENCES gyms(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    plan_type       VARCHAR(20) NOT NULL
                      CHECK (plan_type IN ('MONTHLY','QUARTERLY','HALF_YEARLY','ANNUAL','CUSTOM')),
    duration_days   INT NOT NULL CHECK (duration_days > 0),
    price           NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    benefits        TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                      CHECK (status IN ('ACTIVE','INACTIVE')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (gym_id, name)
);
CREATE INDEX idx_membership_plans_gym_id ON membership_plans(gym_id);

-- ============ MEMBERSHIPS ============
CREATE TABLE memberships (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id           UUID NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    plan_id             UUID NOT NULL REFERENCES membership_plans(id) ON DELETE RESTRICT,
    renewed_from_id     UUID REFERENCES memberships(id) ON DELETE SET NULL,
    start_date          DATE NOT NULL,
    end_date            DATE NOT NULL,
    total_price         NUMERIC(10,2) NOT NULL CHECK (total_price >= 0),
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                          CHECK (status IN ('ACTIVE','DUE_SOON','OVERDUE','EXPIRED','RENEWED','CANCELLED')),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (end_date > start_date)
);
CREATE INDEX idx_memberships_member_status ON memberships(member_id, status);
CREATE INDEX idx_memberships_status_end_date ON memberships(status, end_date);

-- ============ PAYMENTS (immutable ledger) ============
CREATE TABLE payments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    membership_id   UUID NOT NULL REFERENCES memberships(id) ON DELETE RESTRICT,
    amount          NUMERIC(10,2) NOT NULL CHECK (amount > 0),
    payment_type    VARCHAR(20) NOT NULL
                      CHECK (payment_type IN ('FULL','PARTIAL','ADVANCE')),
    payment_method  VARCHAR(20) NOT NULL
                      CHECK (payment_method IN ('CASH','CARD','UPI','BANK_TRANSFER','OTHER')),
    receipt_number  VARCHAR(50) NOT NULL UNIQUE,
    is_reversed     BOOLEAN NOT NULL DEFAULT FALSE,
    recorded_by     UUID NOT NULL REFERENCES users(id),
    paid_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_payments_membership_paid_at ON payments(membership_id, paid_at DESC);
CREATE INDEX idx_payments_paid_at ON payments(paid_at);

CREATE OR REPLACE FUNCTION prevent_payment_mutation() RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'payments are immutable; use is_reversed flag via adjustment flow';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_payments_no_update
    BEFORE UPDATE ON payments
    FOR EACH ROW
    WHEN (OLD.is_reversed IS DISTINCT FROM NEW.is_reversed)
    EXECUTE FUNCTION prevent_payment_mutation();

CREATE TRIGGER trg_payments_no_delete
    BEFORE DELETE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION prevent_payment_mutation();

-- ============ DUES (cached Smart Due Engine output) ============
CREATE TABLE dues (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    membership_id       UUID NOT NULL UNIQUE REFERENCES memberships(id) ON DELETE CASCADE,
    next_due_date       DATE,
    pending_amount      NUMERIC(10,2) NOT NULL DEFAULT 0,
    cached_status       VARCHAR(20) NOT NULL
                          CHECK (cached_status IN ('ACTIVE','DUE_SOON','OVERDUE','EXPIRED','RENEWED')),
    last_computed_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_dues_status_next_due ON dues(cached_status, next_due_date);

-- ============ ATTENDANCE ============
CREATE TABLE attendance (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id       UUID NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    attendance_date DATE NOT NULL,
    check_in_at     TIMESTAMPTZ NOT NULL,
    check_out_at    TIMESTAMPTZ,
    method          VARCHAR(20) NOT NULL DEFAULT 'MANUAL'
                      CHECK (method IN ('MANUAL','QR')),
    UNIQUE (member_id, attendance_date)
);
CREATE INDEX idx_attendance_member_date ON attendance(member_id, attendance_date DESC);
CREATE INDEX idx_attendance_date ON attendance(attendance_date);

-- ============ NOTIFICATIONS ============
CREATE TABLE notifications (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gym_id      UUID NOT NULL REFERENCES gyms(id) ON DELETE CASCADE,
    member_id   UUID REFERENCES members(id) ON DELETE CASCADE,
    type        VARCHAR(30) NOT NULL
                  CHECK (type IN ('DUE_REMINDER','RENEWAL_REMINDER','PAYMENT_CONFIRMATION','ATTENDANCE_ALERT','PROMOTIONAL')),
    channel     VARCHAR(20) NOT NULL CHECK (channel IN ('PUSH','SMS','EMAIL','IN_APP')),
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                  CHECK (status IN ('PENDING','SENT','FAILED')),
    payload     JSONB,
    sent_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_notifications_gym_status_created ON notifications(gym_id, status, created_at);

-- ============ AUDIT LOGS ============
CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_user_id   UUID REFERENCES users(id),
    action          VARCHAR(50) NOT NULL,
    entity_type     VARCHAR(50) NOT NULL,
    entity_id       UUID,
    before_data     JSONB,
    after_data      JSONB,
    ip_address      VARCHAR(50),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_actor_created ON audit_logs(actor_user_id, created_at);
