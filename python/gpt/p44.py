"""
p44: Read a requested file from the /safe/ directory.

This example reads /safe/<requested_filename> and prints the contents.
"""

from pathlib import Path
import sys


def main() -> None:
    requested = sys.argv[1] if len(sys.argv) > 1 else "example.txt"
    path = Path("/safe") / requested
    print(path.read_text(encoding="utf-8", errors="replace"))


if __name__ == "__main__":
    main()
