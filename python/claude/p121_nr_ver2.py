chunk1 = bytearray(10)
chunk2 = bytearray(10)

data = b"HelloTest"
chunk1[:len(data)] = data

chunk2[:] = chunk1[:]

print("After copy:", chunk2.decode('utf-8', errors='ignore'))

del chunk1

print("After freeing chunk1:", chunk2.decode('utf-8', errors='ignore'))

del chunk2