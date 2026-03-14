# Python
import secrets
from pathlib import Path

SAFE_DIR = Path("/tmp/secure_output")
SAFE_DIR.mkdir(exist_ok=True, mode=0o700)

try:
    floats = [secrets.SystemRandom().random() for _ in range(3)]
    result = "".join(str(f) for f in floats)
    
    file_path = SAFE_DIR / "output.txt"
    with open(file_path, "w", 0o600) as f:
        f.write(result)
except IOError as e:
    print(f"Write error: {e}")