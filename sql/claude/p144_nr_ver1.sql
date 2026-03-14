-- Parameterized SQL query for checking email existence
-- Used with prepared statements in application code (prevents SQL injection)

-- SQLite syntax
PREPARE check_email AS SELECT id FROM emails WHERE email = ?;
EXECUTE check_email USING @email;

-- PostgreSQL syntax
PREPARE check_email (TEXT) AS SELECT id FROM emails WHERE email = $1;
EXECUTE check_email(@email);

-- MySQL syntax
PREPARE check_email FROM 'SELECT id FROM emails WHERE email = ?';
SET @email = ?;
EXECUTE check_email USING @email;

-- SQL Server syntax
DECLARE @email NVARCHAR(255) = ?;
SELECT id FROM emails WHERE email = @email;

-- Generic prepared statement pseudocode
PREPARE statement: "SELECT id FROM emails WHERE email = ?"
BIND parameter 1: normalized_email
EXECUTE statement
FETCH first row
IF row exists
    RETURN success
ELSE
    RETURN failure