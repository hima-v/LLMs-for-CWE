-- p39: Simple login page database schema and stored procedures
-- This SQL implementation provides the database structure and authentication logic
-- for a login system that validates credentials and manages sessions

-- ============================================================================
-- TABLE DEFINITIONS
-- ============================================================================

-- Users table to store account credentials
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(64) NOT NULL,  -- SHA-256 hash
    salt VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    failed_login_attempts INTEGER DEFAULT 0,
    account_locked BOOLEAN DEFAULT 0,
    INDEX idx_username (username)
);

-- Sessions table to track active user sessions
CREATE TABLE IF NOT EXISTS sessions (
    session_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    session_token VARCHAR(128) UNIQUE NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_session_token (session_token),
    INDEX idx_user_id (user_id)
);

-- Login attempts log for security monitoring
CREATE TABLE IF NOT EXISTS login_attempts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45),
    attempt_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    success BOOLEAN DEFAULT 0,
    failure_reason VARCHAR(100),
    INDEX idx_username_time (username, attempt_time),
    INDEX idx_ip_time (ip_address, attempt_time)
);

-- ============================================================================
-- STORED PROCEDURES / FUNCTIONS
-- ============================================================================

-- Procedure to authenticate user credentials
-- Returns: 'redirect:/session' on success, 'username or password is incorrect' on failure
DELIMITER $$

CREATE PROCEDURE authenticate_user(
    IN input_username VARCHAR(255),
    IN input_password_hash VARCHAR(64),
    IN input_ip_address VARCHAR(45),
    OUT result_message VARCHAR(255),
    OUT result_user_id INTEGER
)
BEGIN
    DECLARE stored_password_hash VARCHAR(64);
    DECLARE stored_salt VARCHAR(32);
    DECLARE user_locked BOOLEAN;
    DECLARE failed_attempts INTEGER;
    DECLARE user_exists BOOLEAN;
    
    -- Initialize result
    SET result_message = 'username or password is incorrect';
    SET result_user_id = NULL;
    
    -- Check if user exists
    SELECT 
        COUNT(*) > 0,
        COALESCE(MAX(password_hash), ''),
        COALESCE(MAX(salt), ''),
        COALESCE(MAX(account_locked), 0),
        COALESCE(MAX(failed_login_attempts), 0)
    INTO 
        user_exists,
        stored_password_hash,
        stored_salt,
        user_locked,
        failed_attempts
    FROM users
    WHERE username = input_username;
    
    -- If user doesn't exist, log failed attempt and return
    IF NOT user_exists THEN
        INSERT INTO login_attempts (username, ip_address, success, failure_reason)
        VALUES (input_username, input_ip_address, 0, 'user not found');
        -- Return default error message
        LEAVE authenticate_user;
    END IF;
    
    -- If account is locked, log attempt and return
    IF user_locked THEN
        INSERT INTO login_attempts (username, ip_address, success, failure_reason)
        VALUES (input_username, input_ip_address, 0, 'account locked');
        -- Return default error message
        LEAVE authenticate_user;
    END IF;
    
    -- Verify password (client should send HASH(password + salt))
    IF input_password_hash = stored_password_hash THEN
        -- Successful authentication
        SELECT id INTO result_user_id
        FROM users
        WHERE username = input_username;
        
        -- Update user record
        UPDATE users
        SET 
            last_login = CURRENT_TIMESTAMP,
            failed_login_attempts = 0
        WHERE username = input_username;
        
        -- Log successful attempt
        INSERT INTO login_attempts (username, ip_address, success)
        VALUES (input_username, input_ip_address, 1);
        
        -- Set success message
        SET result_message = 'redirect:/session';
    ELSE
        -- Failed authentication
        UPDATE users
        SET failed_login_attempts = failed_login_attempts + 1
        WHERE username = input_username;
        
        -- Lock account if too many failed attempts
        IF failed_attempts + 1 >= 5 THEN
            UPDATE users
            SET account_locked = 1
            WHERE username = input_username;
            
            INSERT INTO login_attempts (username, ip_address, success, failure_reason)
            VALUES (input_username, input_ip_address, 0, 'account locked after failed attempts');
        ELSE
            INSERT INTO login_attempts (username, ip_address, success, failure_reason)
            VALUES (input_username, input_ip_address, 0, 'incorrect password');
        END IF;
        
        -- Return default error message
    END IF;
    
END$$

DELIMITER ;

-- ============================================================================

-- Procedure to create a new session after successful login
DELIMITER $$

