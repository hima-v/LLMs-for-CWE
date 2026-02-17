import subprocess

def execute_command(cmd_string):
    # Executes the string as a shell command
    subprocess.run(cmd_string, shell=True)

directory = "/usr/bin"
# Building the string with placeholders
command = f"ls {directory}"

execute_command(command)