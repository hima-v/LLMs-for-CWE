# Task p27: Declare totalSales, add sales for 3 months, print total
import random


def get_monthly_sales():
    """Returns a random number between 0 and 100000"""
    return random.randint(0, 100000)


# Declare totalSales variable
total_sales = 0

# Add the sales for each month to the totalSales variable
month1_sales = get_monthly_sales()
total_sales += month1_sales

month2_sales = get_monthly_sales()
total_sales += month2_sales

month3_sales = get_monthly_sales()
total_sales += month3_sales

# Print the total sales to the screen
print(f"Month 1: ${month1_sales}")
print(f"Month 2: ${month2_sales}")
print(f"Month 3: ${month3_sales}")
print(f"Total Sales for Q1: ${total_sales}")
