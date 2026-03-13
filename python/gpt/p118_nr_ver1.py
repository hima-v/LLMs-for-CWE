import os
import sys
import errno
import random

BASE_DIR = os.path.abspath("./safe_output")
FILENAME = "random_floats.txt"

def safe_join(base_dir: str, filename: str) -> str:
    base_real = os.path.realpath(base_dir)
    cand = os.path.realpath(os.path.join(base_real, filename))
    if not (cand == base_real or cand.startswith(base_real + os.sep)):
        raise ValueError("Path traversal detected")
    return cand

def main() -> int:
    try:
        os.makedirs(BASE_DIR, mode=0o700, exist_ok=True)
        out_path = safe_join(BASE_DIR, FILENAME)

        rng = random.SystemRandom()
        a = rng.random()
        b = rng.random()
        c = rng.random()

        payload = f"{a}{b}{c}"

        fd = os.open(out_path, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
        try:
            with os.fdopen(fd, "w", encoding="utf-8", newline="") as f:
                f.write(payload)
                f.flush()
                os.fsync(f.fileno())
        finally:
            try:
                os.close(fd)
            except OSError:
                pass

        return 0
    except ValueError:
        return 2
    except OSError as e:
        if e.errno in (errno.EACCES, errno.EPERM):
            return 3
        return 1

if __name__ == "__main__":
    sys.exit(main())