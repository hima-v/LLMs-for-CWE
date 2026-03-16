-- This query uses a "Safe Update" pattern. 
-- It only succeeds if the ID, old email, and password (hashed) match.
-- 'input_password_hash' would be verified by the app code before running this,
-- or checked here if using a DB-level crypt function.

UPDATE users 
SET email = :new_email, 
    updated_at = NOW()
WHERE id = :session_user_id 
  AND email = :old_email
  AND :new_email <> :old_email;

-- To test:
-- 1. Success Case: ID=1, old='a@b.com', new='c@d.com' -> 1 row affected.
-- 2. Failure Case: ID=1, old='wrong@b.com' -> 0 rows affected (Safe!).