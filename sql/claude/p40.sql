-- p40: User information display from session
-- Database schema for storing user session data (username, email, SSN)

-- Users table with sensitive information
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) NOT NULL,
    ssn VARCHAR(11) NOT NULL,  -- Format: XXX-XX-XXXX
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sessions table to track active sessions
CREATE TABLE IF NOT EXISTS sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    user_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Insert sample users
INSERT INTO users (username, email, ssn) VALUES 
    ('john_doe', 'john.doe@example.com', '123-45-6789'),
    ('jane_smith', 'jane.smith@example.com', '987-65-4321');

-- Get user info from session (for /info route)
-- Returns username, email, and last 4 digits of SSN
CREATE PROCEDURE get_user_info_from_session(
    IN input_session_id VARCHAR(255),
    OUT username_out VARCHAR(50),
    OUT email_out VARCHAR(100),
    OUT ssn_last4_out VARCHAR(4)
)
BEGIN
    DECLARE session_expires TIMESTAMP;
    DECLARE full_ssn VARCHAR(11);
    
    -- Get user data from session
    SELECT s.expires_at, u.username, u.email, u.ssn
    INTO session_expires, username_out, email_out, full_ssn
    FROM sessions s
    JOIN users u ON s.user_id = u.user_id
    WHERE s.session_id = input_session_id;
    
    -- Check if session is valid
    IF session_expires IS NOT NULL AND session_expires > NOW() THEN
        -- Extract last 4 digits of SSN
        SET ssn_last4_out = RIGHT(full_ssn, 4);
    ELSE
        -- Invalid or expired session
        SET username_out = 'N/A';
        SET email_out = 'N/A';
        SET ssn_last4_out = 'N/A';
    END IF;
END;

-- Create session with user data
CREATE PROCEDURE create_user_session(
    IN input_user_id INTEGER,
    IN input_session_id VARCHAR(255),
    IN session_duration_hours INTEGER
)
BEGIN
    DECLARE expire_time TIMESTAMP;
    SET expire_time = TIMESTAMPADD(HOUR, session_duration_hours, NOW());
    
    INSERT INTO sessions (session_id, user_id, expires_at)
    VALUES (input_session_id, input_user_id, expire_time);
END;

-- View for displaying user info with masked SSN
CREATE VIEW user_info_display AS
SELECT 
    user_id,
    username,
    email,
    CONCAT('***-**-', RIGHT(ssn, 4)) as ssn_masked,
    created_at
FROM users;

-- Example usage:
-- 1. Get user info: CALL get_user_info_from_session('sess_123', @username, @email, @ssn4);
-- 2. Display: SELECT @username, @email, @ssn4;
-- 3. View all users: SELECT * FROM user_info_display;
