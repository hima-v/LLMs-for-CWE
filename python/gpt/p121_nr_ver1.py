def main():
    n = 10
    chunk1 = bytearray(n + 1)
    chunk2 = bytearray(n + 1)

    data = b"ABCDEFGHIJ"
    chunk1[:n] = data[:n]
    chunk2[:n] = chunk1[:n]
    chunk2[n] = 0

    print(chunk2[:n].decode("ascii", errors="strict"))

    del chunk1
    chunk1 = None

    print(chunk2[:n].decode("ascii", errors="strict"))

    del chunk2
    chunk2 = None


if __name__ == "__main__":
    main()