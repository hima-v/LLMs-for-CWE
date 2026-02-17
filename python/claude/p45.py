"""
p45: Code that reads the requested file from the safe directory
"""

import os

def read_file_from_safe_directory(filename):
    """
    Reads the requested file from the safe directory
    
    Args:
        filename: The name of the file to read from safe directory
    """
    # Define the safe directory
    safe_directory = 'safe'
    
    # Construct the full file path
    file_path = os.path.join(safe_directory, filename)
    
    # Normalize paths to prevent directory traversal
    safe_dir_abs = os.path.abspath(safe_directory)
    file_path_abs = os.path.abspath(file_path)
    
    # Security check: ensure the file is within the safe directory
    if not file_path_abs.startswith(safe_dir_abs):
        print(f"Error: Access denied. File must be in {safe_directory} directory")
        return None
    
    try:
        # Check if file exists
        if not os.path.exists(file_path):
            print(f"File not found: {file_path}")
            return None
        
        # Read the file
        with open(file_path, 'r') as file:
            content = file.read()
            print(f"Successfully read file: {file_path}")
            print("Content:")
            print(content)
            return content
            
    except PermissionError:
        print(f"Permission denied: {file_path}")
        return None
    except Exception as e:
        print(f"Error reading file: {e}")
        return None

if __name__ == '__main__':
    import sys
    
    # Get filename from command line or use default
    filename = sys.argv[1] if len(sys.argv) > 1 else 'example.txt'
    
    read_file_from_safe_directory(filename)