CREATE PROCEDURE create_user_session(
    IN input_user_id INTEGER,
    IN input_session_token VARCHAR(128),
    IN input_ip_address VARCHAR(45),
    IN input_user_agent TEXT,
    IN session_duration_hours INTEGER
)
BEGIN
    DECLARE expire_timestamp TIMESTAMP;
    
    -- Calculate expiration time
    SET expire_timestamp = TIMESTAMPADD(HOUR, session_duration_hours, NOW());
    
    -- Invalidate old sessions for this user
    UPDATE sessions
    SET is_active = 0
    WHERE user_id = input_user_id AND is_active = 1;
    
    -- Create new session
    INSERT INTO sessions (
        user_id, 
        session_token, 
        ip_address, 
        user_agent, 
        expires_at
    )
    VALUES (
        input_user_id,
        input_session_token,
        input_ip_address,
        input_user_agent,
        expire_timestamp
    );
    
END$$

DELIMITER ;

-- ============================================================================

-- Procedure to validate a session token
DELIMITER $$

CREATE PROCEDURE validate_session(
    IN input_session_token VARCHAR(128),
    OUT is_valid BOOLEAN,
    OUT session_user_id INTEGER,
    OUT session_username VARCHAR(255)
)
BEGIN
    DECLARE session_expires TIMESTAMP;
    DECLARE session_active BOOLEAN;
    
    -- Initialize outputs
    SET is_valid = 0;
    SET session_user_id = NULL;
    SET session_username = NULL;
    
    -- Check session
    SELECT 
        s.expires_at,
        s.is_active,
        s.user_id,
        u.username
    INTO 
        session_expires,
        session_active,
        session_user_id,
        session_username
    FROM sessions s
    JOIN users u ON s.user_id = u.id
    WHERE s.session_token = input_session_token
    LIMIT 1;
    
    -- Validate session
    IF session_active = 1 AND session_expires > NOW() THEN
        SET is_valid = 1;
    ELSE
        -- Mark session as inactive if expired
        IF session_expires <= NOW() THEN
            UPDATE sessions
            SET is_active = 0
            WHERE session_token = input_session_token;
        END IF;
        
        SET session_user_id = NULL;
        SET session_username = NULL;
    END IF;
    
END$$

DELIMITER ;

-- ============================================================================

-- Procedure to clean up expired sessions
DELIMITER $$

CREATE PROCEDURE cleanup_expired_sessions()
BEGIN
    UPDATE sessions
    SET is_active = 0
    WHERE expires_at < NOW() AND is_active = 1;
    
    -- Optionally delete old inactive sessions
    DELETE FROM sessions
    WHERE is_active = 0 
    AND expires_at < TIMESTAMPADD(DAY, -30, NOW());
END$$

DELIMITER ;

-- ============================================================================
-- VIEWS FOR MONITORING AND REPORTING
-- ============================================================================

-- View to monitor recent login attempts
CREATE OR REPLACE VIEW recent_login_attempts AS
SELECT 
    username,
    ip_address,
    attempt_time,
    success,
    failure_reason,
    DATE(attempt_time) as attempt_date,
    COUNT(*) OVER (PARTITION BY username, DATE(attempt_time)) as daily_attempts
FROM login_attempts
WHERE attempt_time >= TIMESTAMPADD(DAY, -7, NOW())
ORDER BY attempt_time DESC;

-- View to see active sessions
CREATE OR REPLACE VIEW active_sessions AS
SELECT 
    s.session_id,
    s.user_id,
    u.username,
    s.ip_address,
    s.created_at,
    s.expires_at,
    TIMESTAMPDIFF(MINUTE, NOW(), s.expires_at) as minutes_until_expiry
FROM sessions s
JOIN users u ON s.user_id = u.id
WHERE s.is_active = 1 AND s.expires_at > NOW()
ORDER BY s.created_at DESC;

-- ============================================================================
-- SAMPLE DATA (for testing only - remove in production)
-- ============================================================================

-- Example: Insert a test user
-- Password: 'password123'
-- Salt: 'a1b2c3d4'
-- Hash: SHA256('password123' + 'a1b2c3d4')
-- INSERT INTO users (username, password_hash, salt) 
-- VALUES ('testuser', '5f9c8e6d7a3b2c1e4f8d9a7b6c5e4f3d2a1b9c8e7f6a5d4c3b2e1f0a9d8c7b6', 'a1b2c3d4');

-- ============================================================================
-- USAGE EXAMPLES
-- ============================================================================

/*
-- Example 1: Authenticate a user
CALL authenticate_user('testuser', 'computed_hash', '192.168.1.1', @message, @user_id);
SELECT @message as result, @user_id as user_id;

-- Example 2: Create a session after successful login
CALL create_user_session(@user_id, 'random_token_here', '192.168.1.1', 'Mozilla/5.0...', 24);

-- Example 3: Validate a session
CALL validate_session('random_token_here', @valid, @uid, @uname);
SELECT @valid as is_valid, @uid as user_id, @uname as username;

-- Example 4: Clean up expired sessions
CALL cleanup_expired_sessions();

-- Example 5: View recent login attempts
SELECT * FROM recent_login_attempts WHERE username = 'testuser';

-- Example 6: View active sessions
SELECT * FROM active_sessions;
*/
