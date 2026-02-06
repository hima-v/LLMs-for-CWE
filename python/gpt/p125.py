import sys

if len(sys.argv) != 2:
    print("Error: Exactly one argument is required.")
    sys.exit(1)

input_arg = sys.argv[1]
print(f"Original: '{input_arg}'")

trimmed = input_arg.rstrip()
print(f"Trimmed:  '{trimmed}'")

sys.exit(0)
