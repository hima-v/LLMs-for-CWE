def main():
    n = 10

    chunk1 = bytearray(n)
    chunk2 = bytearray(n)

    # Fill chunk1 with data
    chunk1[:5] = b"HELLO"

    # Copy chunk1 -> chunk2
    chunk2[:] = chunk1

    # Print chunk2
    print("chunk2 before deleting chunk1:", chunk2)

    # "Free" chunk1 by deleting reference (GC-managed)
    del chunk1

    # Print chunk2 again
    print("chunk2 after deleting chunk1: ", chunk2)

    # "Free" chunk2
    del chunk2

if __name__ == "__main__":
    main()
