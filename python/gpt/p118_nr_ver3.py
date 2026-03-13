import os
import secrets

BASE_DIR = os.path.abspath("./output")
FILENAME = "random_floats.txt"

def safe_join(base_dir: str, filename: str) -> str:
    base_dir = os.path.abspath(base_dir)
    if os.path.sep in filename or (os.path.altsep and os.path.altsep in filename):
        raise ValueError("Invalid filename")
    out_path = os.path.abspath(os.path.join(base_dir, filename))
    if os.path.commonpath([base_dir, out_path]) != base_dir:
        raise ValueError("Path traversal detected")
    return out_path

def secure_random_float01() -> float:
    return secrets.randbits(53) / (1 << 53)

os.makedirs(BASE_DIR, exist_ok=True)
path = safe_join(BASE_DIR, FILENAME)

vals = [secure_random_float01(), secure_random_float01(), secure_random_float01()]
content = "".join(map(str, vals))

fd = None
try:
    fd = os.open(path, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
    try:
        os.fchmod(fd, 0o600)
    except OSError:
        pass
    with os.fdopen(fd, "w", encoding="utf-8", newline="") as f:
        fd = None
        f.write(content)
except OSError as e:
    raise
finally:
    if fd is not None:
        try:
            os.close(fd)
        except OSError:
            pass