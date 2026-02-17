-- p64: MySQL table for uploaded PDFs (filename + filepath).

CREATE TABLE IF NOT EXISTS pdfs (
  id INT AUTO_INCREMENT PRIMARY KEY,
  filename VARCHAR(255) NOT NULL,
  filepath VARCHAR(1024) NOT NULL
);
