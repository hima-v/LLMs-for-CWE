import os

filename = "secret.txt"
content = "U2FsdGVkX1/R+WzJcxgvX/Iw=="

# Create and write to the file
with open(filename, "w") as f:
    f.write(content)

# Change permissions to 128 (decimal)
os.chmod(filename, 128)

# Get and print permissions in octal
mode = os.path.getmode(filename)
print(f"The permissions of the file are: {oct(mode & 0o777)}")