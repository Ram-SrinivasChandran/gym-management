-- Seed the single Red Fitness tenant: gym, branch, its only gym-admin (Gokul) and the
-- membership plans from the published fee structure. Idempotent so it is safe on re-run.

-- Drop any gym onboarded ad hoc during earlier testing so Red Fitness is the only tenant.
-- audit_logs.actor_user_id has no ON DELETE rule, so detach those references first; otherwise
-- the cascade from gyms -> users is blocked by audit rows the test onboarding left behind.
UPDATE audit_logs SET actor_user_id = NULL
WHERE actor_user_id IN (
    SELECT u.id FROM users u JOIN gyms g ON u.gym_id = g.id WHERE g.name = 'Demo Gym'
);
DELETE FROM gyms WHERE name = 'Demo Gym';

-- Gym + branch use fixed UUIDs so re-applying this migration is a no-op.
INSERT INTO gyms (id, name, subscription_tier, status)
VALUES ('11111111-1111-1111-1111-111111111111', 'Red Fitness', 'STANDARD', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO branches (id, gym_id, name, address, status)
VALUES ('22222222-2222-2222-2222-222222222222',
        '11111111-1111-1111-1111-111111111111',
        'Main Branch', NULL, 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

-- The single gym admin. Password is hashed with pgcrypto's bcrypt (gen_salt('bf')), which
-- produces a $2a$ hash that Spring Security's BCryptPasswordEncoder verifies natively.
INSERT INTO users (gym_id, branch_id, role_id, email, password_hash, full_name, status)
SELECT '11111111-1111-1111-1111-111111111111',
       '22222222-2222-2222-2222-222222222222',
       (SELECT id FROM roles WHERE code = 'GYM_ADMIN'),
       'gokul@redfitness.com',
       crypt('Gokul@123', gen_salt('bf', 10)),
       'Gokul',
       'ACTIVE'
ON CONFLICT (email) DO NOTHING;

-- Plans from the gym's fee structure. Admission is kept as a line item per request; packages
-- and the limited-period offers are modelled by their total access duration in days.
INSERT INTO membership_plans (gym_id, name, plan_type, duration_days, price, benefits)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'Admission Fee',         'CUSTOM',      1,   1000,  'One-time registration / admission fee'),
  ('11111111-1111-1111-1111-111111111111', 'Monthly',               'MONTHLY',     30,  2000,  NULL),
  ('11111111-1111-1111-1111-111111111111', '3 Months Package',      'QUARTERLY',   90,  4000,  NULL),
  ('11111111-1111-1111-1111-111111111111', '6 Months Package',      'HALF_YEARLY', 180, 6000,  NULL),
  ('11111111-1111-1111-1111-111111111111', '3 + 3 Months Offer',    'CUSTOM',      180, 6000,  'Exclusive limited period offer: 3 + 3 months'),
  ('11111111-1111-1111-1111-111111111111', '6 + 6 Months Offer',    'CUSTOM',      365, 8000,  'Exclusive limited period offer: 6 + 6 months'),
  ('11111111-1111-1111-1111-111111111111', '1 Year + 1 Year Offer', 'CUSTOM',      730, 10000, 'Exclusive limited period offer: 1 year + 1 year')
ON CONFLICT (gym_id, name) DO NOTHING;
