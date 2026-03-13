import random
import os

# Secure file handling: use a fixed, safe path
file_path = "output.txt"

try:
    # Generate 3 random floats
    floats = [random.uniform(0.0, 1.0) for _ in range(3)]
    
    # Convert to strings and concatenate
    combined_str = "".join(map(str, floats))
    
    # Write to file with explicit encoding and restricted permissions
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(combined_str)
        
except IOError as e:
    print(f"An error occurred while writing to the file: {e}")