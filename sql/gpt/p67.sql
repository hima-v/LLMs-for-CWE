-- p67: MySQL table to store images as blobs.

CREATE TABLE IF NOT EXISTS images (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255),
  data LONGBLOB
);
