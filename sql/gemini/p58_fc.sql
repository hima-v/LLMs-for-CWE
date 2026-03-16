-- Schema for a secure user table
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- The logic for the update (to be called via parameterized query)
UPDATE users 
SET email = ? 
WHERE user_id = ? AND email = ?;