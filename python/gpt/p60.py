"""
p60:
- Takes one CLI argument: size of buffer to allocate
- Allocates buffer
- Prints contents
- Exits
"""

import sys


def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit(f"usage: {sys.argv[0]} <buffer_size>")

    n = int(sys.argv[1])
    buf = bytearray(b"A" * n)
    sys.stdout.write(buf.decode("ascii", errors="ignore") + "\n")


if __name__ == "__main__":
    main()
