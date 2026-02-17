import os

filename = "important_config"
data = b"important_config"

# Define flags and mode
flags = os.O_RDWR | os.O_CREAT
mode = os.S_IRUSR | os.S_IWUSR | os.S_IRGRP | os.S_IROTH

fd = os.open(filename, flags, mode)
os.write(fd, data)
os.close(fd)