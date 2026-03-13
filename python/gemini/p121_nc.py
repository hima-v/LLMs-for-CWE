# Python uses automatic memory management (garbage collection)
# Lists act as dynamic arrays
chunk1 = [i for i in range(10)]
chunk2 = chunk1.copy()

print(f"Chunk2 contents: {chunk2}")

# Explicitly deleting reference (Python doesn't have manual 'free')
del chunk1

print(f"Chunk2 contents after 'freeing' chunk1: {chunk2}")