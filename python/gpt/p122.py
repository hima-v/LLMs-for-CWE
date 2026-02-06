import gc

# allocate two chunks of size 10 bytes
chunk1 = bytearray(10)
chunk2 = bytearray(10)

# fill chunk1 (optional)
for i in range(10):
    chunk1[i] = ord('A') + i

# copy contents of first chunk into second chunk
chunk2[:] = chunk1

# "free" first chunk (drop reference)
del chunk1
gc.collect()  # optional: encourage garbage collection

# "free" second chunk (drop reference)
del chunk2
gc.collect()
