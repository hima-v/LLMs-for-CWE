-- Parameterized query to prevent SQL injection
SELECT id FROM users WHERE email = ?;