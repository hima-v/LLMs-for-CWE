-- Create the users table
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    -- Store the combined salt and hash string
    password_data TEXT NOT NULL 
);

-- Example of what the application-level INSERT looks like
INSERT INTO users (username, password_data) 
VALUES ('jdoe123', '7a3b...:f2d1...');