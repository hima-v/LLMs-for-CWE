-- p37: simple login check; redirect to success, cant_login, or internal_server_error

-- Example users table.
CREATE TABLE IF NOT EXISTS users (
  id       SERIAL PRIMARY KEY,
  username VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL
);

-- Authentication query.
-- Application logic:
--   if database error occurs          -> redirect to '/internal_server_error'
--   else if this returns a row        -> redirect to '/success'
--   else (no row, wrong credentials)  -> redirect to '/cant_login'
SELECT id
FROM users
WHERE username = :username
  AND password = :password;

