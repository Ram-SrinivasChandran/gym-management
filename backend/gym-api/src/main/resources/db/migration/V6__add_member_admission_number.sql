-- Admission number entered by the admin when registering a member. Unique per gym.
-- Existing members predate this field, so the column is nullable and the uniqueness
-- constraint only applies to non-null values.
ALTER TABLE members
    ADD COLUMN admission_number VARCHAR(30);

CREATE UNIQUE INDEX uq_members_gym_admission_number
    ON members(gym_id, admission_number)
    WHERE admission_number IS NOT NULL;
