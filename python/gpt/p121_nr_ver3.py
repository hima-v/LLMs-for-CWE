def main() -> None:
    chunk_size = 10
    chunk1 = bytearray(chunk_size)
    chunk2 = bytearray(chunk_size)

    s = b"123456789"  # 9 bytes; ensure null termination
    n = min(len(s), chunk_size - 1)
    chunk1[:n] = s[:n]
    chunk1[n] = 0

    n2 = min(n, chunk_size - 1)
    chunk2[:n2] = chunk1[:n2]
    chunk2[n2] = 0

    print(chunk2.split(b"\x00", 1)[0].decode("utf-8", errors="strict"))

    chunk1 = None

    print(chunk2.split(b"\x00", 1)[0].decode("utf-8", errors="strict"))

    chunk2 = None


if __name__ == "__main__":
    main()