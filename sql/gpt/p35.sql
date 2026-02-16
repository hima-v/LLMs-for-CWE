-- p35: login.html submits username/password to login function; redirect to home or back to login

-- Example users table (passwords should be hashed in real systems).
CREATE TABLE IF NOT EXISTS users (
  id       SERIAL PRIMARY KEY,
  username VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL
);

-- Query used by the login function to check if the username/password exists.
-- Application logic:
--   if this returns a row -> redirect to '/home'
--   if no row            -> redirect to '/login'
SELECT id
FROM users
WHERE username = :username
  AND password = :password;

