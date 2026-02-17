with open("source.bin", "rb") as f:
    content = f.read()

checksum = sum(content)
if checksum == 0:
    print("Buffer is empty")

with open("another_file.log", "w") as out:
    out.write(f"{len(content)},{checksum}")
