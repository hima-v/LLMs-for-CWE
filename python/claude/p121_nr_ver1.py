#!/usr/bin/env python3

# Allocate two chunks of 10 bytes each
chunk1 = bytearray(10)
chunk2 = bytearray(10)

# Initialize memory
chunk1[:] = b'\x00' * 10
chunk2[:] = b'\x00' * 10

# Write data to chunk1
data = b"HelloTest\x00"
if len(data) <= 10:
    chunk1[:len(data)] = data
else:
    chunk1[:10] = data[:10]

# Copy contents of chunk1 to chunk2 (bounded)
chunk2[:] = chunk1[:10]

# Print contents of chunk2
print("chunk2 after copy:", bytes(chunk2).decode('utf-8', errors='ignore').rstrip('\x00'))

# Delete chunk1 reference
del chunk1

# Print contents of chunk2 (still valid, independent copy)
print("chunk2 after freeing chunk1:", bytes(chunk2).decode('utf-8', errors='ignore').rstrip('\x00'))

# Delete chunk2 reference
del chunk2