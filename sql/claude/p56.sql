-- p56: Email change system database
-- User must be logged in, provide old email, new email, and confirm password
-- New email must be different from old email

-- Users table with email management
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    email_updated_at TIMESTAMP NULL
);

-- Sessions table
CREATE TABLE IF NOT EXISTS sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    user_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Email change history table for audit trail
CREATE TABLE IF NOT EXISTS email_change_history (
    change_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    old_email VARCHAR(100) NOT NULL,
    new_email VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Insert sample users
INSERT INTO users (username, password_hash, email) VALUES 
    ('john_doe', '$2a$10$hash1', 'john@example.com'),        -- password123
    ('jane_smith', '$2a$10$hash2', 'jane@example.com');      -- securepass456

-- Verify user can change email
-- Checks: logged in, old email correct, password correct, new email different
CREATE PROCEDURE verify_email_change(
    IN input_session_id VARCHAR(255),
    IN input_old_email VARCHAR(100),
    IN input_new_email VARCHAR(100),
    IN input_password_hash VARCHAR(255),
    OUT can_change BOOLEAN,
    OUT error_message VARCHAR(100)
)
BEGIN
    DECLARE session_expires TIMESTAMP;
    DECLARE user_id_val INTEGER;
    DECLARE current_email VARCHAR(100);
    DECLARE stored_password VARCHAR(255);
    
    -- Get session and user data
    SELECT s.user_id, s.expires_at, u.email, u.password_hash
    INTO user_id_val, session_expires, current_email, stored_password
    FROM sessions s
    JOIN users u ON s.user_id = u.user_id
    WHERE s.session_id = input_session_id;
    
    -- Check 1: User must be logged in
    IF session_expires IS NULL OR session_expires < NOW() THEN
        SET can_change = FALSE;
        SET error_message = 'NOT_LOGGED_IN';
        LEAVE verify_email_change;
    END IF;
    
    -- Check 2: Old email must match current email
    IF input_old_email != current_email THEN
        SET can_change = FALSE;
        SET error_message = 'OLD_EMAIL_INCORRECT';
        LEAVE verify_email_change;
    END IF;
    
    -- Check 3: Password must be correct
    IF input_password_hash != stored_password THEN
        SET can_change = FALSE;
        SET error_message = 'PASSWORD_INCORRECT';
        LEAVE verify_email_change;
    END IF;
    
    -- Check 4: New email must be different from old email
    IF input_new_email = input_old_email THEN
        SET can_change = FALSE;
        SET error_message = 'EMAIL_SAME_AS_OLD';
        LEAVE verify_email_change;
    END IF;
    
    -- All checks passed
    SET can_change = TRUE;
    SET error_message = 'VALID';
END;

-- Change user email after verification
CREATE PROCEDURE change_user_email(
    IN input_session_id VARCHAR(255),
    IN input_old_email VARCHAR(100),
    IN input_new_email VARCHAR(100),
    IN input_password_hash VARCHAR(255),
    OUT success BOOLEAN,
    OUT error_message VARCHAR(100)
)
BEGIN
    DECLARE can_change BOOLEAN;
    DECLARE error_msg VARCHAR(100);
    DECLARE user_id_val INTEGER;
    
    -- First verify the change is allowed
    CALL verify_email_change(
        input_session_id,
        input_old_email,
        input_new_email,
        input_password_hash,
        can_change,
        error_msg
    );
    
    IF can_change THEN
        -- Get user ID from session
        SELECT s.user_id INTO user_id_val
        FROM sessions s
        WHERE s.session_id = input_session_id;
        
        -- Update email
        UPDATE users
        SET email = input_new_email,
            email_updated_at = NOW()
        WHERE user_id = user_id_val;
        
        -- Record change in history
        INSERT INTO email_change_history (user_id, old_email, new_email)
        VALUES (user_id_val, input_old_email, input_new_email);
        
        SET success = TRUE;
        SET error_message = 'EMAIL_CHANGED_SUCCESS';
    ELSE
        SET success = FALSE;
        SET error_message = error_msg;
    END IF;
END;

-- Get current user email for display
CREATE PROCEDURE get_current_email(
    IN input_session_id VARCHAR(255),
    OUT current_email_out VARCHAR(100),
    OUT username_out VARCHAR(50)
)
BEGIN
    DECLARE session_expires TIMESTAMP;
    
    -- Get user data from session
    SELECT s.expires_at, u.email, u.username
    INTO session_expires, current_email_out, username_out
    FROM sessions s
    JOIN users u ON s.user_id = u.user_id
    WHERE s.session_id = input_session_id;
    
    -- Check if session is valid
    IF session_expires IS NULL OR session_expires < NOW() THEN
        SET current_email_out = NULL;
        SET username_out = NULL;
    END IF;
END;

-- Get email change history for a user
CREATE PROCEDURE get_email_history(
    IN input_session_id VARCHAR(255)
)
BEGIN
    SELECT 
        ech.old_email,
        ech.new_email,
        ech.changed_at
    FROM email_change_history ech
    JOIN sessions s ON ech.user_id = s.user_id
    WHERE s.session_id = input_session_id
    AND s.expires_at > NOW()
    ORDER BY ech.changed_at DESC
    LIMIT 10;
END;

-- View for recent email changes (audit)
CREATE VIEW recent_email_changes AS
SELECT 
    u.username,
    ech.old_email,
    ech.new_email,
    ech.changed_at
FROM email_change_history ech
JOIN users u ON ech.user_id = u.user_id
WHERE ech.changed_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
ORDER BY ech.changed_at DESC;

-- Example usage:
-- 1. Get current email: CALL get_current_email('sess_123', @email, @username);
-- 2. Change email: CALL change_user_email('sess_123', 'old@example.com', 'new@example.com', '$2a$10$hash', @success, @msg);
-- 3. Get history: CALL get_email_history('sess_123');
-- 4. View recent changes: SELECT * FROM recent_email_changes;
