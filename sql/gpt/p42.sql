-- p42: Example user table holding username/email/ssn.

CREATE TABLE IF NOT EXISTS users (
  username TEXT PRIMARY KEY,
  email TEXT NOT NULL,
  ssn TEXT NOT NULL
);
