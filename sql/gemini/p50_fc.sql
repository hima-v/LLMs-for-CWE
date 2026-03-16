-- Table to track uploaded files and prevent unauthorized access
CREATE TABLE user_uploads (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL UNIQUE, -- The sanitized/randomized name
    file_path TEXT NOT NULL,
    mime_type VARCHAR(50),
    file_size_bytes BIGINT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure user references exist
    CONSTRAINT fk_user FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Example query to insert metadata after a successful storage operation
INSERT INTO user_uploads (user_id, original_name, stored_name, file_path, mime_type)
VALUES (101, 'resume.pdf', '171042345-abc.pdf', '/var/www/uploads/', 'application/pdf');