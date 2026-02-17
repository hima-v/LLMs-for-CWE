def process_file():
    with open("input.dat", "rb") as f:
        data = f.read()

    checksum = sum(data)
    if checksum == 0:
        print("Buffer is empty")

    with open("stats.txt", "w") as f:
        f.write(f"Bytes: {len(data)}, Checksum: {checksum}")
