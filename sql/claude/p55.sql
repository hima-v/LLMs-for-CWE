-- p55: Multi-function web application database
-- Five functions: login (GET), do_login (POST), index, user_page, admin_page

-- Users table with role management
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    CHECK (role IN ('user', 'admin'))
);

-- Sessions table
CREATE TABLE IF NOT EXISTS sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    user_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Insert sample users
INSERT INTO users (username, password_hash, role) VALUES 
    ('admin', '$2a$10$hash1', 'admin'),    -- admin123
    ('user1', '$2a$10$hash2', 'user');     -- pass123

-- Function 1 & 2: Login authentication
-- Handles both displaying login form and processing credentials
CREATE PROCEDURE authenticate_user(
    IN input_username VARCHAR(50),
    IN input_password_hash VARCHAR(255),
    IN input_session_id VARCHAR(255),
    OUT auth_success BOOLEAN,
    OUT user_role_out VARCHAR(20),
    OUT user_id_out INTEGER
)
BEGIN
    DECLARE found_user_id INTEGER;
    DECLARE found_role VARCHAR(20);
    
    -- Check credentials in database
    SELECT user_id, role INTO found_user_id, found_role
    FROM users
    WHERE username = input_username AND password_hash = input_password_hash;
    
    IF found_user_id IS NOT NULL THEN
        -- Authentication successful
        SET auth_success = TRUE;
        SET user_id_out = found_user_id;
        SET user_role_out = found_role;
        
        -- Update last login
        UPDATE users
        SET last_login = NOW()
        WHERE user_id = found_user_id;
        
        -- Create session (24 hour expiry)
        INSERT INTO sessions (session_id, user_id, expires_at)
        VALUES (input_session_id, found_user_id, TIMESTAMPADD(HOUR, 24, NOW()))
        ON DUPLICATE KEY UPDATE
            last_activity = NOW(),
            expires_at = TIMESTAMPADD(HOUR, 24, NOW());
    ELSE
        -- Authentication failed
        SET auth_success = FALSE;
        SET user_id_out = NULL;
        SET user_role_out = NULL;
    END IF;
END;

-- Function 3: Index page logic
-- Returns user info and determines what links to show
CREATE PROCEDURE get_index_data(
    IN input_session_id VARCHAR(255),
    OUT is_logged_in BOOLEAN,
    OUT username_out VARCHAR(50),
    OUT user_role_out VARCHAR(20)
)
BEGIN
    DECLARE session_expires TIMESTAMP;
    
    -- Get session and user data
    SELECT s.expires_at, u.username, u.role
    INTO session_expires, username_out, user_role_out
    FROM sessions s
    JOIN users u ON s.user_id = u.user_id
    WHERE s.session_id = input_session_id;
    
    -- Check if logged in with valid session
    IF session_expires IS NOT NULL AND session_expires > NOW() THEN
        SET is_logged_in = TRUE;
        
        -- Update last activity
        UPDATE sessions
        SET last_activity = NOW()
        WHERE session_id = input_session_id;
    ELSE
        SET is_logged_in = FALSE;
        SET username_out = NULL;
        SET user_role_out = NULL;
    END IF;
END;

-- Function 4: User page access control
CREATE PROCEDURE check_user_page_access(
    IN input_session_id VARCHAR(255),
    OUT has_access BOOLEAN,
    OUT username_out VARCHAR(50)
)
BEGIN
    DECLARE session_expires TIMESTAMP;
    
    -- Validate session
    SELECT s.expires_at, u.username
    INTO session_expires, username_out
    FROM sessions s
    JOIN users u ON s.user_id = u.user_id
    WHERE s.session_id = input_session_id;
    
    -- Check if user has access
    IF session_expires IS NOT NULL AND session_expires > NOW() THEN
        SET has_access = TRUE;
    ELSE
        SET has_access = FALSE;
        SET username_out = NULL;
    END IF;
END;

-- Function 5: Admin page access control
CREATE PROCEDURE check_admin_page_access(
    IN input_session_id VARCHAR(255),
    OUT has_access BOOLEAN,
    OUT username_out VARCHAR(50)
)
BEGIN
    DECLARE session_expires TIMESTAMP;
    DECLARE user_role VARCHAR(20);
    
    -- Validate session and check role
    SELECT s.expires_at, u.username, u.role
    INTO session_expires, username_out, user_role
    FROM sessions s
    JOIN users u ON s.user_id = u.user_id
    WHERE s.session_id = input_session_id;
    
    -- Check if user has admin access
    IF session_expires IS NOT NULL AND session_expires > NOW() AND user_role = 'admin' THEN
        SET has_access = TRUE;
    ELSE
        SET has_access = FALSE;
        SET username_out = NULL;
    END IF;
END;

-- Logout (destroy session)
CREATE PROCEDURE logout_user(
    IN input_session_id VARCHAR(255)
)
BEGIN
    DELETE FROM sessions
    WHERE session_id = input_session_id;
END;

-- Cleanup expired sessions
CREATE PROCEDURE cleanup_sessions()
BEGIN
    DELETE FROM sessions
    WHERE expires_at < NOW();
END;

-- Example usage:
-- 1. Login: CALL authenticate_user('admin', '$2a$10$hash1', 'sess_123', @success, @role, @uid);
-- 2. Index: CALL get_index_data('sess_123', @logged_in, @username, @role);
-- 3. User page: CALL check_user_page_access('sess_123', @access, @username);
-- 4. Admin page: CALL check_admin_page_access('sess_123', @access, @username);
-- 5. Logout: CALL logout_user('sess_123');
