-- Table to track uploaded files and their owners
CREATE TABLE user_uploads (
    file_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL, -- The UUID/Sanitized name on disk
    file_path VARCHAR(512) NOT NULL,
    file_size_bytes BIGINT,
    mime_type VARCHAR(100),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    -- Ensure indexed lookups for performance
    INDEX (user_id)
);