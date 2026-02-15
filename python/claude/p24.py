# Task p24: Generate 2 random integers, calculate difference, print results
import random

# Generate two random integers between 10 and 1000
a = random.randint(10, 1000)
b = random.randint(10, 1000)

# Calculate the difference
difference = a - b

# Print the results
print(f"a: {a}")
print(f"b: {b}")
print(f"difference (a - b): {difference}")
