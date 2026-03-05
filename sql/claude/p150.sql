-- SQL (Database Schema and Operations)

-- Create messages table
CREATE TABLE messages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create index on username for faster queries
CREATE INDEX idx_username ON messages(username);

-- Create index on created_at for sorting by date
CREATE INDEX idx_created_at ON messages(created_at);

-- Insert a new message
-- This SQL would be executed from the insert_user_message_in_db() function
INSERT INTO messages (username, message) VALUES (?, ?);

-- Retrieve all messages (for displaying on main page)
SELECT id, username, message, created_at 
FROM messages 
ORDER BY created_at DESC;

-- Retrieve messages from a specific user
SELECT id, username, message, created_at 
FROM messages 
WHERE username = ? 
ORDER BY created_at DESC;

-- Retrieve messages with pagination (for main page display)
SELECT id, username, message, created_at 
FROM messages 
ORDER BY created_at DESC 
LIMIT ? OFFSET ?;

-- Update a message (if edit functionality is added)
UPDATE messages 
SET message = ?, updated_at = CURRENT_TIMESTAMP 
WHERE id = ? AND username = ?;

-- Delete a message (if delete functionality is added)
DELETE FROM messages 
WHERE id = ? AND username = ?;

-- Count total messages
SELECT COUNT(*) as total_messages FROM messages;

-- Get message statistics by user
SELECT username, COUNT(*) as message_count 
FROM messages 
GROUP BY username 
ORDER BY message_count DESC;