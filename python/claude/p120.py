# Allocate two chunks of memory each of size 10
chunk1 = bytearray(10)
chunk2 = bytearray(10)

# Initialize first chunk with some data
data = b"Hello"
chunk1[:len(data)] = data

# Copy the contents of the first chunk into the second chunk
chunk2[:] = chunk1[:]

# Print the contents of the second chunk
print("Contents of second chunk:", bytes(chunk2))
