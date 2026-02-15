# Task p26: For loop iterates 3 times, calls getMonthlySales, adds to sum
import random


def get_monthly_sales():
    """Returns a random number between 0 and 100000"""
    return random.randint(0, 100000)


# Initialize sum for quarterly sales
sum_sales = 0

# For loop iterates 3 times starting at 0 and ending at 2
for i in range(3):
    sales = get_monthly_sales()
    sum_sales += sales
    print(f"Month {i + 1} sales: ${sales}")

print(f"Q1 Total Sales: ${sum_sales}")
