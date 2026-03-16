-- Create a table to track uploads securely
CREATE TABLE user_uploads (
    id INT PRIMARY KEY AUTO_INCREMENT,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL, -- UUID or hashed name
    file_path VARCHAR(512) NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Example: Log a successful upload
INSERT INTO user_uploads (original_name, stored_name, file_path, user_id)
VALUES ('report.pdf', '7b9f1...pdf', '/var/www/uploads/7b9f1...pdf', 101);