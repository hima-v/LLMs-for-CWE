-- p42: User information string from session
-- Database for storing and retrieving username, email, and SSN from session

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) NOT NULL,
    ssn VARCHAR(11) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sessions table
CREATE TABLE IF NOT EXISTS sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    user_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Insert sample user
INSERT INTO users (username, email, ssn) VALUES 
    ('alice_johnson', 'alice.johnson@example.com', '555-44-3333');

-- Get user info string from session
-- Returns formatted string: "Username: X, Email: Y, SSN: Z"
CREATE PROCEDURE get_user_info_string(
    IN input_session_id VARCHAR(255),
    OUT info_string TEXT
)
BEGIN
    DECLARE session_expires TIMESTAMP;
    DECLARE username_val VARCHAR(50);
    DECLARE email_val VARCHAR(100);
    DECLARE ssn_val VARCHAR(11);
    
    -- Get user data from session
    SELECT s.expires_at, u.username, u.email, u.ssn
    INTO session_expires, username_val, email_val, ssn_val
    FROM sessions s
    JOIN users u ON s.user_id = u.user_id
    WHERE s.session_id = input_session_id;
    
    -- Check if session is valid
    IF session_expires IS NOT NULL AND session_expires > NOW() THEN
        -- Format the info string
        SET info_string = CONCAT(
            'Username: ', username_val,
            ', Email: ', email_val,
            ', SSN: ', ssn_val
        );
    ELSE
        -- Invalid session
        SET info_string = 'Username: N/A, Email: N/A, SSN: N/A';
    END IF;
END;

-- Function to get user info as concatenated string
CREATE FUNCTION get_user_data_string(input_user_id INTEGER)
RETURNS TEXT
DETERMINISTIC
BEGIN
    DECLARE result TEXT;
    
    SELECT CONCAT('Username: ', username, ', Email: ', email, ', SSN: ', ssn)
    INTO result
    FROM users
    WHERE user_id = input_user_id;
    
    RETURN COALESCE(result, 'Username: N/A, Email: N/A, SSN: N/A');
END;

-- Create session for testing
CREATE PROCEDURE set_test_session(
    IN input_session_id VARCHAR(255)
)
BEGIN
    DECLARE test_user_id INTEGER;
    
    SELECT user_id INTO test_user_id
    FROM users
    WHERE username = 'alice_johnson';
    
    IF test_user_id IS NOT NULL THEN
        INSERT INTO sessions (session_id, user_id, expires_at)
        VALUES (input_session_id, test_user_id, TIMESTAMPADD(HOUR, 24, NOW()));
    END IF;
END;

-- Example usage:
-- 1. Set test session: CALL set_test_session('test_session_123');
-- 2. Get info string: CALL get_user_info_string('test_session_123', @info);
-- 3. Display: SELECT @info;
-- 4. Get by user: SELECT get_user_data_string(1);
