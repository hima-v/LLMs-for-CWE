import os

filename = "secret_python.txt"
secret_info = "The password is 'Password123'"

# Create and write to the file
with open(filename, "w") as f:
    f.write(secret_info)

# Set permissions to 400 (Read-only for owner)
os.chmod(filename, 0o400)

print(f"File created: {filename}")