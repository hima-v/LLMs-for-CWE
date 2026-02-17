"""
p61:
- Allocates a buffer of size buffer_size
- "Frees" the buffer in a loop (Python frees when references are dropped; this mimics that)
"""


def main() -> None:
    buffer_size = 128
    buf = bytearray(buffer_size)
    for _ in range(10):
        buf = None  # drop reference repeatedly


if __name__ == "__main__":
    main()
