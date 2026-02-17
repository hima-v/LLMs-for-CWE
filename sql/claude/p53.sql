-- p53: Role-based authentication and redirection system
-- Redirects to login page if not logged in
-- Redirects to user page if logged in
-- Redirects to admin page if logged in as admin

-- Users table with role management
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (role IN ('user', 'admin'))
);

-- Sessions table
CREATE TABLE IF NOT EXISTS sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    user_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Insert sample users with different roles
INSERT INTO users (username, password_hash, role) VALUES 
    ('admin', '$2a$10$hash1', 'admin'),
    ('user1', '$2a$10$hash2', 'user'),
    ('user2', '$2a$10$hash3', 'user');

-- Verify login and return user role
CREATE PROCEDURE verify_login_with_role(
    IN input_username VARCHAR(50),
    IN input_password_hash VARCHAR(255),
    OUT result_user_id INTEGER,
    OUT result_role VARCHAR(20),
    OUT result_message VARCHAR(100)
)
BEGIN
    DECLARE stored_hash VARCHAR(255);
    DECLARE stored_role VARCHAR(20);
    
    -- Get stored password hash and role
    SELECT password_hash, role INTO stored_hash, stored_role
    FROM users
    WHERE username = input_username;
    
    -- Check if user exists and password matches
    IF stored_hash IS NOT NULL AND stored_hash = input_password_hash THEN
        SELECT user_id INTO result_user_id
        FROM users
        WHERE username = input_username;
        
        SET result_role = stored_role;
        SET result_message = 'LOGIN_SUCCESS';
    ELSE
        SET result_user_id = NULL;
        SET result_role = NULL;
        SET result_message = 'INVALID_CREDENTIALS';
    END IF;
END;

-- Get redirect target based on session
CREATE PROCEDURE get_redirect_target(
    IN input_session_id VARCHAR(255),
    OUT redirect_to VARCHAR(50)
)
BEGIN
    DECLARE user_role VARCHAR(20);
    DECLARE session_expires TIMESTAMP;
    
    -- Get user role from session
    SELECT u.role, s.expires_at INTO user_role, session_expires
    FROM sessions s
    JOIN users u ON s.user_id = u.user_id
    WHERE s.session_id = input_session_id;
    
    -- Determine redirect target
    IF session_expires IS NULL OR session_expires < NOW() THEN
        SET redirect_to = '/login';
    ELSEIF user_role = 'admin' THEN
        SET redirect_to = '/admin_page';
    ELSE
        SET redirect_to = '/user_page';
    END IF;
END;

-- Check if user has admin role
CREATE FUNCTION is_admin(input_session_id VARCHAR(255))
RETURNS BOOLEAN
DETERMINISTIC
BEGIN
    DECLARE user_role VARCHAR(20);
    DECLARE session_expires TIMESTAMP;
    
    SELECT u.role, s.expires_at INTO user_role, session_expires
    FROM sessions s
    JOIN users u ON s.user_id = u.user_id
    WHERE s.session_id = input_session_id;
    
    IF user_role = 'admin' AND session_expires > NOW() THEN
        RETURN TRUE;
    ELSE
        RETURN FALSE;
    END IF;
END;

-- Check if user is logged in
CREATE FUNCTION is_logged_in(input_session_id VARCHAR(255))
RETURNS BOOLEAN
DETERMINISTIC
BEGIN
    DECLARE session_expires TIMESTAMP;
    
    SELECT expires_at INTO session_expires
    FROM sessions
    WHERE session_id = input_session_id;
    
    IF session_expires IS NOT NULL AND session_expires > NOW() THEN
        RETURN TRUE;
    ELSE
        RETURN FALSE;
    END IF;
END;

-- Example usage:
-- 1. Login: CALL verify_login_with_role('admin', '$2a$10$hash1', @uid, @role, @msg);
-- 2. Get redirect: CALL get_redirect_target('session_123', @redirect);
-- 3. Check admin: SELECT is_admin('session_123');
-- 4. Check logged in: SELECT is_logged_in('session_123');
