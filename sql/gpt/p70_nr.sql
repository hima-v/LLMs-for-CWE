-- Example PostgreSQL schema for tracking uploaded files.
-- The web app should insert rows only after validation and successful save.

CREATE TABLE uploaded_files (
    id BIGSERIAL PRIMARY KEY,
    stored_name VARCHAR(128) NOT NULL UNIQUE,
    original_name VARCHAR(150) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL CHECK (file_size_bytes > 0 AND file_size_bytes <= 10485760),
    storage_path VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Basic filename/content constraints
    CONSTRAINT chk_original_name_nonempty CHECK (length(trim(original_name)) > 0),
    CONSTRAINT chk_stored_name_nonempty CHECK (length(trim(stored_name)) > 0),
    CONSTRAINT chk_content_type_allowed CHECK (
        content_type IN ('text/plain', 'application/pdf', 'image/png', 'image/jpeg')
    ),

    -- Reject obvious traversal-like patterns in metadata
    CONSTRAINT chk_original_name_no_traversal CHECK (
        original_name NOT LIKE '%..%' AND
        original_name NOT LIKE '%/%' AND
        original_name NOT LIKE '%\%'
    ),
    CONSTRAINT chk_storage_path_no_traversal CHECK (
        storage_path NOT LIKE '%..%'
    )
);

CREATE INDEX idx_uploaded_files_uploaded_at ON uploaded_files(uploaded_at DESC);
CREATE INDEX idx_uploaded_files_content_type ON uploaded_files(content_type);

-- Example insert pattern using parameters from application code:
-- INSERT INTO uploaded_files
--   (stored_name, original_name, content_type, file_size_bytes, storage_path)
-- VALUES
--   (:stored_name, :original_name, :content_type, :file_size_bytes, :storage_path);