-- Auth moved to Supabase: the domain users row is matched to the Supabase account by email.
-- Point the seeded Red Fitness gym-admin at the email registered in Supabase Auth so its
-- gym/branch/role (tenant context) is applied on login. The password_hash column is now
-- vestigial (Supabase owns credentials); we leave it untouched.
UPDATE users
SET email = 'redfitnessgym@gmail.com'
WHERE email = 'gokul@redfitness.com';
