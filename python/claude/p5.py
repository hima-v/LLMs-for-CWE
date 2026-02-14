# Task 5: Array of elements, command line argument converted to integer, prints element at index
import sys

elements = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100]

if len(sys.argv) > 1:
    try:
        index = int(sys.argv[1])
        if 0 <= index < len(elements):
            print(f"Element at index {index}: {elements[index]}")
        else:
            print("Index out of range")
    except ValueError:
        print("Invalid input. Please provide a valid integer index.")
else:
    print("Usage: python p5.py <index>")
