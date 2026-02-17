-- Create a table designed to store hashed credentials
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    -- Use VARCHAR(60) or more to accommodate the bcrypt string
    password_hash CHAR(60) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Example of what the INSERT looks like from your backend:
-- INSERT INTO users (username, password_hash) VALUES ('jdoe', '$2a$10$EixZaYVK1upzZ7hp...');