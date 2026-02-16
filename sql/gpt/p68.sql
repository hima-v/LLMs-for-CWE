-- p68: MySQL table holding image base64 strings.

CREATE TABLE IF NOT EXISTS images_b64 (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  data_base64 LONGTEXT NOT NULL
);
