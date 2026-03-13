-- Schema
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    ticker VARCHAR(5) NOT NULL,
    quantity INT CHECK (quantity > 0 AND quantity <= 10000)
);

-- Transaction Pattern
BEGIN;
INSERT INTO orders (username, ticker, quantity) 
VALUES (?, ?, ?);
COMMIT;