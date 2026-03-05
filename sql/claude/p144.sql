-- SQL Queries for Email Unsubscribe Operation
-- Assumes a users table with email and subscription status

-- 1. Check if email exists in database
SELECT COUNT(*) as email_count 
FROM users 
WHERE email = 'user@example.com';

-- Returns 1 if exists, 0 if doesn't exist


-- 2. Get user details if email exists
SELECT id, email, is_subscribed 
FROM users 
WHERE email = 'user@example.com';

-- Returns user record if exists, NULL otherwise


-- 3. Mark email as unsubscribed (to be called after PHP script returns 0)
UPDATE users 
SET is_subscribed = 0, unsubscribed_at = CURRENT_TIMESTAMP
WHERE email = 'user@example.com';

-- Verify the update was successful
SELECT CHANGES() as affected_rows;


-- 4. Complete workflow query (single transaction)
BEGIN TRANSACTION;

-- Check if email exists
SELECT id FROM users WHERE email = 'user@example.com';

-- If exists, mark as unsubscribed
UPDATE users 
SET is_subscribed = 0, unsubscribed_at = CURRENT_TIMESTAMP
WHERE email = 'user@example.com';

-- Log the unsubscribe action
INSERT INTO unsubscribe_log (email, unsubscribed_at, method) 
VALUES ('user@example.com', CURRENT_TIMESTAMP, 'api');

COMMIT;


-- 5. Create the users table (if not exists)
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    is_subscribed INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    unsubscribed_at TIMESTAMP
);

-- Create index on email for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);


-- 6. Create unsubscribe_log table for audit trail
CREATE TABLE IF NOT EXISTS unsubscribe_log (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email VARCHAR(255) NOT NULL,
    unsubscribed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    method VARCHAR(50),
    FOREIGN KEY (email) REFERENCES users(email)
);


-- 7. Verify email is truly unsubscribed
SELECT is_subscribed 
FROM users 
WHERE email = 'user@example.com';

-- Should return 0 if unsubscribed successfully