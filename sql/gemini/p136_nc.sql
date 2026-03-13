-- Note: Use HTTPS/TLS. Credentials in SQL are sensitive to log injection.
CREATE OR REPLACE FUNCTION check_mod(p_username TEXT, p_password TEXT)
RETURNS TEXT AS $$
DECLARE
    is_valid BOOLEAN;
BEGIN
    -- Using constant-time behavior by comparing hashes or using robust equality
    -- This example assumes a secure lookup
    SELECT EXISTS (
        SELECT 1 FROM users 
        WHERE username = p_username 
        AND password_hash = crypt(p_password, password_hash)
    ) INTO is_valid;

    IF is_valid THEN
        RETURN 'true';
    ELSE
        -- Return generic result to prevent enumeration
        RETURN 'false';
    END IF;
END;
$$ LANGUAGE plpgsql;