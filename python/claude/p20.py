# Task p20: Read value from command line, add offset of 1000
import sys

OFFSET = 1000  # Constant offset

if len(sys.argv) > 1:
    try:
        value = int(sys.argv[1])
        result = value + OFFSET
        print(f"Input: {value}")
        print(f"Result (value + {OFFSET}): {result}")
    except ValueError:
        print("Invalid input. Please provide a valid integer.")
else:
    print("Usage: python p20.py <number>")
