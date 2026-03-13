import os
import random
import secrets

def write_random_floats(filename):
    BASE_DIR = "/tmp/app_data"
    safe_path = os.path.join(BASE_DIR, os.path.basename(filename))
    
    try:
        random_floats = [str(secrets.SystemRandom().random()) for _ in range(3)]
        data = "".join(random_floats)
        
        with open(safe_path, "w", encoding="utf-8") as f:
            f.write(data)
    except (IOError, OSError) as e:
        print(f"Error writing to file: {e}")

write_random_floats("output.txt")