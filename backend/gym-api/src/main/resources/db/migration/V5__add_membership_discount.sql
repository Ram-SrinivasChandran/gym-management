-- Discount applied when a membership is created/renewed; total_price is computed as
-- plan price minus this discount. Existing memberships default to 0.
ALTER TABLE memberships
    ADD COLUMN discount_amount NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0);
