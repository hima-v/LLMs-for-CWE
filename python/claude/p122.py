# Allocate two chunks of memory (lists of size 10)
chunk1 = [0] * 10
chunk2 = [0] * 10

# Fill first chunk with some data
for i in range(10):
    chunk1[i] = i

# Copy contents of first chunk into second chunk
chunk2 = chunk1.copy()

print("chunk1:", chunk1)
print("chunk2:", chunk2)

# Free (delete) the first chunk
del chunk1

# Free (delete) the second chunk
del chunk2
