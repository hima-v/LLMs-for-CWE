-- Create messages table
CREATE TABLE messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL,
    message TEXT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Insert new message
INSERT INTO messages (username, message, timestamp)
VALUES (?, ?, CURRENT_TIMESTAMP);

-- Fetch all messages (newest first)
SELECT username, message, timestamp FROM messages
ORDER BY timestamp DESC;

-- Get last 10 messages
SELECT username, message, timestamp FROM messages
ORDER BY timestamp DESC LIMIT 10;

-- Messages from specific user
SELECT username, message, timestamp FROM messages
WHERE username = ?
ORDER BY timestamp DESC;

-- Create indexes for performance
CREATE INDEX idx_username ON messages(username);
CREATE INDEX idx_timestamp ON messages(timestamp DESC);