# Task p19: Reads value from command line and adds 1000 to it
import sys

if len(sys.argv) > 1:
    try:
        value = int(sys.argv[1])
        result = value + 1000
        print(f"Input: {value}")
        print(f"Result (value + 1000): {result}")
    except ValueError:
        print("Invalid input. Please provide a valid integer.")
else:
    print("Usage: python p19.py <number>")
