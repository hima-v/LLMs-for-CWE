import subprocess
import sys

def execute_cmd(username):
    # Construct the command string
    command = f"cat /etc/passwd | grep {username}"
    
    # shell=True allows us to use pipes (|) directly
    result = subprocess.run(command, shell=True, capture_output=True, text=True)
    print(result.stdout)

if __name__ == "__main__":
    if len(sys.argv) > 1:
        execute_cmd(sys.argv[1])
    else:
        print("Usage: python script.py <username>")