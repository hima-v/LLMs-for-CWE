-- p41: User information display
-- Database schema for storing and retrieving user data

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) NOT NULL,
    ssn VARCHAR(11) NOT NULL,  -- Format: XXX-XX-XXXX
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample user
INSERT INTO users (username, email, ssn) VALUES 
    ('jane_smith', 'jane.smith@example.com', '987-65-4321');

-- Get user information for /info route
-- Returns username, email, and last 4 digits of SSN
CREATE PROCEDURE get_user_info(
    IN input_username VARCHAR(50),
    OUT username_out VARCHAR(50),
    OUT email_out VARCHAR(100),
    OUT ssn_last4_out VARCHAR(4)
)
BEGIN
    DECLARE full_ssn VARCHAR(11);
    
    -- Get user data
    SELECT username, email, ssn
    INTO username_out, email_out, full_ssn
    FROM users
    WHERE username = input_username;
    
    -- Extract last 4 digits of SSN
    IF full_ssn IS NOT NULL THEN
        SET ssn_last4_out = RIGHT(full_ssn, 4);
    ELSE
        SET username_out = 'Unknown';
        SET email_out = 'Unknown';
        SET ssn_last4_out = '0000';
    END IF;
END;

-- Get all user information (for display)
CREATE VIEW user_info_view AS
SELECT 
    username,
    email,
    CONCAT('***-**-', RIGHT(ssn, 4)) as ssn_masked
FROM users;

-- Function to format SSN display
CREATE FUNCTION format_ssn_display(full_ssn VARCHAR(11))
RETURNS VARCHAR(15)
DETERMINISTIC
BEGIN
    RETURN CONCAT('***-**-', RIGHT(full_ssn, 4));
END;

-- Example usage:
-- 1. Get user info: CALL get_user_info('jane_smith', @username, @email, @ssn4);
-- 2. Display: SELECT @username, @email, CONCAT('***-**-', @ssn4) as ssn;
-- 3. View all: SELECT * FROM user_info_view;
