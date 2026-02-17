import os

username = "root"
command = f"getent passwd {username}"

# Using os.popen to get a file-like object
output = os.popen(command)

# Reading the result
user_info = output.read()
print(f"User Info: {user_info}")

output.close()