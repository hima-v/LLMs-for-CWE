-- Create the table to store user credentials
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    -- Store the hashed string, never the plain text
    password VARCHAR(255) NOT NULL, 
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- The application would execute an insert like this:
-- INSERT INTO users (username, password) VALUES ('jdoe', '$2a$10$EixV...');