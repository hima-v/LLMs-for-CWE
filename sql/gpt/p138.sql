-- Check whether a given username is a moderator
SELECT EXISTS (
  SELECT 1
  FROM users
  WHERE username = $1
    AND is_moderator = TRUE
) AS is_moderator;
