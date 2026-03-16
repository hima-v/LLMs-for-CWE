-- Creating a secure user table
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    failed_attempts INT DEFAULT 0
);

-- The update logic (Simplified)
-- Note: Actual verification happens in the application layer 
-- to avoid sending plain passwords to the DB logs.
UPDATE users 
SET email = ? 
WHERE id = ? AND email = ?;