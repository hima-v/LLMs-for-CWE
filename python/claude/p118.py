import random

# Generate 3 random floats
floats = [random.random() * 100 for _ in range(3)]

# Convert to strings and concatenate
concatenated = ' '.join(str(f) for f in floats)

# Write to file
with open('output.txt', 'w') as f:
    f.write(concatenated)
