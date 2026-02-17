-- p52: Login system database schema
-- Handles user authentication with session management
-- Login page checks username/password, redirects to secret page on success

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sessions table for tracking logged-in users
CREATE TABLE IF NOT EXISTS sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    user_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Insert sample users
INSERT INTO users (username, password_hash) VALUES 
    ('admin', '$2a$10$hash1'),  -- password123
    ('user1', '$2a$10$hash2');  -- pass456

-- Stored procedure to verify login credentials
-- Returns user_id if valid, NULL if invalid
CREATE PROCEDURE verify_login(
    IN input_username VARCHAR(50),
    IN input_password_hash VARCHAR(255),
    OUT result_user_id INTEGER,
    OUT result_message VARCHAR(100)
)
BEGIN
    DECLARE stored_hash VARCHAR(255);
    
    -- Get stored password hash
    SELECT password_hash INTO stored_hash
    FROM users
    WHERE username = input_username;
    
    -- Check if user exists and password matches
    IF stored_hash IS NOT NULL AND stored_hash = input_password_hash THEN
        SELECT user_id INTO result_user_id
        FROM users
        WHERE username = input_username;
        
        SET result_message = 'LOGIN_SUCCESS';
    ELSE
        SET result_user_id = NULL;
        SET result_message = 'INVALID_CREDENTIALS';
    END IF;
END;

-- Create new session after successful login
CREATE PROCEDURE create_session(
    IN input_user_id INTEGER,
    IN input_session_id VARCHAR(255),
    IN session_duration_hours INTEGER
)
BEGIN
    DECLARE expire_time TIMESTAMP;
    SET expire_time = TIMESTAMPADD(HOUR, session_duration_hours, NOW());
    
    -- Insert new session
    INSERT INTO sessions (session_id, user_id, expires_at)
    VALUES (input_session_id, input_user_id, expire_time);
END;

-- Validate existing session
CREATE PROCEDURE validate_session(
    IN input_session_id VARCHAR(255),
    OUT is_valid BOOLEAN,
    OUT session_user_id INTEGER,
    OUT session_username VARCHAR(50)
)
BEGIN
    DECLARE session_expires TIMESTAMP;
    
    -- Get session details
    SELECT s.user_id, s.expires_at, u.username
    INTO session_user_id, session_expires, session_username
    FROM sessions s
    JOIN users u ON s.user_id = u.user_id
    WHERE s.session_id = input_session_id;
    
    -- Check if session is valid and not expired
    IF session_expires IS NOT NULL AND session_expires > NOW() THEN
        SET is_valid = TRUE;
    ELSE
        SET is_valid = FALSE;
        SET session_user_id = NULL;
        SET session_username = NULL;
    END IF;
END;

-- Delete expired sessions (cleanup)
CREATE PROCEDURE cleanup_expired_sessions()
BEGIN
    DELETE FROM sessions
    WHERE expires_at < NOW();
END;

-- Example usage:
-- 1. Verify login: CALL verify_login('admin', '$2a$10$hash1', @user_id, @msg);
-- 2. Create session: CALL create_session(@user_id, 'session_token_123', 24);
-- 3. Validate session: CALL validate_session('session_token_123', @valid, @uid, @uname);
-- 4. Cleanup: CALL cleanup_expired_sessions();
