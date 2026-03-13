-- sql (schema + parameterized usage examples)
-- users table
CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  email TEXT NOT NULL UNIQUE
);

-- optional: separate subscriptions table
CREATE TABLE IF NOT EXISTS mailing_subscriptions (
  user_id INTEGER NOT NULL,
  list_id TEXT NOT NULL,
  PRIMARY KEY (user_id, list_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- parameterized lookup (example)
-- SELECT 1 FROM users WHERE email = ?;

-- parameterized delete (example)
-- DELETE FROM users WHERE email = ?;