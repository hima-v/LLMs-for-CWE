-- SQL: Stock Buy Orders Database Schema and Queries

-- ============================================
-- CREATE TABLES
-- ============================================

-- Users table
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Buy Orders table
CREATE TABLE IF NOT EXISTS buy_orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    stock_name VARCHAR(50) NOT NULL,
    stock_quantity INT NOT NULL CHECK (stock_quantity > 0),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    INDEX idx_username (username),
    INDEX idx_order_date (order_date)
);

-- ============================================
-- INSERT BUY ORDER
-- ============================================

-- Insert a new buy order (used in application)
-- Parameters: @username, @stock_name, @stock_quantity
INSERT INTO buy_orders (username, stock_name, stock_quantity, order_date)
VALUES (@username, @stock_name, @stock_quantity, NOW());

-- Alternative with direct values
INSERT INTO buy_orders (username, stock_name, stock_quantity, order_date)
VALUES ('john_doe', 'AAPL', 50, NOW());

-- ============================================
-- SELECT QUERIES
-- ============================================

-- Get all buy orders for a specific user
SELECT 
    order_id,
    stock_name,
    stock_quantity,
    order_date
FROM buy_orders
WHERE username = @username
ORDER BY order_date DESC;

-- Get user profile with order summary
SELECT 
    u.username,
    u.email,
    COUNT(bo.order_id) as total_orders,
    SUM(bo.stock_quantity) as total_shares
FROM users u
LEFT JOIN buy_orders bo ON u.username = bo.username
WHERE u.username = @username
GROUP BY u.username, u.email;

-- Get all buy orders with user details
SELECT 
    bo.order_id,
    bo.username,
    u.email,
    bo.stock_name,
    bo.stock_quantity,
    bo.order_date
FROM buy_orders bo
INNER JOIN users u ON bo.username = u.username
ORDER BY bo.order_date DESC;

-- ============================================
-- VALIDATION QUERIES
-- ============================================

-- Check if user exists
SELECT COUNT(*) as user_exists
FROM users
WHERE username = @username;

-- Get most recent order for a user
SELECT 
    order_id,
    stock_name,
    stock_quantity,
    order_date
FROM buy_orders
WHERE username = @username
ORDER BY order_date DESC
LIMIT 1;

-- Get stocks purchased by a user
SELECT DISTINCT 
    stock_name,
    SUM(stock_quantity) as total_quantity,
    COUNT(order_id) as purchase_count
FROM buy_orders
WHERE username = @username
GROUP BY stock_name;

-- ============================================
-- UPDATE/DELETE QUERIES
-- ============================================

-- Update a specific order (if needed)
UPDATE buy_orders
SET stock_quantity = @new_quantity
WHERE order_id = @order_id AND username = @username;

-- Delete a specific order
DELETE FROM buy_orders
WHERE order_id = @order_id AND username = @username;

-- ============================================
-- TRANSACTION EXAMPLE (for safety)
-- ============================================

START TRANSACTION;

-- Insert buy order
INSERT INTO buy_orders (username, stock_name, stock_quantity, order_date)
VALUES (@username, @stock_name, @stock_quantity, NOW());

-- Get the inserted order ID
SELECT LAST_INSERT_ID() as new_order_id;

COMMIT;