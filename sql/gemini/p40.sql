-- Fetching data to populate session
SELECT username, email, ssn FROM users WHERE id = ?;
