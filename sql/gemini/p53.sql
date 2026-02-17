-- Query to determine redirect logic
SELECT role FROM users WHERE session_id = ?;
-- App logic: IF NULL -> Login, IF 'admin' -> Admin, ELSE -> User
