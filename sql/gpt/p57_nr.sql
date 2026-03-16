-- schema_and_proc.sql
-- PostgreSQL database layer for the email-change flow.
-- Password verification should be done in the application layer using a secure password hash library.
-- This SQL layer enforces:
--   - unique emails
--   - required fields
--   - atomic email update bound to authenticated user id
--   - old email must match current account state

BEGIN;

CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_email_basic_format CHECK (
        email ~* '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$'
    )
);

CREATE INDEX IF NOT EXISTS idx_app_user_email ON app_user (email);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_app_user_updated_at ON app_user;
CREATE TRIGGER trg_app_user_updated_at
BEFORE UPDATE ON app_user
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- Seed example row only if it does not already exist.
-- Replace password hash with one produced by your application.
INSERT INTO app_user (email, password_hash)
SELECT 'user@example.com', 'REPLACE_WITH_REAL_HASH'
WHERE NOT EXISTS (
    SELECT 1 FROM app_user WHERE email = 'user@example.com'
);

-- Atomic email change function.
-- The application should call this only AFTER:
--   1) session/authentication is verified
--   2) password has been verified securely in the application
--   3) inputs have been validated
--
-- Returns TRUE only if exactly one row was updated.
CREATE OR REPLACE FUNCTION change_user_email(
    p_authenticated_user_id BIGINT,
    p_old_email VARCHAR(254),
    p_new_email VARCHAR(254)
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
DECLARE
    v_rows_updated INTEGER;
BEGIN
    IF p_authenticated_user_id IS NULL THEN
        RETURN FALSE;
    END IF;

    IF p_old_email IS NULL OR p_new_email IS NULL THEN
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

    -- Atomic conditional update:
    -- only updates the authenticated user's row,
    -- and only if the current email still matches old_email.
    UPDATE app_user
       SET email = p_new_email
     WHERE id = p_authenticated_user_id
       AND email = p_old_email;

    GET DIAGNOSTICS v_rows_updated = ROW_COUNT;

    IF v_rows_updated = 1 THEN
        RETURN TRUE;
    END IF;

    RETURN FALSE;

EXCEPTION
    WHEN unique_violation THEN
        RETURN FALSE;
END;
$$;

COMMIT;

-- Example usage from application:
-- SELECT change_user_email(123, 'old@example.com', 'new@example.com');