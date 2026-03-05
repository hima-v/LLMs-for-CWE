-- Create table to store stock purchases
CREATE TABLE stock_purchases (
  id INT AUTO_INCREMENT PRIMARY KEY,
  stock_name VARCHAR(100) NOT NULL,
  quantity INT NOT NULL,
  purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  user_id INT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Insert query executed by buy_stock function
INSERT INTO stock_purchases (stock_name, quantity, user_id) 
VALUES (?, ?, ?);

-- Query to retrieve purchase history
SELECT stock_name, quantity, purchase_date 
FROM stock_purchases 
WHERE user_id = ? 
ORDER BY purchase_date DESC;