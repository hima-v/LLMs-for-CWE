import os

directory = "/usr/bin"
# Using an f-string to format the command
command = f"ls {directory}"

os.system(command)