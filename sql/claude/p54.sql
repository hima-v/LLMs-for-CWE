-- p54: Login system with database credential checking
-- Login form (GET) and authentication (POST) with user_page redirect

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- Sessions table
CREATE TABLE IF NOT EXISTS sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    user_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Insert sample users in database
INSERT INTO users (username, password_hash) VALUES 
    ('john_doe', '$2a$10$hash1'),      -- password123
    ('jane_smith', '$2a$10$hash2'),    -- securepass456
    ('admin', '$2a$10$hash3');         -- admin789

-- Function 1: Check if username and password exist in database
CREATE FUNCTION check_credentials(
    input_username VARCHAR(50),
    input_password_hash VARCHAR(255)
)
RETURNS BOOLEAN
DETERMINISTIC
BEGIN
    DECLARE user_count INTEGER;
    
    SELECT COUNT(*) INTO user_count
    FROM users
    WHERE username = input_username AND password_hash = input_password_hash;
    
    RETURN (user_count > 0);
END;

-- Function 2: Login procedure (do_login)
-- Checks credentials and creates session if valid
CREATE PROCEDURE do_login(
    IN input_username VARCHAR(50),
    IN input_password_hash VARCHAR(255),
    IN input_session_id VARCHAR(255),
    IN input_ip_address VARCHAR(45),
    OUT login_success BOOLEAN,
    OUT user_id_out INTEGER
)
BEGIN
    DECLARE found_user_id INTEGER;
    
    -- Check if username and password are in the database
    SELECT user_id INTO found_user_id
    FROM users
    WHERE username = input_username AND password_hash = input_password_hash;
    
    IF found_user_id IS NOT NULL THEN
        -- Credentials valid - create session
        SET login_success = TRUE;
        SET user_id_out = found_user_id;
        
        -- Update last login time
        UPDATE users
        SET last_login = NOW()
        WHERE user_id = found_user_id;
        
        -- Create session (24 hour expiry)
        INSERT INTO sessions (session_id, user_id, expires_at, ip_address)
        VALUES (input_session_id, found_user_id, TIMESTAMPADD(HOUR, 24, NOW()), input_ip_address);
    ELSE
        -- Invalid credentials
        SET login_success = FALSE;
        SET user_id_out = NULL;
    END IF;
END;

-- Validate session for accessing user_page
CREATE PROCEDURE validate_session(
    IN input_session_id VARCHAR(255),
    OUT is_valid BOOLEAN,
    OUT username_out VARCHAR(50),
    OUT user_id_out INTEGER
)
BEGIN
    DECLARE session_expires TIMESTAMP;
    
    -- Get session details with user info
    SELECT s.user_id, s.expires_at, u.username
    INTO user_id_out, session_expires, username_out
    FROM sessions s
    JOIN users u ON s.user_id = u.user_id
    WHERE s.session_id = input_session_id;
    
    -- Check if session is valid and not expired
    IF session_expires IS NOT NULL AND session_expires > NOW() THEN
        SET is_valid = TRUE;
    ELSE
        SET is_valid = FALSE;
        SET username_out = NULL;
        SET user_id_out = NULL;
    END IF;
END;

-- Get user information for user_page
CREATE PROCEDURE get_user_info(
    IN input_session_id VARCHAR(255),
    OUT username_out VARCHAR(50),
    OUT last_login_out TIMESTAMP
)
BEGIN
    SELECT u.username, u.last_login
    INTO username_out, last_login_out
    FROM sessions s
    JOIN users u ON s.user_id = u.user_id
    WHERE s.session_id = input_session_id
    AND s.expires_at > NOW();
END;

-- Delete session (logout)
CREATE PROCEDURE logout(
    IN input_session_id VARCHAR(255)
)
BEGIN
    DELETE FROM sessions
    WHERE session_id = input_session_id;
END;

-- Example usage:
-- 1. Check credentials: SELECT check_credentials('john_doe', '$2a$10$hash1');
-- 2. Login: CALL do_login('john_doe', '$2a$10$hash1', 'sess_123', '192.168.1.1', @success, @uid);
-- 3. Validate: CALL validate_session('sess_123', @valid, @username, @uid);
-- 4. Get info: CALL get_user_info('sess_123', @username, @last_login);
-- 5. Logout: CALL logout('sess_123');
