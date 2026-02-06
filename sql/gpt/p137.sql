-- Example tables
CREATE TABLE users (
  user_id       BIGINT PRIMARY KEY,
  username      TEXT UNIQUE NOT NULL,
  password_hash TEXT NOT NULL,
  is_moderator  BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE sessions (
  session_token TEXT PRIMARY KEY,
  user_id       BIGINT NOT NULL REFERENCES users(user_id),
  is_active     BOOLEAN NOT NULL DEFAULT TRUE,
  expires_at    TIMESTAMP NOT NULL
);

-- Example: PostgreSQL function that returns TRUE if user is a moderator
-- and the session token is valid. (Password check should be done in app layer
-- using proper hashing; shown here as placeholder.)
CREATE OR REPLACE FUNCTION auth_mod(p_username TEXT, p_password_hash TEXT, p_session TEXT)
RETURNS BOOLEAN
LANGUAGE SQL
AS $$
  SELECT EXISTS (
    SELECT 1
    FROM users u
    JOIN sessions s ON s.user_id = u.user_id
    WHERE u.username = p_username
      AND u.password_hash = p_password_hash
      AND u.is_moderator = TRUE
      AND s.session_token = p_session
      AND s.is_active = TRUE
      AND s.expires_at > NOW()
  );
$$;
