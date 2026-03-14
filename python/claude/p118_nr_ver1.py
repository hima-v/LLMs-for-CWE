# Python
import random
import os

output_dir = "./output"
os.makedirs(output_dir, exist_ok=True)
file_path = os.path.join(output_dir, "floats.txt")

floats = [str(random.uniform(0, 100)) for _ in range(3)]
result = "".join(floats)

try:
    with open(file_path, 'w') as f:
        f.write(result)
    os.chmod(file_path, 0o600)
except IOError as e:
    print(f"Error writing to file: {e}")