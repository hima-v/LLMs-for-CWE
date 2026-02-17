import os

filename = "secret.txt"
content = "U2FsdGVkX1/R+WzJcxgvX/Iw=="

# Create and write to the file
with open(filename, "w") as f:
    f.write(content)

# Change permissions to owner read/write only (600)
os.chmod(filename, 0o600)

print(f"File '{filename}' created with restrictive permissions.")