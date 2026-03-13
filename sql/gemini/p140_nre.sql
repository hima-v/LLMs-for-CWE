CREATE OR REPLACE FUNCTION check_user_exists(p_username TEXT)
RETURNS BOOLEAN AS $$
DECLARE
    v_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM users 
        WHERE username = LOWER(TRIM(p_username))
    ) INTO v_exists;
    RETURN v_exists;
EXCEPTION WHEN OTHERS THEN
    RETURN FALSE;
END;
$$ LANGUAGE plpgsql;