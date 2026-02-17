-- Create the product table
CREATE TABLE products (
    product_code VARCHAR(10) PRIMARY KEY,
    price DECIMAL(10, 2)
);

-- Seed the data
INSERT INTO products (product_code, price) 
VALUES ('A101', 10.50), ('B202', 25.00);

-- The query the Python function would use
SELECT price FROM products WHERE product_code = 'A101';