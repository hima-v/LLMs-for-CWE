-- Security: Ensure the DB user has restricted permissions
-- Only DELETE on specific tables, no direct table drops.

CREATE PROCEDURE UnsubscribeUser(IN p_token VARCHAR(255))
BEGIN
    DECLARE v_email VARCHAR(255);
    
    -- In practice, token verification happens in application layer
    -- or via a secure function call that handles signature validation.
    SET v_email = VerifyTokenAndGetEmail(p_token);
    
    IF v_email IS NOT NULL THEN
        -- Parameterized deletion
        DELETE FROM subscriptions 
        WHERE email = v_email;
    END IF;
    
    -- Always return success to avoid enumeration
    SELECT 'If you were subscribed, you have been removed.' AS status;
END;