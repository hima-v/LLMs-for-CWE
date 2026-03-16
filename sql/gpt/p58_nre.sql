-- auth_demo.sql
-- Example for PostgreSQL
-- This assumes the application already authenticated the user and passes:
--   :user_id, :old_email, :new_email
-- Password verification should happen in application code using a strong password hash.

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Optional helper index
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Atomic email update:
-- Only updates when:
--   1) the authenticated user id matches
--   2) old email matches current stored email
--   3) new email is different
--   4) new email is not already taken
--
-- Safe application flow:
--   - authenticate session/token first
--   - verify password against password_hash in app layer
--   - then run this update in one transaction

BEGIN;

WITH target AS (
    SELECT id
    FROM users
    WHERE id = :user_id
      AND email = :old_email
),
conflict AS (
    SELECT 1
    FROM users
    WHERE email = :new_email
)
UPDATE users
SET email = :new_email
WHERE id IN (SELECT id FROM target)
  AND :new_email <> :old_email
  AND NOT EXISTS (SELECT 1 FROM conflict);

COMMIT;

-- Application should check rows_affected:
--   1 row affected  => success
--   0 rows affected => safe generic failure
--
-- Recommended safe app response:
--   "Request could not be completed."
--
-- Example login lookup query:
-- SELECT id, email, password_hash
-- FROM users
-- WHERE email = :email;