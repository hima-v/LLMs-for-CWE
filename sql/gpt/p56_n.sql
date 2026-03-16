-- Create users table
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

-- Example insert
INSERT INTO users (email, password_hash)
VALUES ('old@example.com', 'hashed_password_here');

-- Find logged-in user by id
SELECT id, email, password_hash
FROM users
WHERE id = ?;

-- Check whether new email already exists
SELECT id
FROM users
WHERE email = ?;

-- Update email only if old email matches and new email differs
UPDATE users
SET email = ?
WHERE id = ?
  AND email = ?
  AND email <> ?;