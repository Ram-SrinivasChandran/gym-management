-- Per-plan discount amount, entered when creating/editing a plan. Existing plans default to 0.
ALTER TABLE membership_plans
    ADD COLUMN discount_amount NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0);
