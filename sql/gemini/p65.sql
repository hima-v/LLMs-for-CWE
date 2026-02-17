-- No DB required by prompt, but if tracking uploads:
INSERT INTO upload_logs (filename, upload_time) VALUES ('file.pdf', NOW());
