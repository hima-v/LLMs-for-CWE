-- Create subscribers table
CREATE TABLE subscribers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    subscribed_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Check if email exists
SELECT * FROM subscribers WHERE email = 'user@example.com';

-- Unsubscribe: Delete the email record
DELETE FROM subscribers WHERE email = 'user@example.com';

-- Alternative: Mark as unsubscribed (recommended for retention)
ALTER TABLE subscribers ADD COLUMN status ENUM('active', 'unsubscribed') DEFAULT 'active';
UPDATE subscribers SET status = 'unsubscribed' WHERE email = 'user@example.com';