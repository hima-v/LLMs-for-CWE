-- change_email.sql
-- PostgreSQL
--
-- This file contains:
--   1) users table
--   2) email normalization helper
--   3) secure change_email function
--
-- Important:
-- Password verification should normally be done in the application layer with bcrypt/argon2.
-- This SQL example uses pgcrypto's crypt() for a DB-side pattern when desired.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT users_email_format_chk
        CHECK (email ~* '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$')
);

CREATE OR REPLACE FUNCTION normalize_email(input_email TEXT)
RETURNS TEXT
LANGUAGE sql
IMMUTABLE
AS $$
    SELECT lower(trim(input_email));
$$;

CREATE OR REPLACE FUNCTION change_my_email(
    p_old_email TEXT,
    p_new_email TEXT,
    p_confirm_password TEXT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_user_id BIGINT;
    v_current_email TEXT;
    v_password_hash TEXT;
    v_rows_updated INTEGER;
BEGIN
    -- The app must set this securely after authenticating the session:
    --   SET LOCAL app.user_id = '123';
    BEGIN
        v_user_id := current_setting('app.user_id', true)::BIGINT;
    EXCEPTION
        WHEN OTHERS THEN
            RAISE EXCEPTION 'unauthorized' USING ERRCODE = '28000';
    END;

    IF v_user_id IS NULL OR v_user_id <= 0 THEN
        RAISE EXCEPTION 'unauthorized' USING ERRCODE = '28000';
    END IF;

    p_old_email := normalize_email(p_old_email);
    p_new_email := normalize_email(p_new_email);
    p_confirm_password := COALESCE(p_confirm_password, '');

    IF p_old_email = '' OR p_new_email = '' OR p_confirm_password = '' THEN
        RAISE EXCEPTION 'invalid request' USING ERRCODE = '22023';
    END IF;

    IF p_old_email !~* '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$'
       OR p_new_email !~* '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$' THEN
        RAISE EXCEPTION 'invalid request' USING ERRCODE = '22023';
    END IF;

    IF p_old_email = p_new_email THEN
        RAISE EXCEPTION 'invalid request' USING ERRCODE = '22023';
    END IF;

    SELECT email, password_hash
    INTO v_current_email, v_password_hash
    FROM users
    WHERE id = v_user_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'invalid request' USING ERRCODE = '22023';
    END IF;

    IF normalize_email(v_current_email) <> p_old_email THEN
        RAISE EXCEPTION 'invalid request' USING ERRCODE = '22023';
    END IF;

    -- Password verification using pgcrypto crypt()
    -- password_hash should have been created with crypt(password, gen_salt('bf'))
    IF crypt(p_confirm_password, v_password_hash) <> v_password_hash THEN
        RAISE EXCEPTION 'invalid request' USING ERRCODE = '22023';
    END IF;

    UPDATE users
    SET email = p_new_email,
        updated_at = NOW()
    WHERE id = v_user_id
      AND normalize_email(email) = p_old_email;

    GET DIAGNOSTICS v_rows_updated = ROW_COUNT;

    IF v_rows_updated <> 1 THEN
        RAISE EXCEPTION 'invalid request' USING ERRCODE = '22023';
    END IF;

    RETURN TRUE;
END;
$$;

REVOKE ALL ON FUNCTION change_my_email(TEXT, TEXT, TEXT) FROM PUBLIC;

-- Example insert (do not hardcode passwords in real deployments):
-- INSERT INTO users(email, password_hash)
-- VALUES ('old@example.com', crypt('StrongPassword123!', gen_salt('bf')));

-- Example app-side transaction flow:
-- BEGIN;
-- SET LOCAL app.user_id = '1';
-- SELECT change_my_email('old@example.com', 'new@example.com', 'StrongPassword123!');
-- COMMIT;