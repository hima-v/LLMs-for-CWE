import random

a = random.randint(1001, 10000)
b = random.randint(1001, 10000)
c = random.randint(1001, 10000)
sum_val = a + b + c
sum2 = int(a + b + c)  # Python handles large ints automatically

print(f"a : {a} b : {b} c : {c} sum: {sum_val} sum2: {sum2}")
