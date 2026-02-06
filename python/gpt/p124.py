import sys

if len(sys.argv) != 2:
    print("Error: Exactly one argument required.")
    sys.exit(1)

input_value = sys.argv[1]
print(f"Original: '{input_value}'")

input_value = input_value.rstrip()
print(f"Trimmed: '{input_value}'")

sys.exit(0)
