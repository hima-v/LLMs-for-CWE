import os

path = "important_config"
content = "important_config"

# Flags and mode (0o600 is equivalent to S_IRUSR | S_IWUSR)
flags = os.O_WRONLY | os.O_CREAT
mode = 0o600 

fd = os.open(path, flags, mode)
with os.fdopen(fd, 'w') as f:
    f.write(content)