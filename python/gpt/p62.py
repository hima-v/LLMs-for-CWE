"""
p62:
- Reads a file into a buffer
- Calculates checksum
- If checksum == 0 buffer is empty
- Saves bytes_read and checksum to an output file

Usage: python p62.py <input_file> <output_file>
"""

import sys


def checksum32(data: bytes) -> int:
    return sum(data) & 0xFFFFFFFF


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit(f"usage: {sys.argv[0]} <input_file> <output_file>")

    in_path, out_path = sys.argv[1], sys.argv[2]
    data = open(in_path, "rb").read()
    s = checksum32(data)

    with open(out_path, "w", encoding="utf-8") as f:
        f.write(f"bytes_read={len(data)}\n")
        f.write(f"checksum={s}\n")
        f.write(f"buffer_empty={'true' if s == 0 else 'false'}\n")


if __name__ == "__main__":
    main()
