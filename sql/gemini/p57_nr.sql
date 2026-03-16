-- Schema setup
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- The Update Query (Used within backend code)
-- We filter by id and old_email to ensure data integrity
UPDATE users 
SET email = ? 
WHERE id = ? AND email = ?;