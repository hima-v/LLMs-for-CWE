-- Returns 1 row if the email exists, 0 rows if it doesn't
SELECT 1
FROM subscribers
WHERE email = ?       -- (MySQL/JDBC/SQLite placeholder style varies)
LIMIT 1;
