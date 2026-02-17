import os

important_config = "important_config"
# Open with specific flags and mode 0o700 (octal)
fd = os.open("config.txt", os.O_RDWR | os.O_CREAT, 0o700)

with os.fdopen(fd, 'w') as f:
    f.write(important_config)