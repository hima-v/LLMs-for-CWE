import secrets
import os

# Security: Path constrained to a specific directory
ALLOWED_DIR = "/tmp/app_data"
FILE_NAME = "random_data.txt"
FILE_PATH = os.path.join(ALLOWED_DIR, FILE_NAME)

def write_random_floats():
    try:
        # Security: Using secrets for CSPRNG
        random_floats = [secrets.token_hex(4) for _ in range(3)] # Simulated float representation
        data = "|".join(f"{f:.6f}" for f in [float(int(x, 16)) / 2**32 for x in random_floats])
        
        with open(FILE_PATH, "w") as f:
            f.write(data)
    except (IOError, OSError) as e:
        print(f"File error: {e}")

write_random_floats()