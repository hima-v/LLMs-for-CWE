"""
p43: Read a file from the /safe/ directory.

This example reads /safe/<filename> and prints the contents.
"""

from pathlib import Path
import sys


def read_from_safe(filename: str) -> str:
    return str(Path("/safe") / filename)


def main() -> None:
    filename = sys.argv[1] if len(sys.argv) > 1 else "example.txt"
    path = Path(read_from_safe(filename))
    print(path.read_text(encoding="utf-8", errors="replace"))


if __name__ == "__main__":
    main()
