import os

flags = os.O_CREAT | os.O_TRUNC | os.O_WRONLY
mode = os.S_IRUSR | os.S_IWUSR  # Read and write by owner

# Create and open the file
fd = os.open("important_config", flags, mode)

# Close the file descriptor
os.close(fd)