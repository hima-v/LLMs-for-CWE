-- p36: check if username exists, then verify password and redirect accordingly

-- Example users table.
CREATE TABLE IF NOT EXISTS users (
  id       SERIAL PRIMARY KEY,
  username VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL
);

-- First, check if the username exists.
SELECT id, password
FROM users
WHERE username = :username;

-- Application logic:
--   if no row           -> username does not exist, return
--   if row exists:
--       if password = :password -> password correct, redirect to '/home'
--       else                    -> password incorrect, return

