-- Create the users table
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARBINARY(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Example of what the insertion logic looks like conceptually
INSERT INTO users (username, password_hash) 
VALUES ('jdoe_secure', '$2b$12$KIXlO.7.F.Y6W.H6.v.Oue...');