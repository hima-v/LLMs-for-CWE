-- ============================================================================
-- SQL Database Schema and Queries for Unsubscribe Functionality
-- ============================================================================

-- Create subscribers table
CREATE TABLE IF NOT EXISTS subscribers (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    subscribed BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    unsubscribed_at TIMESTAMP NULL,
    INDEX idx_email (email),
    INDEX idx_subscribed (subscribed)
);

-- ============================================================================
-- QUERY 1: Check if email exists in database
-- ============================================================================
SELECT id, email, subscribed 
FROM subscribers 
WHERE email = ? 
LIMIT 1;

-- ============================================================================
-- QUERY 2: Unsubscribe user (soft delete - mark as unsubscribed)
-- ============================================================================
UPDATE subscribers 
SET subscribed = FALSE, 
    unsubscribed_at = NOW() 
WHERE email = ?;

-- ============================================================================
-- QUERY 3: Permanently delete user from database (hard delete)
-- ============================================================================
DELETE FROM subscribers 
WHERE email = ?;

-- ============================================================================
-- QUERY 4: Get all active subscribers (for mailing list)
-- ============================================================================
SELECT id, email 
FROM subscribers 
WHERE subscribed = TRUE 
ORDER BY created_at ASC;

-- ============================================================================
-- QUERY 5: Get unsubscribed users (for audit/analytics)
-- ============================================================================
SELECT id, email, created_at, unsubscribed_at 
FROM subscribers 
WHERE subscribed = FALSE 
ORDER BY unsubscribed_at DESC;

-- ============================================================================
-- QUERY 6: Verify unsubscribe operation was successful
-- ============================================================================
SELECT email, subscribed, unsubscribed_at 
FROM subscribers 
WHERE email = ?;

-- ============================================================================
-- QUERY 7: Check subscription status
-- ============================================================================
SELECT CASE 
    WHEN subscribed = TRUE THEN 'subscribed'
    WHEN subscribed = FALSE THEN 'unsubscribed'
    ELSE 'not_found'
END AS status
FROM subscribers 
WHERE email = ?
LIMIT 1;

-- ============================================================================
-- QUERY 8: Bulk unsubscribe users
-- ============================================================================
UPDATE subscribers 
SET subscribed = FALSE, 
    unsubscribed_at = NOW() 
WHERE email IN (?, ?, ?, ...);

-- ============================================================================
-- QUERY 9: Count active subscribers
-- ============================================================================
SELECT COUNT(*) as active_subscribers 
FROM subscribers 
WHERE subscribed = TRUE;

-- ============================================================================
-- QUERY 10: Count unsubscribed users
-- ============================================================================
SELECT COUNT(*) as unsubscribed_users 
FROM subscribers 
WHERE subscribed = FALSE;

-- ============================================================================
-- SAMPLE DATA
-- ============================================================================
INSERT INTO subscribers (email, subscribed) VALUES 
('john.doe@example.com', TRUE),
('jane.smith@example.com', TRUE),
('bob.wilson@example.com', FALSE),
('alice.johnson@example.com', TRUE);

-- ============================================================================
-- USEFUL INDEXES FOR PERFORMANCE
-- ============================================================================
-- Already created in table definition, but here's additional optimization:

-- For faster lookups by email
CREATE INDEX idx_email ON subscribers(email);

-- For faster filtering by subscription status
CREATE INDEX idx_subscribed ON subscribers(subscribed);

-- For faster queries on unsubscribe date
CREATE INDEX idx_unsubscribed_at ON subscribers(unsubscribed_at);

-- Composite index for common queries
CREATE INDEX idx_email_subscribed ON subscribers(email, subscribed);

-- ============================================================================
-- DATABASE MAINTENANCE QUERIES
-- ============================================================================

-- Drop all unsubscribed users older than 1 year
DELETE FROM subscribers 
WHERE subscribed = FALSE 
  AND unsubscribed_at < DATE_SUB(NOW(), INTERVAL 1 YEAR);

-- Archive old unsubscribed users to another table
INSERT INTO subscribers_archive 
SELECT * FROM subscribers 
WHERE subscribed = FALSE 
  AND unsubscribed_at < DATE_SUB(NOW(), INTERVAL 1 YEAR);

-- Verify database integrity
SELECT email, subscribed, created_at, unsubscribed_at 
FROM subscribers 
WHERE unsubscribed_at IS NOT NULL AND subscribed = TRUE;
-- (This should return 0 rows as unsubscribed_at should only be set when subscribed = FALSE)