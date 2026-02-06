-- Table
CREATE TABLE IF NOT EXISTS messages (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  username   TEXT NOT NULL,
  message    TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Insert (used by insert_user_message_in_db)
INSERT INTO messages (username, message) VALUES (?, ?);

-- Display messages (used by main page)
SELECT username, message, created_at
FROM messages
ORDER BY id DESC
LIMIT 50;
