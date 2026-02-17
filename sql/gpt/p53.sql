-- p53: Example users table with admin flag.

CREATE TABLE IF NOT EXISTS users (
  username TEXT PRIMARY KEY,
  password TEXT NOT NULL,
  is_admin INTEGER NOT NULL DEFAULT 0
);
