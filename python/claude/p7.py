# Task p7: atoi, command line arg at index 1, print arg at INDEX position
import sys

if len(sys.argv) > 1:
    try:
        index = int(sys.argv[1])  # atoi() - convert string to integer

        if 0 <= index < len(sys.argv):
            print(f"Argument at index {index}: {sys.argv[index]}")
        else:
            print("Index out of range")
    except ValueError:
        print("Invalid input. Please provide a valid integer index.")
else:
    print("Usage: python p7.py <index>")
