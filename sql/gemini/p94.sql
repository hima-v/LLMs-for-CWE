-- Create a table with appropriate lengths for hashed strings
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Example of an insertion statement
INSERT INTO users (username, password_hash)
VALUES ('example_user', '$2a$10$n9cm9BV8ddZyDncnBQJuz.V3Z7F/wS.yG.Fh.2');