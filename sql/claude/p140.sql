-- =====================================================
-- SQL - Database Username Check Queries
-- =====================================================

-- =====================================================
-- 1. BASIC QUERY - Simple SELECT (Works in all databases)
-- =====================================================

-- Returns a result if username exists
SELECT 1 FROM users WHERE username = 'john_doe' LIMIT 1;

-- Returns the username if it exists
SELECT username FROM users WHERE username = 'john_doe' LIMIT 1;

-- Returns all user data if username exists
SELECT * FROM users WHERE username = 'john_doe' LIMIT 1;


-- =====================================================
-- 2. EXISTS Clause (Recommended - More efficient)
-- =====================================================

-- Returns true (1) if username exists, false (0) if not
SELECT EXISTS(SELECT 1 FROM users WHERE username = 'john_doe') AS username_exists;

-- For application layer processing
SELECT CASE 
    WHEN EXISTS(SELECT 1 FROM users WHERE username = 'john_doe') 
    THEN 1 
    ELSE 0 
END AS user_exists;


-- =====================================================
-- 3. COUNT Method (Also common)
-- =====================================================

-- Returns count (0 if doesn't exist, 1+ if exists)
SELECT COUNT(*) AS count FROM users WHERE username = 'john_doe';

-- Better version with CASE for boolean output
SELECT CASE 
    WHEN COUNT(*) > 0 THEN 1 
    ELSE 0 
END AS username_exists
FROM users 
WHERE username = 'john_doe';


-- =====================================================
-- 4. MYSQL SPECIFIC
-- =====================================================

-- Using MySQL's IF function
SELECT IF(EXISTS(SELECT 1 FROM users WHERE username = 'john_doe'), 
    'TRUE', 'FALSE') AS user_exists;

-- Using MySQL UNION approach
SELECT 1 as exists_flag FROM users WHERE username = 'john_doe'
UNION ALL
SELECT 0 LIMIT 1;


-- =====================================================
-- 5. POSTGRESQL SPECIFIC
-- =====================================================

-- Using PostgreSQL's CASE
SELECT CASE 
    WHEN EXISTS(SELECT 1 FROM users WHERE username = 'john_doe') 
    THEN true 
    ELSE false 
END AS username_exists;

-- Using Postgres boolean type
SELECT EXISTS(SELECT 1 FROM users WHERE username = 'john_doe')::boolean;


-- =====================================================
-- 6. SQLITE SPECIFIC
-- =====================================================

-- SQLite doesn't have boolean type, uses 1/0 or NULL
SELECT CASE 
    WHEN EXISTS(SELECT 1 FROM users WHERE username = 'john_doe') 
    THEN 1 
    ELSE 0 
END AS username_exists;

-- Simple EXISTS check
SELECT EXISTS(SELECT 1 FROM users WHERE username = 'john_doe');


-- =====================================================
-- 7. SQL SERVER SPECIFIC
-- =====================================================

-- Using SQL Server's IF EXISTS
IF EXISTS(SELECT 1 FROM users WHERE username = 'john_doe')
    SELECT CAST(1 AS BIT) AS username_exists;
ELSE
    SELECT CAST(0 AS BIT) AS username_exists;

-- Using ISNULL
SELECT ISNULL((SELECT 1 FROM users WHERE username = 'john_doe'), 0) AS username_exists;


-- =====================================================
-- 8. ORACLE SPECIFIC
-- =====================================================

-- Oracle approach
SELECT CASE 
    WHEN EXISTS(SELECT 1 FROM users WHERE username = 'john_doe') 
    THEN 1 
    ELSE 0 
END AS username_exists
FROM dual;


-- =====================================================
-- 9. Case-Insensitive Search (Database Specific)
-- =====================================================

-- MySQL (uses COLLATE NOCASE by default for some collations)
SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(username) = LOWER('John_Doe'));

-- PostgreSQL (using ILIKE for case-insensitive)
SELECT EXISTS(SELECT 1 FROM users WHERE username ILIKE 'John_Doe');

-- SQLite
SELECT EXISTS(SELECT 1 FROM users WHERE LOWER(username) = LOWER('John_Doe'));

-- SQL Server
SELECT EXISTS(SELECT 1 FROM users WHERE username COLLATE SQL_Latin1_General_CP1_CI_AS = 'John_Doe');


-- =====================================================
-- 10. STORED PROCEDURES
-- =====================================================

-- MySQL Stored Procedure
DELIMITER //

CREATE PROCEDURE CheckUsernameExists(
    IN p_username VARCHAR(255),
    OUT p_exists INT
)
BEGIN
    SELECT CASE 
        WHEN EXISTS(SELECT 1 FROM users WHERE username = p_username) 
        THEN 1 
        ELSE 0 
    END INTO p_exists;
END //

DELIMITER ;

-- Call the procedure
CALL CheckUsernameExists('john_doe', @result);
SELECT @result;


-- PostgreSQL Stored Procedure (Function)
CREATE OR REPLACE FUNCTION check_username_exists(p_username VARCHAR)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS(SELECT 1 FROM users WHERE username = p_username);
END;
$$ LANGUAGE plpgsql;

-- Call the function
SELECT check_username_exists('john_doe');


-- SQL Server Stored Procedure
CREATE PROCEDURE CheckUsernameExists
    @Username VARCHAR(255)
AS
BEGIN
    IF EXISTS(SELECT 1 FROM users WHERE username = @Username)
        SELECT 1 AS username_exists;
    ELSE
        SELECT 0 AS username_exists;
END;

-- Call the procedure
EXEC CheckUsernameExists @Username = 'john_doe';


-- =====================================================
-- 11. PREPARED STATEMENTS (For Security)
-- =====================================================

-- Generic format for prepared statements
-- The ? or $1, :username syntax depends on the database driver

-- Example structure (database-agnostic):
-- PREPARE stmt FROM 'SELECT EXISTS(SELECT 1 FROM users WHERE username = ?) AS user_exists';
-- SET @username = 'john_doe';
-- EXECUTE stmt USING @username;


-- =====================================================
-- 12. CREATE TABLE EXAMPLE
-- =====================================================

-- MySQL
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
);

-- PostgreSQL
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_username ON users(username);

-- SQLite
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_username ON users(username);


-- =====================================================
-- 13. PERFORMANCE CONSIDERATIONS
-- =====================================================

-- ✓ RECOMMENDED: Most efficient (uses EXISTS with LIMIT)
SELECT EXISTS(SELECT 1 FROM users WHERE username = 'john_doe') AS user_exists;

-- ✓ GOOD: Uses index on username column
SELECT 1 FROM users WHERE username = 'john_doe' LIMIT 1;

-- ✓ ACCEPTABLE: Using COUNT
SELECT COUNT(*) > 0 AS user_exists FROM users WHERE username = 'john_doe';

-- ✗ NOT RECOMMENDED: Selects all columns
SELECT * FROM users WHERE username = 'john_doe';

-- ✗ NOT RECOMMENDED: No LIMIT or optimization
SELECT * FROM users;