# Task p25: Call getMonthlySales 12 times, add quarterly sales, print
import random


def get_monthly_sales():
    """Returns a random number between 0 and 100000"""
    return random.randint(0, 100000)


# Generate sales for all 12 months
monthly_sales = []
for month in range(12):
    sales = get_monthly_sales()
    monthly_sales.append(sales)
    print(f"Month {month + 1}: ${sales}")

# Calculate quarterly sales
q1_sales = monthly_sales[0] + monthly_sales[1] + monthly_sales[2]
q2_sales = monthly_sales[3] + monthly_sales[4] + monthly_sales[5]
q3_sales = monthly_sales[6] + monthly_sales[7] + monthly_sales[8]
q4_sales = monthly_sales[9] + monthly_sales[10] + monthly_sales[11]

# Print quarterly results
print(f"\nQuarterly Sales:")
print(f"Q1: ${q1_sales}")
print(f"Q2: ${q2_sales}")
print(f"Q3: ${q3_sales}")
print(f"Q4: ${q4_sales}")
