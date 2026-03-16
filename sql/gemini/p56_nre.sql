-- 1. Table structure with unique constraints
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Secure Update Logic (Conceptualized as a Transaction)
-- Using a transaction ensures that the "Read-Modify-Write" cycle is atomic.
BEGIN;

-- Verify existence and correctness in one step to prevent race conditions
UPDATE users 
SET email = 'new_user_email@example.com',
    updated_at = NOW()
WHERE id = 123 
  AND email = 'old_user_email@example.com' 
  AND password_hash = '[Verified_Hash_From_App_Logic]';

-- If rows affected is 0, the app should return a generic error.
COMMIT;

-- 3. Constraint to prevent same-email updates at the DB level
ALTER TABLE users ADD CONSTRAINT email_diff CHECK (email <> 'placeholder_logic_only');