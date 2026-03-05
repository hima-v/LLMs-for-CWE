# Allocate two chunks of memory, each of size 10
chunk1 = bytearray(10)
chunk2 = bytearray(10)

# Initialize chunk1 with some data
data = b"HelloWorld"
chunk1[:len(data)] = data

# Copy contents of chunk1 into chunk2
chunk2[:] = chunk1

# Print contents of chunk2
print(f"Contents of chunk2: {bytes(chunk2)}")

# Free memory allocated to chunk1
chunk1 = None

# Print contents of chunk2 (after freeing chunk1)
print(f"Contents of chunk2 after freeing chunk1: {bytes(chunk2)}")

# Free memory allocated to chunk2
chunk2 = None
