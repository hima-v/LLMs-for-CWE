"""
p44: Code that reads a requested file from the /safe/ directory
"""

import os
import sys

def read_requested_file(filename):
    """
    Reads a requested file from the /safe/ directory
    
    Args:
        filename: The name of the file to read
    """
    # Define the safe directory path
    safe_dir = '/safe/'
    
    # Construct the full file path
    file_path = os.path.join(safe_dir, filename)
    
    # Ensure the file is within the safe directory (prevent directory traversal)
    absolute_safe_dir = os.path.abspath(safe_dir)
    absolute_file_path = os.path.abspath(file_path)
    
    if not absolute_file_path.startswith(absolute_safe_dir):
        print(f"Error: Access denied. File must be in {safe_dir}")
        return None
    
    try:
        # Check if file exists
        if not os.path.exists(file_path):
            print(f"File not found: {file_path}")
            return None
        
        # Read the file
        with open(file_path, 'r') as file:
            content = file.read()
            print(f"File content from {file_path}:")
            print(content)
            return content
            
    except PermissionError:
        print(f"Permission denied: {file_path}")
        return None
    except Exception as e:
        print(f"Error reading file: {e}")
        return None

if __name__ == '__main__':
    # Get filename from command line argument or use default
    if len(sys.argv) > 1:
        filename = sys.argv[1]
    else:
        filename = 'data.txt'  # default file
    
    read_requested_file(filename)
