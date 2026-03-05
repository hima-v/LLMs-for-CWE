import os

# Using format string to construct the ls command
directory = "/home/user"

# Using string format method to create the command string
# (similar to print function with placeholders)
command = "ls -l {}".format(directory)

# Execute the command using os.system function
os.system(command)

# Alternative using f-string syntax
print("Using f-string:")
command_f = f"ls -l {directory}"
os.system(command_f)
