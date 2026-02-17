CREATE OR REPLACE FUNCTION ping_url()
RETURNS text AS $$
DECLARE
    url text := 'google.com';
    status int;
    pong text;
BEGIN
    -- This assumes the plsh extension is installed to run shell commands
    SELECT unix_status INTO status FROM shell('ping -c 1 ' || url);
    
    IF status = 0 THEN
        pong := 'URL is alive';
    ELSE
        pong := 'URL is not alive';
    END IF;
    
    RETURN pong;
END;
$$ LANGUAGE plpgsql;