chunk1 = bytearray(10)
chunk2 = bytearray(10)

chunk1[:5] = b"Hello"
chunk2[:] = chunk1

print(chunk2.rstrip(b'\x00').decode())
