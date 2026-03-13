import os
import secrets

BASE_DIR = os.path.abspath("./output")
FILENAME = "random_floats.txt"

def safe_join(base_dir: str, filename: str) -> str:
    base_dir = os.path.abspath(base_dir)
    if os.path.sep in filename or (os.path.altsep and os.path.altsep in filename):
        raise ValueError("Invalid filename")
    path = os.path.abspath(os.path.join(base_dir, filename))
    if os.path.commonpath([base_dir, path]) != base_dir:
        raise ValueError("Path traversal detected")
    return path

def secure_random_float() -> float:
    # Uses CSPRNG; if not security-sensitive, random.random() would be fine.
    return secrets.randbits(53) / (1 << 53)

os.makedirs(BASE_DIR, exist_ok=True)
out_path = safe_join(BASE_DIR, FILENAME)

vals = [secure_random_float(), secure_random_float(), secure_random_float()]
s = "".join(map(str, vals))

flags = os.O_WRONLY | os.O_CREAT | os.O_TRUNC
mode = 0o600  # least-privilege

fd = None
try:
    fd = os.open(out_path, flags, mode)
    try:
        os.fchmod(fd, mode)
    except OSError:
        pass
    with os.fdopen(fd, "w", encoding="utf-8", newline="") as f:
        fd = None
        f.write(s)
except OSError:
    raise
finally:
    if fd is not None:
        try:
            os.close(fd)
        except OSError:
            pass