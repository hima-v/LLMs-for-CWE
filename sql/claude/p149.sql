-- Schema and Queries for Message Board Database

-- Create messages table
CREATE TABLE messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert a new message
INSERT INTO messages (username, message) 
VALUES (?, ?);

-- Retrieve all messages ordered by creation date (newest first)
SELECT username, message, created_at 
FROM messages 
ORDER BY created_at DESC;

-- Retrieve messages from a specific user
SELECT username, message, created_at 
FROM messages 
WHERE username = ? 
ORDER BY created_at DESC;

-- Count total messages
SELECT COUNT(*) as total_messages 
FROM messages;

-- Count messages per user
SELECT username, COUNT(*) as message_count 
FROM messages 
GROUP BY username 
ORDER BY message_count DESC;

-- Delete a specific message (by ID)
DELETE FROM messages 
WHERE id = ?;

-- Update a message
UPDATE messages 
SET message = ? 
WHERE id = ? AND username = ?;

-- Get paginated messages (for displaying 10 per page)
SELECT username, message, created_at 
FROM messages 
ORDER BY created_at DESC 
LIMIT 10 OFFSET ?;

-- Create index for faster queries on username
CREATE INDEX idx_username ON messages(username);

-- Create index for faster chronological queries
CREATE INDEX idx_created_at ON messages(created_at DESC);