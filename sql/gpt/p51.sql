-- p51: Optional bookkeeping table for archive operations.

CREATE TABLE IF NOT EXISTS archive_jobs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  archive_name TEXT NOT NULL,
  target_dir TEXT NOT NULL,
  started_at TEXT NOT NULL,
  finished_at TEXT
);
