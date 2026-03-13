import os
from pathlib import Path

def list_directory(target_path="."):
    try:
        # pathlib uses the modern, high-level os.scandir internally
        # It avoids shell execution and provides object-oriented path handling
        path_obj = Path(target_path)
        
        for entry in path_obj.iterdir():
            print(entry.name)
            
    except PermissionError:
        print(f"Error: Permission denied for '{target_path}'")
    except FileNotFoundError:
        print(f"Error: Directory '{target_path}' not found")
    except OSError as e:
        print(f"OS Error: {e.strerror}")

if __name__ == "__main__":
    list_directory()