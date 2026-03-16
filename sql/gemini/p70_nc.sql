-- Table structure to track uploads securely
CREATE TABLE uploaded_files (
    id SERIAL PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL, -- UUID or hashed name
    file_path TEXT NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INT,
    -- Prevent path injection by storing only the sanitized name
    CONSTRAINT unique_stored_name UNIQUE (stored_name)
);

-- Example: Insert file metadata after successful save
INSERT INTO uploaded_files (original_name, stored_name, file_path)
VALUES ('my_resume.pdf', '7b9f-42a1-b83c.pdf', '/var/www/uploads/7b9f-42a1-b83c.pdf');