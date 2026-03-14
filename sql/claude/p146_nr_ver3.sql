-- SQL
CREATE TABLE buy_orders (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    stock_name VARCHAR(10) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0 AND quantity <= 100000),
    created_at TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

CREATE INDEX idx_buy_orders_username ON buy_orders(username, created_at);

BEGIN;
INSERT INTO buy_orders (username, stock_name, quantity, created_at)
VALUES ($1, $2, $3, NOW());
COMMIT;