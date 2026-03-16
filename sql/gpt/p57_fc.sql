-- email_change.sql
-- PostgreSQL single-file DB layer for:
-- - users table
-- - password hashes stored securely by application
-- - transactional email change function
--
-- Notes:
-- 1) Password hashing should be done in the application layer using Argon2, bcrypt, or PBKDF2.
-- 2) This function assumes you already know the authenticated user id.
-- 3) The application should verify password before calling update_email_for_authenticated_user.

BEGIN;

CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Demo record: password_hash here is just a placeholder.
-- In real use, insert a real Argon2/bcrypt/PBKDF2 hash from the app.
INSERT INTO app_user (email, password_hash)
VALUES ('alice@example.com', '$APP_GENERATED_HASH$')
ON CONFLICT (email) DO NOTHING;

CREATE OR REPLACE FUNCTION update_email_for_authenticated_user(
    p_user_id BIGINT,
    p_old_email VARCHAR,
    p_new_email VARCHAR
)
RETURNS TABLE(success BOOLEAN, message TEXT)
LANGUAGE plpgsql
AS $$
DECLARE
    v_current_email VARCHAR(254);
    v_existing_id BIGINT;
BEGIN
    p_old_email := lower(trim(p_old_email));
    p_new_email := lower(trim(p_new_email));

    IF p_user_id IS NULL THEN
        RETURN QUERY SELECT FALSE, 'Unauthorized.';
        RETURN;
    END IF;

    IF p_old_email IS NULL OR p_new_email IS NULL
       OR length(p_old_email) = 0 OR length(p_new_email) = 0 THEN
        RETURN QUERY SELECT FALSE, 'Invalid input.';
        RETURN;
    END IF;

    IF p_old_email !~ '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$'
       OR p_new_email !~ '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$' THEN
        RETURN QUERY SELECT FALSE, 'Invalid input.';
        RETURN;
    END IF;

    IF p_old_email = p_new_email THEN
        RETURN QUERY SELECT FALSE, 'New email must be different.';
        RETURN;
    END IF;

    SELECT email
      INTO v_current_email
      FROM app_user
     WHERE id = p_user_id
     FOR UPDATE;

    IF NOT FOUND THEN
        RETURN QUERY SELECT FALSE, 'Unauthorized.';
        RETURN;
    END IF;

    IF lower(trim(v_current_email)) <> p_old_email THEN
        RETURN QUERY SELECT FALSE, 'Request could not be completed.';
        RETURN;
    END IF;

    SELECT id
      INTO v_existing_id
      FROM app_user
     WHERE email = p_new_email;

    IF FOUND THEN
        RETURN QUERY SELECT FALSE, 'Request could not be completed.';
        RETURN;
    END IF;

    UPDATE app_user
       SET email = p_new_email
     WHERE id = p_user_id
       AND email = p_old_email;

    IF NOT FOUND THEN
        RETURN QUERY SELECT FALSE, 'Request could not be completed.';
        RETURN;
    END IF;

    RETURN QUERY SELECT TRUE, 'Email updated successfully.';
END;
$$;

COMMIT;

-- Example usage after the app authenticates user and verifies password:
-- SELECT * FROM update_email_for_authenticated_user(1, 'alice@example.com', 'alice.new@example.com');