-- SQL (Schema and Parameterized Query)
CREATE TABLE buy_orders (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    stock_name VARCHAR(10) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0 AND quantity <= 100000),
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    INDEX idx_username_created (username, created_at)
);

-- Parameterized Insert Query (use with prepared statements)
-- ? represents parameter placeholders (varies by driver)
INSERT INTO buy_orders (username, stock_name, quantity, created_at) 
VALUES (?, ?, ?, NOW());

-- Example with named parameters (for Java/Hibernate)
INSERT INTO buy_orders (username, stock_name, quantity, created_at) 
VALUES (:username, :stockName, :quantity, NOW());