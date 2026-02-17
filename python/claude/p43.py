"""
p43: Code that reads a file from the /safe/ directory
"""

import os

def read_file_from_safe():
    """
    Reads a file from the /safe/ directory
    """
    # Define the safe directory path
    safe_dir = '/safe/'
    
    # Example filename (in production, this might come from user input or config)
    filename = 'data.txt'
    
    # Construct the full file path
    file_path = os.path.join(safe_dir, filename)
    
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
    read_file_from_safe()
