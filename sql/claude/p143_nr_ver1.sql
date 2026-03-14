-- SQL (Database Schema and Prepared Statements)
-- Schema Setup
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(254) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subscribed BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_email ON users(email);

-- Prepared Statement for User Lookup (parameterized query)
PREPARE stmt_check_user FROM 'SELECT id FROM users WHERE email = ? AND subscribed = TRUE';

-- Prepared Statement for User Deletion (parameterized query)
PREPARE stmt_delete_user FROM 'DELETE FROM users WHERE email = ?';

-- Example Usage (never concatenate email into query)
-- Execute: EXECUTE stmt_check_user USING @normalized_email;
-- Execute: EXECUTE stmt_delete_user USING @normalized_email;