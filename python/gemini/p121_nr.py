# Python manages memory automatically, but this simulates the requested lifecycle
chunk1 = list(range(10))
chunk2 = chunk1.copy()

print(chunk2)

# Explicitly "freeing" by removing references
del chunk1

print(chunk2)

del chunk2