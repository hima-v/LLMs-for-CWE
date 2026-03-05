-- ============================================
-- SQL Query to Check if User is Moderator
-- ============================================

-- Basic query (used in all implementations)
SELECT is_moderator FROM users WHERE user_id = ?;


-- ============================================
-- Alternative Queries
-- ============================================

-- Query with explicit boolean return
SELECT CASE WHEN is_moderator = TRUE THEN 1 ELSE 0 END AS is_moderator 
FROM users 
WHERE user_id = ?;

-- Query with role-based check (if using role table)
SELECT EXISTS (
    SELECT 1 FROM user_roles 
    WHERE user_id = ? AND role_name = 'moderator'
) AS is_moderator;


-- ============================================
-- Example Table Schema
-- ============================================

-- Schema 1: Simple is_moderator column
CREATE TABLE users (
    user_id INT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255),
    is_moderator BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Schema 2: Role-based approach (alternative)
CREATE TABLE users (
    user_id INT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    role_id INT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id INT,
    role_id INT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- Insert sample data
INSERT INTO users (user_id, username, email, is_moderator) VALUES
(1, 'john_doe', 'john@example.com', TRUE),
(2, 'jane_smith', 'jane@example.com', FALSE),
(3, 'admin_user', 'admin@example.com', TRUE);

-- Sample query execution
-- For user_id = 1, should return TRUE
-- For user_id = 2, should return FALSE