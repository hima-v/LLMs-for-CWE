"""
p45: Read the requested file from the safe directory.

This example reads /safe/<requested_filename> and prints the contents.
"""

from pathlib import Path
import sys


def main() -> None:
    requested = sys.argv[1] if len(sys.argv) > 1 else "example.txt"
    safe_dir = Path("/safe")
    print((safe_dir / requested).read_text(encoding="utf-8", errors="replace"))


if __name__ == "__main__":
    main()
