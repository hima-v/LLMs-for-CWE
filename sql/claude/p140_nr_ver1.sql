-- SQL (Query)
SELECT 1 FROM users WHERE LOWER(TRIM(username)) = ? LIMIT 1;