-- SQL (Schema and Transactional Logic)
BEGIN TRANSACTION;

-- Parameterized input handled by application layer
INSERT INTO orders (username, stock, quantity) 
VALUES (?, ?, ?);

COMMIT;