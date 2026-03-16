-- change_email.sql
-- PostgreSQL version
-- Requires:
--   CREATE EXTENSION IF NOT EXISTS pgcrypto;
--
-- App rule:
--   Never trust a user-supplied account id.
--   Pass p_authenticated_user_id from the server-side session only.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    failed_email_change_attempts INTEGER NOT NULL DEFAULT 0,
    last_failed_email_change_at TIMESTAMPTZ
);

-- Example of creating a user securely:
-- INSERT INTO app_users (email, password_hash)
-- VALUES ('user@example.com', crypt('StrongPassword123!', gen_salt('bf')));

CREATE OR REPLACE FUNCTION change_user_email(
    p_authenticated_user_id BIGINT,
    p_old_email TEXT,
    p_new_email TEXT,
    p_password TEXT
)
RETURNS TABLE(success BOOLEAN, message TEXT)
LANGUAGE plpgsql
AS $$
DECLARE
    v_current_email TEXT;
    v_password_hash TEXT;
    v_new_email_taken BOOLEAN;
BEGIN
    -- Server-side session/login check
    IF p_authenticated_user_id IS NULL THEN
        RETURN QUERY SELECT FALSE, 'Unable to process request.';
        RETURN;
    END IF;

    -- Basic input validation
    IF p_old_email IS NULL OR p_new_email IS NULL OR p_password IS NULL THEN
        RETURN QUERY SELECT FALSE, 'Unable to process request.';
        RETURN;
    END IF;

    p_old_email := lower(trim(p_old_email));
    p_new_email := lower(trim(p_new_email));

    IF p_old_email = '' OR p_new_email = '' OR p_password = '' THEN
        RETURN QUERY SELECT FALSE, 'Unable to process request.';
        RETURN;
    END IF;

    IF p_old_email !~ '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$'
       OR p_new_email !~ '^[^[:space:]@]+@[^[:space:]@]+\.[^[:space:]@]+$' THEN
        RETURN QUERY SELECT FALSE, 'Unable to process request.';
        RETURN;
    END IF;

    IF p_old_email = p_new_email THEN
        RETURN QUERY SELECT FALSE, 'New email must be different.';
        RETURN;
    END IF;

    -- Lock the authenticated row so update stays safe
    SELECT email, password_hash
      INTO v_current_email, v_password_hash
      FROM app_users
     WHERE id = p_authenticated_user_id
     FOR UPDATE;

    IF NOT FOUND THEN
        RETURN QUERY SELECT FALSE, 'Unable to process request.';
        RETURN;
    END IF;

    -- Old email must match authenticated user's current email
    IF v_current_email <> p_old_email THEN
        UPDATE app_users
           SET failed_email_change_attempts = failed_email_change_attempts + 1,
               last_failed_email_change_at = NOW()
         WHERE id = p_authenticated_user_id;

        RETURN QUERY SELECT FALSE, 'Unable to process request.';
        RETURN;
    END IF;

    -- Secure password verification using pgcrypto
    IF v_password_hash <> crypt(p_password, v_password_hash) THEN
        UPDATE app_users
           SET failed_email_change_attempts = failed_email_change_attempts + 1,
               last_failed_email_change_at = NOW()
         WHERE id = p_authenticated_user_id;

        RETURN QUERY SELECT FALSE, 'Unable to process request.';
        RETURN;
    END IF;

    SELECT EXISTS (
        SELECT 1 FROM app_users WHERE email = p_new_email
    ) INTO v_new_email_taken;

    IF v_new_email_taken THEN
        RETURN QUERY SELECT FALSE, 'Unable to process request.';
        RETURN;
    END IF;

    UPDATE app_users
       SET email = p_new_email,
           failed_email_change_attempts = 0,
           last_failed_email_change_at = NULL
     WHERE id = p_authenticated_user_id
       AND email = p_old_email;

    IF NOT FOUND THEN
        RETURN QUERY SELECT FALSE, 'Unable to process request.';
        RETURN;
    END IF;

    RETURN QUERY SELECT TRUE, 'Email updated successfully.';
END;
$$;

-- Example usage from application code:
-- SELECT * FROM change_user_email(
--   p_authenticated_user_id := 123,      -- from server session only
--   p_old_email := 'old@example.com',
--   p_new_email := 'new@example.com',
--   p_password := 'user-entered-password'
-- );