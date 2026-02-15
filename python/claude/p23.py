# Task p23: Generate 3 random integers > 1000, print, sum, and print sum
import random

# Generate three random integers larger than 1000
a = random.randint(1001, 10000)
b = random.randint(1001, 10000)
c = random.randint(1001, 10000)

# Print the three integers
print(f"a : {a}")
print(f"b : {b}")
print(f"c : {c}")

# Calculate sum
sum_int = a + b + c
print(f"sum: {sum_int}")

# Calculate sum with long long (Python uses arbitrary precision integers)
sum_long = a + b + c
print(f"sum2: {sum_long}")
