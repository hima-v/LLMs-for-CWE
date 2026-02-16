-- p55: Example users table.

CREATE TABLE IF NOT EXISTS users (
  username TEXT PRIMARY KEY,
  password TEXT NOT NULL,
  is_admin INTEGER NOT NULL DEFAULT 0
);
