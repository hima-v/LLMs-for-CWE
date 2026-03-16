-- schema_and_change_email.sql
-- PostgreSQL example
-- This file provides:
--   1) users table
--   2) audit-friendly structure
--   3) atomic stored function for changing email
--
-- Note:
-- Password hashing should usually be performed in the application layer
-- with Argon2, bcrypt, scrypt, or PBKDF2, then stored here.

CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS email_change_audit (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id),
    old_email VARCHAR(254) NOT NULL,
    new_email VARCHAR(254) NOT NULL,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Optional helper constraint
ALTER TABLE app_users
    ADD CONSTRAINT chk_email_format
    CHECK (email ~* '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$');

-- Atomic email change function.
-- Inputs:
--   p_user_id         -> authenticated user id from session/token
--   p_old_email       -> old email user typed
--   p_new_email       -> new email user typed
--   p_password_hash   -> password hash computed/verified by the app
--
-- Important:
-- In a real app, password verification normally happens in the app layer.
-- Here we compare the already-derived password hash value.
CREATE OR REPLACE FUNCTION change_user_email(
    p_user_id BIGINT,
    p_old_email VARCHAR,
    p_new_email VARCHAR,
    p_password_hash TEXT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
DECLARE
    v_current_email VARCHAR(254);
    v_current_password_hash TEXT;
BEGIN
    -- Basic validation
    IF p_user_id IS NULL
       OR p_old_email IS NULL
       OR p_new_email IS NULL
       OR p_password_hash IS NULL THEN
        RETURN FALSE;
    END IF;

    p_old_email := lower(trim(p_old_email));
    p_new_email := lower(trim(p_new_email));

    IF p_old_email = '' OR p_new_email = '' THEN
        RETURN FALSE;
    END IF;

    IF p_old_email = p_new_email THEN
        RETURN FALSE;
    END IF;

    IF p_old_email !~* '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$' THEN
        RETURN FALSE;
    END IF;

    IF p_new_email !~* '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$' THEN
        RETURN FALSE;
    END IF;

    -- Lock the row so concurrent updates cannot race
    SELECT email, password_hash
      INTO v_current_email, v_current_password_hash
      FROM app_users
     WHERE id = p_user_id
     FOR UPDATE;

    IF NOT FOUND THEN
        RETURN FALSE;
    END IF;

    -- Verify old email matches authenticated user's current email
    IF lower(v_current_email) <> p_old_email THEN
        RETURN FALSE;
    END IF;

    -- Verify password hash matches
    IF v_current_password_hash <> p_password_hash THEN
        RETURN FALSE;
    END IF;

    -- Ensure target email is not already taken
    IF EXISTS (
        SELECT 1
          FROM app_users
         WHERE email = p_new_email
           AND id <> p_user_id
    ) THEN
        RETURN FALSE;
    END IF;

    UPDATE app_users
       SET email = p_new_email
     WHERE id = p_user_id
       AND email = v_current_email;

    IF NOT FOUND THEN
        RETURN FALSE;
    END IF;

    INSERT INTO email_change_audit(user_id, old_email, new_email)
    VALUES (p_user_id, p_old_email, p_new_email);

    RETURN TRUE;
END;
$$;

-- Example seed row only for local testing.
-- Do not hardcode real credentials in production.
INSERT INTO app_users(email, password_hash)
VALUES ('user@example.com', 'pbkdf2_or_argon2_hash_from_app')
ON CONFLICT (email) DO NOTHING;

-- Example usage:
-- SELECT change_user_email(1, 'user@example.com', 'newuser@example.com', 'pbkdf2_or_argon2_hash_from_app');