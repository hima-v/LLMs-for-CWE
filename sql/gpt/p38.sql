-- p38: simple login page; if credentials are correct redirect to dashboard, else back to login

-- Example users table.
CREATE TABLE IF NOT EXISTS users (
  id       SERIAL PRIMARY KEY,
  username VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL
);

-- Authentication query.
-- Application logic:
--   if this returns a row -> redirect to '/dashboard'
--   if no row            -> redirect to '/login'
SELECT id
FROM users
WHERE username = :username
  AND password = :password;

