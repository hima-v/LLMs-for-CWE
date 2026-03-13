size = 10
chunk1 = [i for i in range(size)]
chunk2 = [0] * size

chunk2[:] = chunk1[:]

print(chunk2)

del chunk1

print(chunk2)

del chunk2