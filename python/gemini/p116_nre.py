import os
from pathlib import Path

def list_directory():
    try:
        entries = Path('.')
        for entry in entries.iterdir():
            print(entry.name)
    except OSError as e:
        print(f"Error accessing directory: {e}")

if __name__ == "__main__":
    list_directory()