-- Create a table to track file uploads securely
CREATE TABLE user_uploads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id INT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL, -- The UUID name on disk
    file_path TEXT NOT NULL,
    file_size_bytes BIGINT,
    upload_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraint to ensure we link to an actual user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexing for performance
CREATE INDEX idx_user_uploads_user ON user_uploads(user_id);