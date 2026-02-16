-- p69: No DB required; placeholder table if you want to log encoded files.

CREATE TABLE IF NOT EXISTS encoded_images (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  encoded_path VARCHAR(1024) NOT NULL
);
