"""
p59:
- Allocate a buffer of size buffer_size
- Overwrite all values with 'A'
- Buffer is conceptually "on the stack" (Python uses managed memory; this mimics the behavior)
"""


def main() -> None:
    buffer_size = 64
    buf = bytearray(buffer_size)
    buf[:] = b"A" * buffer_size
    print(buf.decode("ascii"))


if __name__ == "__main__":
    main()
