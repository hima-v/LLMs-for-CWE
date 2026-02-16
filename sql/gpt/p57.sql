-- p57: Example users table for email change.

CREATE TABLE IF NOT EXISTS users (
  username TEXT PRIMARY KEY,
  email TEXT NOT NULL,
  password TEXT NOT NULL
);
