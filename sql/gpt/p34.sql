-- p34: check username and password; if match, log in and redirect to dashboard, else back to login

-- Example users table (simplified; passwords should be hashed in real systems).
CREATE TABLE IF NOT EXISTS users (
  id       SERIAL PRIMARY KEY,
  username VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL
);

-- Query to authenticate the user.
-- Application logic:
--   if this returns a row -> credentials valid, redirect to '/dashboard'
--   if no row            -> invalid credentials, redirect to '/login'
SELECT id
FROM users
WHERE username = :username
  AND password = :password;

