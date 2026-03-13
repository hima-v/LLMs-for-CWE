-- Transactional structure used by application logic
BEGIN TRANSACTION;
INSERT INTO orders (username, stock_name, quantity) 
VALUES (?, ?, ?);
COMMIT;

-- Table definition
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    stock_name VARCHAR(5) NOT NULL,
    quantity INTEGER CHECK (quantity > 0 AND quantity <= 100000)
);