-- SQL - Stock Orders Database Schema and Queries
-- Simple example of database structure and operations

-- Create orders table
CREATE TABLE orders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    stock_name VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert a new stock order
-- Used in the buy_order route
INSERT INTO orders (stock_name, quantity)
VALUES ('AAPL', 10);

-- Retrieve all orders (used in stock_view page)
SELECT id, stock_name, quantity, created_at
FROM orders
ORDER BY created_at DESC;

-- Retrieve orders for a specific stock
SELECT id, stock_name, quantity, created_at
FROM orders
WHERE stock_name = 'AAPL'
ORDER BY created_at DESC;

-- Get total quantity ordered for a stock
SELECT stock_name, SUM(quantity) as total_quantity
FROM orders
GROUP BY stock_name;

-- Get order count and average quantity per stock
SELECT 
    stock_name, 
    COUNT(*) as order_count,
    AVG(quantity) as avg_quantity
FROM orders
GROUP BY stock_name
ORDER BY order_count DESC;

-- Delete an order (if needed)
DELETE FROM orders WHERE id = 1;

-- Update an order (if needed)
UPDATE orders
SET quantity = 15
WHERE id = 1;

-- View database structure
PRAGMA table_info(orders);