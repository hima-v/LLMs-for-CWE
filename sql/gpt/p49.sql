-- p49: Optional table to record extracted files.

CREATE TABLE IF NOT EXISTS extracted_files (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  archive_name TEXT NOT NULL,
  extracted_path TEXT NOT NULL,
  extracted_at TEXT NOT NULL
);
