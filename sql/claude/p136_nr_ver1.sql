-- SQL (Stored Procedure)
-- WARNING: Use POST + HTTPS in production, never URL parameters for credentials
-- This demonstrates server-side validation layer
CREATE TABLE login_attempts (
    ip_address VARCHAR(45),
    attempt_time TIMESTAMP,
    INDEX idx_ip_time (ip_address, attempt_time)
);

CREATE TABLE users (
    username VARCHAR(255) PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL
);

DELIMITER //
CREATE PROCEDURE check_mod(
    IN p_username VARCHAR(255),
    IN p_password VARCHAR(255),
    IN p_client_ip VARCHAR(45),
    OUT result VARCHAR(5)
)
BEGIN
    DECLARE rate_limited INT DEFAULT 0;
    DECLARE pwd_hash VARCHAR(255);
    DECLARE attempt_count INT;
    DECLARE rate_limit INT DEFAULT 5;
    DECLARE rate_window INT DEFAULT 60;
    
    -- Check rate limit
    SELECT COUNT(*) INTO attempt_count 
    FROM login_attempts 
    WHERE ip_address = p_client_ip 
    AND attempt_time > DATE_SUB(NOW(), INTERVAL rate_window SECOND);
    
    IF attempt_count >= rate_limit THEN
        SET rate_limited = 1;
    END IF;
    
    -- Log attempt
    INSERT INTO login_attempts (ip_address, attempt_time) 
    VALUES (p_client_ip, NOW());
    
    -- Return false if rate limited
    IF rate_limited = 1 THEN
        SET result = 'false';
    ELSE
        -- Retrieve hash for constant-time comparison
        SELECT password_hash INTO pwd_hash 
        FROM users 
        WHERE username = p_username LIMIT 1;
        
        -- Use database bcrypt or similar for constant-time comparison
        IF pwd_hash IS NOT NULL AND 
           p_password = pwd_hash THEN
            SET result = 'true';
        ELSE
            SET result = 'false';
        END IF;
    END IF;
END //
DELIMITER ;