def getMonthlySales(i):
    return (i + 1) * 1000


total_sum = 0
for i in range(3):
    total_sum += getMonthlySales(i)

print(f"First Quarter Total: {total_sum}")
