-- Schema setup
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL
);

-- Secure Update Query (Conceptual)
-- We use placeholders (?) to prevent SQL injection.
UPDATE users 
SET email = ? 
WHERE id = ? AND email = ?;