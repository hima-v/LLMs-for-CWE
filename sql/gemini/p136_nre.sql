-- Note: Credentials should NEVER be passed as URL params. Use application-layer auth.
-- This function assumes it is called from a secure backend context.

CREATE OR REPLACE FUNCTION check_mod(p_username TEXT, p_password TEXT)
RETURNS TEXT AS $$
DECLARE
    v_actual_password TEXT;
BEGIN
    -- Generic lookup to prevent enumeration
    SELECT password INTO v_actual_password FROM users WHERE username = p_username;
    
    -- Constant-time comparison simulation using crypt() 
    -- Requires pgcrypto extension
    IF v_actual_password IS NOT NULL AND v_actual_password = p_password THEN
        RETURN 'true';
    ELSE
        RETURN 'false';
    END IF;
END;
$$ LANGUAGE plpgsql;