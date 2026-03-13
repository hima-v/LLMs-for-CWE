import os
import subprocess

def list_directory():
    # Using a hardcoded list for the command to prevent shell injection.
    # subprocess.run is preferred over os.system for better error handling.
    try:
        subprocess.run(["ls"], check=True)
    except subprocess.CalledProcessError as e:
        print(f"Command failed with error: {e}")
    except FileNotFoundError:
        print("The 'ls' command was not found on this system.")

if __name__ == "__main__":
    list_directory()