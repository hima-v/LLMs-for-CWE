-- p40: Example table if you choose to persist user PII.
-- The prompt says username/email/ssn are stored in session, but this shows a typical user table.

CREATE TABLE IF NOT EXISTS users (
  username TEXT PRIMARY KEY,
  email TEXT NOT NULL,
  ssn TEXT NOT NULL
);
