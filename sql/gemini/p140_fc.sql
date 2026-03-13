-- Language: SQL (Stored Procedure)
CREATE OR REPLACE FUNCTION check_user_exists(p_username TEXT)
RETURNS BOOLEAN AS $$
DECLARE
    v_normalized_user TEXT := lower(trim(p_username));
    v_exists BOOLEAN;
BEGIN
    -- Parameterized access via arguments prevents SQL injection
    SELECT EXISTS (
        SELECT 1 FROM users WHERE username = v_normalized_user
    ) INTO v_exists;
    
    RETURN v_exists;
EXCEPTION WHEN OTHERS THEN
    -- Prevent information exposure
    RETURN FALSE;
END;
$$ LANGUAGE plpgsql;