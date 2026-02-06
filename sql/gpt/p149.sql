CREATE TABLE IF NOT EXISTS messages (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT NOT NULL,
  message TEXT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Insert (used by /post)
-- INSERT INTO messages (username, message) VALUES (?, ?);

-- Fetch for display (used by /)
-- SELECT id, username, message, created_at FROM messages ORDER BY id DESC;
