-- Users table
CREATE TABLE users (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Helpful index for case-insensitive email lookup
CREATE UNIQUE INDEX ux_users_email_lower ON users ((LOWER(email)));

-- Example safe update pattern:
-- Only update if:
-- 1) user is already authenticated in server code
-- 2) old email matches the logged-in user
-- 3) new email is different
-- 4) new email is not already used

UPDATE users
SET email = :new_email
WHERE id = :authenticated_user_id
  AND LOWER(email) = LOWER(:old_email)
  AND LOWER(:old_email) <> LOWER(:new_email)
  AND NOT EXISTS (
      SELECT 1
      FROM users u2
      WHERE LOWER(u2.email) = LOWER(:new_email)
  );