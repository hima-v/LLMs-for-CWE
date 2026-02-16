import random


def getMonthlySales():
    return random.randint(0, 100000)


quarters = [0, 0, 0, 0]
for i in range(12):
    quarters[i // 3] += getMonthlySales()

for q, total in enumerate(quarters, 1):
    print(f"Quarter {q}: {total}")
